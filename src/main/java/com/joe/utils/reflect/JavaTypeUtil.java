package com.joe.utils.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.joe.utils.common.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年04月04日 09:52 JoeKerouac Exp $
 */
@Slf4j
public class JavaTypeUtil {

    /**
     * super泛型，匹配? super Object这种泛型
     */
    private static final Pattern SUPER_PATTERN   = Pattern.compile("(.*) super.*");

    /**
     * extends泛型，匹配? extends Object这种泛型
     */
    private static final Pattern EXTENDS_PATTERN = Pattern.compile("(.*) extends.*");

    /**
     * 获取指定类上声明的泛型列表，例如有如下类：
     * <p>
     * <code>public class Test<String></code>
     * <p>
     * 对该类使用该方法将会获得String的class
     * 
     * @param clazz 类型
     * @return 类上声明的泛型列表
     */
    public static List<JavaType> getGenericSuperclasses(Class<?> clazz) {
        Assert.notNull(clazz, "clazz不能为null");
        Type genericSuperclass = clazz.getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return Arrays.stream(actualTypeArguments).map(JavaTypeUtil::createJavaType)
            .collect(Collectors.toList());
    }

    /**
     * /**
     * 构建map类型的JavaType
     *
     * @param m   map的Class
     * @param k   key的Class
     * @param v   value的Class
     * @param <K> key的实际类型
     * @param <V> value的实际类型
     * @param <M> map的实际类型
     * @return 对应的JavaType
     */
    public static <K, V, M extends Map<K, V>> JavaType createMapType(Class<M> m, Class<K> k,
                                                                     Class<V> v) {
        BaseType mapType = new BaseType();
        BaseType keyType = new BaseType();
        BaseType valueType = new BaseType();
        keyType.setType(k);
        valueType.setType(v);
        JavaType[] generics = { keyType, valueType };
        mapType.setType(m);
        mapType.setGenerics(generics);
        return mapType;
    }

    /**
     * 构建集合类型的JavaType
     *
     * @param k   集合的基本类型
     * @param t   集合
     * @param <T> 集合的泛型类型
     * @param <K> 集合的实际类型
     * @return 带泛型的集合类型
     */
    public static <T, K extends Collection<T>> JavaType createCollectionType(Class<K> k,
                                                                             Class<T> t) {
        BaseType collectionType = new BaseType();
        BaseType genericType = new BaseType();
        genericType.setType(t);
        JavaType[] generics = { genericType };
        collectionType.setType(k);
        collectionType.setGenerics(generics);
        return collectionType;
    }

    /**
     * 根据java系统类型得出自定义类型
     *
     * @param type java反射取得的类型
     * @return 自定义java类型说明
     */
    public static JavaType createJavaType(Type type) {
        if (type instanceof JavaType) {
            return (JavaType) type;
        }
        String typeName = type.getTypeName();
        JavaType javaType;
        if (type instanceof WildcardType) {
            // 该类型是不确定的泛型，即泛型为 ?
            log.debug("类型{}是不确定的泛型", typeName);
            WildcardType wildcardTypeImpl = (WildcardType) type;
            Type[] child = wildcardTypeImpl.getLowerBounds();// 子类
            Type[] parent = wildcardTypeImpl.getUpperBounds();// 父类
            javaType = new GenericType();
            GenericType genericType = (GenericType) javaType;
            if (child.length > 0) {
                log.debug("类型{}必须是{}的父类型", typeName, child[0]);
                genericType.setChild(createJavaType(child[0]));
            } else {
                log.debug("类型{}必须是{}的子类型", typeName, parent[0]);
                genericType.setParent(createJavaType(parent[0]));
            }
            genericType.setName(dealName(typeName));
        } else if (type instanceof ParameterizedType) {
            // 该类型存在泛型
            log.debug("类型{}存在泛型", typeName);
            ParameterizedType parameterizedTypeImpl = (ParameterizedType) type;
            Type[] types = parameterizedTypeImpl.getActualTypeArguments();
            JavaType[] generics = new JavaType[types.length];
            for (int i = 0; i < types.length; i++) {
                generics[i] = createJavaType(types[i]);
            }
            javaType = new BaseType();
            BaseType baseType = (BaseType) javaType;
            baseType.setType((Class<?>) parameterizedTypeImpl.getRawType());
            baseType.setGenerics(generics);
            baseType.setName(baseType.getType().getSimpleName());
        } else if (type instanceof TypeVariable) {
            // 该类型是泛型
            log.debug("类型{}是泛型", typeName);
            TypeVariable typeVariableImpl = (TypeVariable) type;
            javaType = new GenericType();
            GenericType genericType = (GenericType) javaType;
            // 指定名字的泛型只能继承，不能使用关键字super，所以getBounds该方法得出的是泛型的父类型
            genericType.setParent(createJavaType(typeVariableImpl.getBounds()[0]));
            genericType.setName(dealName(type.getTypeName()));
        } else if (type instanceof Class) {
            // 该类型是普通类型（没有泛型，本身也不是泛型参数）
            javaType = new BaseType();
            BaseType baseType = (BaseType) javaType;
            Class<?> clazz = (Class) type;
            baseType.setType(clazz);
            baseType.setName(clazz.getSimpleName());
        } else {
            throw new IllegalArgumentException("type[" + type + "]类型未知");
        }

        // 判断如果是基本类型的话是不是自定义类型，如果是的话对自定义类型的各个属性进行描述
        if (javaType instanceof BaseType) {
            BaseType baseType = (BaseType) javaType;
            //需要isSimple判断出来类型是否是基本类型，如果不是则认为类型是pojo类型，然后对该类型的各个字段进行描述
            if (!isNotPojo(baseType.getType())) {
                // 是自定义类型，对自定义类型的各个属性进行描述
                Map<String, JavaType> params = new TreeMap<>();
                // 获取该自定义类型的各个属性
                BeanUtils.CustomPropertyDescriptor[] props = BeanUtils
                    .getPropertyDescriptors(baseType.getType());
                for (BeanUtils.CustomPropertyDescriptor prop : props) {
                    if ("class".equals(prop.getName())) {
                        continue;
                    }
                    // 遍历属性，将属性转换为JavaType
                    // 获取属性读取方法上的注解
                    Annotation[] methodAnnotations = prop.getReadMethod().getAnnotations();
                    Annotation[] annotations;
                    try {
                        // 获取属性字段上的注解
                        Field field = baseType.getType().getField(prop.getName());
                        Annotation[] fieldAnnotations = field.getAnnotations();
                        annotations = new Annotation[methodAnnotations.length
                                                     + fieldAnnotations.length];
                        System.arraycopy(methodAnnotations, 0, annotations, 0,
                            methodAnnotations.length);
                        System.arraycopy(fieldAnnotations, 0, annotations, methodAnnotations.length,
                            fieldAnnotations.length);
                    } catch (Exception e) {
                        annotations = new Annotation[methodAnnotations.length];
                        System.arraycopy(methodAnnotations, 0, annotations, 0,
                            methodAnnotations.length);
                    }
                    JavaType filedType = createJavaType(
                        prop.getReadMethod().getGenericReturnType());
                    filedType.setAnnotations(annotations);
                    params.put(prop.getName(), filedType);
                }
                baseType.setIncludes(params);
            }
        }

        return javaType;
    }

    /**
     * 判断Class对象是否为指定的几种简单类型（该方法认为java自带简单类型包括java八大基本类型及其对应的封装类型、Number的子
     * 类、String、Collection的子类、Map的子类、Enum，如果不是这些类型将会认为该类型是一个复杂类型（pojo类型））
     *
     * @param clazz Class对象，不能为null
     * @return 如果是pojo则返回<code>false</code>
     * @throws NullPointerException 当传入Class对象为null时抛出该异常
     */
    public static boolean isNotPojo(Class<?> clazz) throws NullPointerException {
        Assert.notNull(clazz, "clazz不能为null");
        return Boolean.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)
               || Number.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)
               || String.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz)
               || Enum.class.isAssignableFrom(clazz) || isGeneralType(clazz);
    }

    /**
     * 判断是否是八大基本类型、枚举类型、String、Number类型
     * @param clazz Class
     * @return 如果不是以上几种类型返回false
     */
    public static boolean isSimple(Class<?> clazz) {
        return isGeneralType(clazz) || isBasic(clazz) || Enum.class.isAssignableFrom(clazz)
               || String.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz);
    }

    /**
     * 判断Class对象是否为八大基本类型的封装类型
     *
     * @param clazz Class对象，不能为null
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException 当传入Class对象为null时抛出该异常
     */
    public static boolean isBasic(Class<?> clazz) throws NullPointerException {
        Assert.notNull(clazz, "clazz不能为null");
        return Boolean.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)
               || Byte.class.isAssignableFrom(clazz) || Short.class.isAssignableFrom(clazz)
               || Integer.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)
               || Double.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz);
    }

    /**
     * 判断指定Class是否是8大基本类型（int、short等，不包含对应的封装类型）
     *
     * @param clazz class对象
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException 当传入Class对象为空时抛出该异常
     */
    public static boolean isGeneralType(Class<?> clazz) throws NullPointerException {
        Assert.notNull(clazz, "clazz不能为null");
        return clazz.isPrimitive();
    }

    /**
     * 从JavaType中抽取真实的基类
     *
     * @param type 指定的JavaType
     * @return 该指定JavaType对应的基类
     */
    public static Class<?> getRealType(JavaType type) {
        Assert.notNull(type, "type不能为null");
        if (type instanceof BaseType) {
            log.debug("参数不是泛型的");
            return ((BaseType) type).getType();
        } else {
            log.debug("参数是泛型的");
            JavaType parent = ((GenericType) type).getParent();
            JavaType child = ((GenericType) type).getChild();
            return parent == null ? getRealType(child) : getRealType(parent);
        }
    }

    /**
     * 判断指定数组名称是否是java八大基本类型（int[]、short[]、int[][]等，不包含对应的封装类型）
     *
     * @param clazz 指定数组名称
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException 当传入Class对象为空时抛出该异常
     */
    public static boolean isGeneralArrayType(Class<?> clazz) throws NullPointerException {
        Assert.notNull(clazz, "clazz不能为null");
        Class<?> componentType = clazz.getComponentType();
        return componentType != null && isGeneralType(componentType);
    }

    /**
     * 处理泛型名称
     *
     * @param fullName 泛型全名
     * @return 泛型的名称
     */
    private static String dealName(String fullName) {
        Matcher matcher = SUPER_PATTERN.matcher(fullName);
        String name;
        if (matcher.find()) {
            name = matcher.group(1);
        } else {
            matcher = EXTENDS_PATTERN.matcher(fullName);
            if (matcher.find()) {
                name = matcher.group(1);
            } else {
                name = fullName;
            }
        }
        return name;
    }
}
