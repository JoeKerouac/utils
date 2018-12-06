package com.joe.utils.proxy.bytebuddy;

import java.lang.reflect.Type;

import com.joe.utils.collection.CollectionUtil;
import com.joe.utils.common.StringUtils;
import com.joe.utils.proxy.MethodMetadata;
import com.joe.utils.proxy.ProxyClassLoader;
import com.joe.utils.proxy.ProxyClient;
import com.joe.utils.reflect.ClassUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月07日 11:31 JoeKerouac Exp $
 */
@Slf4j
public class ByteBuddyProxyClient implements ProxyClient {
    @Override
    public <T> Builder<T> createBuilder(Class<T> parent) {
        return new DefaultBuilder<>(parent);
    }

    @Override
    public <T> Builder<T> createBuilder(Class<T> parent, ProxyClassLoader loader) {
        return new DefaultBuilder<>(parent, loader);
    }

    @Override
    public ClientType getClientType() {
        return ClientType.BYTE_BUDDY;
    }

    private static class DefaultBuilder<T> extends Builder<T> {

        DefaultBuilder(Class<T> parent) {
            super(parent);
        }

        DefaultBuilder(Class<T> parent, ProxyClassLoader loader) {
            super(parent, loader);
        }

        @Override
        public T build() {
            BuilderHolder<T> builderHolder = new BuilderHolder<>(new ByteBuddy().subclass(parent));

            if (!StringUtils.isEmpty(name)) {
                builderHolder.setBuilder(builderHolder.getBuilder().name(name));
            }

            log.debug("开始处理要拦截的方法");
            proxyMap.forEach((k, v) -> {
                log.debug("使用方法[{}]替换/实现方法[{}]", v, k);
                MethodEqualsElementMatcher matcher = new MethodEqualsElementMatcher(k);

                // 下面如果不用LambdaInterception包装如果v是一个lambda表达式将会在build的时候抛出异常
                builderHolder.setBuilder(builderHolder.getBuilder().method(matcher)
                    .intercept(MethodDelegation.to(new GeneralInterceptor(v))));
            });
            log.debug("要拦截的方法处理完毕");

            Class<? extends T> tClass = builderHolder.getBuilder().make().load(loader,
                ((classLoader, types) -> CollectionUtil.convert(types, classLoader::buildClass)))
                .getLoaded();
            return ClassUtils.getInstance(tClass);
        }

        @Data
        @AllArgsConstructor
        private static class BuilderHolder<T> {
            private DynamicType.Builder<T> builder;
        }
    }

    private static class MethodEqualsElementMatcher implements ElementMatcher<MethodDescription> {

        private final MethodMetadata metadata;

        MethodEqualsElementMatcher(MethodMetadata metadata) {
            this.metadata = metadata;
        }

        @Override
        public boolean matches(MethodDescription target) {
            return compareName(target) && compareReturnType(target) && compareParams(target);
        }

        /**
         * 比较ByteBuddy方法说明的方法名与metadata是否一致
         * @param target 方法说明
         * @return 返回true表示一致
         */
        private boolean compareName(MethodDescription target) {
            return target.getActualName().equals(metadata.getName());
        }

        /**
         * 比较ByteBuddy方法说明的返回类型与metadata是否一致
         * @param target ByteBuddy的方法说明
         * @return 返回true表示一致
         */
        private boolean compareReturnType(MethodDescription target) {
            return target.getReturnType().getTypeName()
                .equals(metadata.getReturnType().getTypeName());
        }

        /**
         * 比较ByteBuddy方法说明的参数列表类型与metadata是否一致
         * @param target ByteBuddy的方法说明
         * @return 返回true表示一致
         */
        private boolean compareParams(MethodDescription target) {
            ParameterList<?> parameterList = target.getParameters();
            Type[] params = metadata.getParams();
            if (parameterList.size() != params.length) {
                return false;
            } else if (parameterList.size() == 0) {
                return true;
            }

            int size = parameterList.size();
            for (int i = 0; i < size; i++) {
                if (!parameterList.get(i).getType().getTypeName().equals(params[i].getTypeName())) {
                    return false;
                }
            }

            return true;
        }
    }
}
