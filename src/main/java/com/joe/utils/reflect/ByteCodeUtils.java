package com.joe.utils.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.joe.utils.common.Assert;
import com.joe.utils.common.string.StringUtils;
import com.joe.utils.reflect.type.JavaTypeUtil;

/**
 * byte code工具
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月19日 20:40 JoeKerouac Exp $
 */
public class ByteCodeUtils {

    /**
     * 构造器方法名
     */
    public static final String INIT = "<init>";

    /**
     * 获取Class类型的byte code标识，例如int对应的是I，String对应的是Ljava/lang/String，注意：只要不是原生类型肯定是以'L'开头
     * 或者N个'['后跟一个'L'，'['标识数组
     * @param clazz Class对象
     * @return 对应的byte code类型，结尾如果是对象会以';'结尾，如果是放在返回值时需要删除
     */
    public static String getByteCodeType(Class<?> clazz) {
        Assert.notNull(clazz);
        if (byte.class == clazz) {
            return "B";
        } else if (short.class == clazz) {
            return "S";
        } else if (int.class == clazz) {
            return "I";
        } else if (long.class == clazz) {
            return "J";
        } else if (double.class == clazz) {
            return "D";
        } else if (float.class == clazz) {
            return "F";
        } else if (boolean.class == clazz) {
            return "Z";
        } else if (char.class == clazz) {
            return "C";
        } else if (void.class == clazz) {
            // void只有在返回值中才会有
            return "V";
        } else if (StringUtils.trim(clazz.getName(), "[").startsWith("L")) {
            // 是对象数组（这里的对象指的是非原生类型）
            return clazz.getName().replace(".", "/");
        } else if (!JavaTypeUtil.isGeneralType(clazz)) {
            // 是对象（这里的对象指的是非原生类型）
            return "L" + clazz.getName().replace(".", "/") + ";";
        } else {
            return clazz.getName().replace(".", "/");
        }
    }

    /**
     * 获取指定构造器的byte code说明
     * @param constructor 构造器
     * @return 指定构造器的说明
     */
    public static String getConstructorDesc(Constructor<?> constructor) {
        Assert.notNull(constructor);
        return getDesc(void.class, constructor.getParameterTypes());
    }

    /**
     * 获取指定方法的说明
     * @param method 方法对象
     * @return 方法说明
     */
    public static String getMethodDesc(Method method) {
        Assert.notNull(method);
        return getDesc(method.getReturnType(), method.getParameterTypes());
    }

    /**
     * 将类名转换为byte code中的类名标识
     * @param clazz Class对象
     * @return byte code中的类名标识
     */
    public static String convert(Class<?> clazz) {
        return convert(clazz.getName());
    }

    /**
     * 将类名转换为byte code中的类名标识
     * @param className Class名字
     * @return byte code中的类名标识
     */
    public static String convert(String className) {
        return className.replace(".", "/");
    }

    /**
     * 获取指定参数类型、返回值的方法的byte code说明
     * @param returnType 方法返回值
     * @param paramTypes 方法参数
     * @return byte code方法说明
     */
    public static String getDesc(Class<?> returnType, Class<?>... paramTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Class<?> type : paramTypes) {
            sb.append(ByteCodeUtils.getByteCodeType(type));
        }
        sb.append(")");
        //        sb.append(StringUtils.trim(ByteCodeUtils.getByteCodeType(returnType), ";"));
        sb.append(ByteCodeUtils.getByteCodeType(returnType));
        return sb.toString();
    }
}
