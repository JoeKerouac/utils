package com.joe.utils.ws;

import com.joe.http.client.IHttpClient;
import com.joe.http.request.IHttpGet;
import com.joe.http.request.IHttpPost;
import com.joe.http.request.IHttpRequestBase;
import com.joe.http.response.IHttpResponse;
import com.joe.utils.parse.json.JsonParser;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP资源工厂，主要为了方便测试HTTP接口（接口需要符合JAX-RS (JSR 311)规范），可以达
 * 到像调用本地方法一样调用远程方法。
 * <p>
 * 支持说明：<p>
 * 只支持POST和GET方法<p>
 * 参数只支持从HEADER、FORM表单、PATH中和JSON数据中取
 * <p>
 * 用法说明：<p>
 * ResourceFactory resourceFactory = ResourceFactory.build("http://127.0.0.1:8080/ws/");<p>
 * 通过以下方法得到资源类的实例后就可以与调用普通java方法一样调用资源了（PS：将Resource替换为实际的资源类即可）<p>
 * Resource resource = resourceFactory.buildSuccess(Resource.class);<p>
 * resource.somemethod("参数");
 *
 * @author joe
 */
public class ResourceFactory {
    private static final Logger logger = LoggerFactory.getLogger(ResourceFactory.class);
    private static final Map<String, ResourceFactory> cache = new HashMap<>();
    private String baseUrl;

    private ResourceFactory(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 构建资源工厂
     *
     * @param baseUrl 资源的根目录
     * @return 资源工厂
     */
    public static ResourceFactory build(String baseUrl) {
        if (!cache.containsKey(baseUrl)) {
            synchronized (cache) {
                if (!cache.containsKey(baseUrl)) {
                    cache.put(baseUrl, new ResourceFactory(baseUrl));
                }
            }
        }
        return cache.get(baseUrl);
    }

    /**
     * 构建可调用的资源
     *
     * @param t   资源对应的类（该方法假设类中的接口都符合规范，如果接口不符合规范那么将会出现异常）
     * @param <T> 资源类型
     * @return 可调用的资源
     */
    public <T> T build(Class<T> t) throws NotResourceException {
        if (t.getAnnotation(Path.class) == null) {
            logger.error("类{}不是资源类，不能调用", t);
            throw new NotResourceException(t);
        }
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(t);
        enhancer.setCallback(CglibHTTPProxy.build(baseUrl));
        @SuppressWarnings("unchecked")
        T resource = (T) enhancer.create();
        return resource;
    }

    /**
     * CGLIB的HTTP代理，用于测试接口使用，暂时只支持返回JSON类型的接口测试
     */
    private static class CglibHTTPProxy implements MethodInterceptor {
        private static final JsonParser parser = JsonParser.getInstance();
        private static final Logger logger = LoggerFactory.getLogger(CglibHTTPProxy.class);
        private static final IHttpClient client = IHttpClient.builder().build();
        private static final Map<String, CglibHTTPProxy> cache = new HashMap<>();
        private String baseUrl;

        private CglibHTTPProxy(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        /**
         * 构建代理，主要为了缓存
         *
         * @param baseUrl 代理的根目录
         * @return 代理
         */
        static CglibHTTPProxy build(String baseUrl) {
            if (!CglibHTTPProxy.cache.containsKey(baseUrl)) {
                synchronized (cache) {
                    if (!cache.containsKey(baseUrl)) {
                        cache.put(baseUrl, new CglibHTTPProxy(baseUrl));
                    }
                }
            }
            return cache.get(baseUrl);
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            logger.debug("开始代理方法");
            if (method.getAnnotation(Path.class) == null) {
                logger.error("方法{}不是资源方法，不能调用", method);
                throw new NotResourceException(method);
            }
            logger.debug("开始构建HTTP请求");
            IHttpRequestBase request = build(o, objects, method);
            logger.debug("开始发送HTTP请求");
            IHttpResponse response = client.execute(request);
            logger.debug("HTTP请求发送完成，HTTP请求状态码为：{}", response.getStatus());
            String result = response.getResult();
            logger.debug("HTTP请求结果为：{}", result);
            return parser.readAsObject(result, method.getReturnType());
        }

        /**
         * 根据信息构建HTTP请求
         *
         * @param o       资源对象
         * @param objects 参数
         * @param method  资源方法
         * @return 该资源对应的请求
         */
        private IHttpRequestBase build(Object o, Object[] objects, Method method) {
            //此时的o对象已经是cglib动态生成的代理对象了，所以需要获取他的父类才是真正的被代理类
            //获取请求前缀
            Path prePath = o.getClass().getSuperclass().getDeclaredAnnotation(Path.class);
            String prefix = prePath.value();
            logger.debug("请求的前缀是：{}", prefix);

            Path namePath = method.getAnnotation(Path.class);
            String name = namePath.value();
            logger.debug("接口名是：{}", name);

            if (!prefix.startsWith("/")) {
                prefix = "/" + prefix;
            }
            if (!prefix.endsWith("/")) {
                prefix = prefix + "/";
            }
            if (name.startsWith("/")) {
                name = name.replaceFirst("/", "");
            }
            String path = prefix + name;
            logger.debug("请求路径是：{}", path);

            //判断是否是POST请求
            boolean post = false;
            if (method.getAnnotation(POST.class) != null) {
                post = true;
            }

            logger.debug("请求是{}请求", post ? "POST" : "GET");

            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            path = baseUrl + path;
            logger.debug("要请求的路径初步解析为：{}", path);
            IHttpRequestBase request;
            if (post) {
                request = new IHttpPost(path);
            } else {
                request = new IHttpGet(path);
            }

            logger.debug("开始解析参数");
            // 解析参数，根据参数类型放入不同的地方
            Parameter[] parameters = method.getParameters();
            String data = null;

            //最后一个没有注解的参数在参数列表中的下标
            int index = -1;
            for (int i = 0; i < parameters.length; i++) {
                if (objects[i] == null) {
                    logger.debug("参数为null，跳过该参数");
                    continue;
                }
                Object value = objects[i];

                Parameter parameter = parameters[i];
                Annotation[] annotations = parameter.getAnnotations();

                //如果该参数没有加合适的注解该值到最后还是true，否则会变为false
                boolean empty = true;
                for (Annotation annotation : annotations) {
                    if (annotation instanceof QueryParam) {
                        QueryParam queryParam = (QueryParam) annotation;
                        logger.debug("参数是QueryParam，参数名为：{}，参数值为：{}", queryParam.value(), value);
                        request.addQueryParam(queryParam.value(), String.valueOf(value));
                        empty = false;
                        break;
                    } else if (annotation instanceof HeaderParam) {
                        //解析headerparam
                        HeaderParam headerParam = (HeaderParam) annotation;
                        logger.debug("参数是HeaderParam，参数名为：{}，参数值为：{}", headerParam.value(), value);
                        request.addHeader(headerParam.value(), String.valueOf(value));
                        empty = false;
                        break;
                    } else if (annotation instanceof PathParam) {
                        PathParam pathParam = (PathParam) annotation;
                        logger.debug("参数是PathParam，参数名为：{}，参数值为：{}", pathParam.value(), value);
                        path = path.replace("{" + pathParam.value() + "}", String.valueOf(value));
                        empty = false;
                        break;
                    } else if (annotation instanceof FormParam) {
                        FormParam formParam = (FormParam) annotation;
                        logger.debug("参数是FormParam，参数名为：{}，参数值为：{}", formParam.value(), value);
                        if (data == null) {
                            data = formParam.value() + "=" + value;
                        } else {
                            data += "&" + formParam.value() + "=" + value;
                        }
                        empty = false;
                        break;
                    } else if (annotation instanceof Context) {
                        //当前是需要context的，跳过
                        empty = false;
                        break;
                    }
                }

                if (empty) {
                    index = i;
                }
            }

            if (index != -1) {
                logger.debug("参数是json类型的参数");
                data = parser.toJson(objects[index]);
            }

            if (data != null) {
                logger.debug("请求包含数据：{}", data);
                request.setEntity(data);
            }

            //获取请求的content-type
            Consumes consumes = method.getAnnotation(Consumes.class);
            if (consumes != null) {
                String contentType = consumes.value()[0];
                logger.debug("请求的content-type为：{}", contentType);
                request.setContentType(contentType);
            }

            //有可能有PathPram，有PathPram时就会改变URL，所以需要更新
            request.setUrl(path);
            return request;
        }
    }
}
