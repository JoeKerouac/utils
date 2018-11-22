package com.joe.utils.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.joe.utils.collection.LRUCacheMap;
import com.joe.utils.common.Assert;
import com.joe.utils.reflect.BeanUtils.CustomPropertyDescriptor;
import com.joe.utils.common.StringUtils;
import com.joe.utils.scan.ClassScanner;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * java类型相关工具类
 *
 * @author joe
 */
@Slf4j
public class ReflectUtil {
    private static final Pattern                        superPattern    = Pattern
        .compile("(.*) super.*");
    private static final Pattern                        extendsPattern  = Pattern
        .compile("(.*) extends.*");
    private static final ClassScanner                   CLASS_SCANNER   = ClassScanner
        .getInstance();
    /**
     * 方法缓存
     */
    private static final Map<MethodKey, Method>         METHOD_CACHE    = new LRUCacheMap<>();
    /**
     * field缓存
     */
    private static final Map<FieldKey, Field>           FIELD_CACHE     = new LRUCacheMap<>();
    /**
     * 所有field缓存
     */
    private static final LRUCacheMap<Class<?>, Field[]> ALL_FIELD_CACHE = new LRUCacheMap<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class MethodKey {
        private String     methodName;
        private Class<?>   clazz;
        private Class<?>[] parameterTypes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class FieldKey {
        private String   fieldName;
        private Class<?> clazz;
    }

    private ReflectUtil() {
    }

    /**
     * 调用指定对象的指定方法
     * @param obj 指定对象，不能为空
     * @param methodName 要调用的方法名，不能为空
     * @param parameterTypes  方法参数类型，方法没有参数请传null
     * @param args 参数
     * @param <R> 结果类型
     * @return 调用结果
     */
    @SuppressWarnings("unchecked")
    public static <R> R invoke(Object obj, String methodName, Class<?>[] parameterTypes,
                               Object... args) {
        Assert.isTrue(parameterTypes != null ^ args != null, "方法参数类型列表必须和方法参数列表一致");
        Assert.isTrue(parameterTypes == null || parameterTypes.length == args.length,
            "方法参数类型列表长度必须和方法参数列表长度一致");
        Method method = getMethod(obj.getClass(), methodName, parameterTypes);
        try {
            return (R) method.invoke(obj, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ReflectException("调用方法[" + methodName + "]失败", e);
        }
    }

    /**
     * 获取指定类型中指定的方法
     * @param clazz 类型
     * @param methodName 方法名
     * @param parameterTypes 方法参数类型
     * @return 指定方法，获取不到时会抛出异常
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        Assert.notNull(clazz, "类型不能为空");
        Assert.notNull(methodName, "方法名不能为空");

        return METHOD_CACHE.compute(new MethodKey(methodName, clazz, parameterTypes), (k, v) -> {
            if (v == null) {
                try {
                    return allowAccess(clazz.getDeclaredMethod(methodName, parameterTypes));
                } catch (NoSuchMethodException e) {
                    log.error(
                        StringUtils.format("类[{}]中不存在方法名为[{}]、方法列表为[{}]的方法", clazz, methodName,
                            parameterTypes == null ? "null" : Arrays.toString(parameterTypes)));
                    throw new ReflectException(
                        StringUtils.format("类[{}]中不存在方法名为[{}]、方法列表为[{}]的方法", clazz, methodName,
                            parameterTypes == null ? "null" : Arrays.toString(parameterTypes)),
                        e);
                }
            } else {
                return v;
            }
        });
    }

    /**
     * 执行方法
     * @param method 要调用的方法
     * @param target 方法所在Class的实例
     * @param params 调用方法的参数
     * @param <T> 方法返回类型
     * @return 方法调用结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T execMethod(Method method, Object target, Object... params) {
        allowAccess(method);
        try {
            return (T) method.invoke(target, params);
        } catch (Throwable e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 获取指定包中所有带指定注解的类
     *
     * @param annotation 注解
     * @param packages   包集合
     * @return 包集合中所有带指定注解的类
     */
    public static List<Class<?>> getAllAnnotationPresentClass(Class<? extends Annotation> annotation,
                                                              String... packages) {
        if (packages == null || packages.length == 0) {
            return Collections.emptyList();
        }

        log.debug("开始扫描包{}下的带注解[{}]的列表", packages, annotation);
        List<Class<?>> classes = CLASS_SCANNER.scanByFilter(
            Collections.singletonList(clazz -> !clazz.isAnnotationPresent(annotation)), packages);
        log.debug("包{}下的带注解{}的列表为：[{}]", packages, annotation, classes);
        return classes;
    }

    /**
     * 获取指定类型内所有带有指定注解的方法的集合
     *
     * @param type       指定类型
     * @param annotation 指定注解
     * @return 带有指定注解的方法集合
     */
    public static List<Method> getAllAnnotationPresentMethod(Class<?> type,
                                                             Class<? extends Annotation> annotation) {
        Method[] methods = type.getDeclaredMethods();
        if (methods.length == 0) {
            return Collections.emptyList();
        }
        return Stream.of(methods).filter(method -> method.isAnnotationPresent(annotation))
            .collect(Collectors.toList());
    }

    /**
     * 获取指定对象中指定字段名对应的字段的值
     *
     * @param obj       对象
     * @param fieldName 字段名
     * @param <T>       字段类型
     * @return 指定对象中指定字段名对应字段的值
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> T getFieldValue(Object obj, String fieldName) {
        Assert.notNull(obj, "obj不能为空");
        Assert.notNull(fieldName, "fieldName不能为空");

        Field field = getField(obj.getClass(), fieldName);
        try {
            return (T) field.get(obj);
        } catch (IllegalArgumentException e) {
            //不可能有这种情况
            throw new ReflectException(e);
        } catch (IllegalAccessException e) {
            String msg = StringUtils.format("类型[{}]的字段[{}]不允许访问", obj.getClass(), fieldName);
            log.error(msg);
            throw new ReflectException(msg, e);
        }
    }

    /**
     * 设置指定对象指定对象名的字段值
     * @param obj 对象
     * @param fieldName 字段名
     * @param fieldValue 字段值
     * @param <T> 字段值的类型
     * @return 字段值原样返回
     */
    public static <T extends Object> T setFieldValue(Object obj, String fieldName, T fieldValue) {
        Assert.notNull(obj, "obj不能为空");
        Assert.notNull(fieldName, "fieldName不能为空");
        Field field = getField(obj.getClass(), fieldName);
        try {
            field.set(obj, fieldValue);
        } catch (IllegalAccessException e) {
            String msg = StringUtils.format("类型[{}]的字段[{}]不允许设置", obj.getClass(), fieldName);
            log.error(msg);
            throw new ReflectException(msg, e);
        }
        return fieldValue;
    }

    /**
     * 从指定Class中获取指定Field，并尝试将其accessible属性设置为true
     * @param clazz Class
     * @param fieldName fieldName
     * @return Field，不会为null，只会抛出异常
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        Assert.notNull(clazz, "clazz不能为空");
        Assert.notNull(fieldName, "fieldName不能为空");
        return FIELD_CACHE.compute(new FieldKey(fieldName, clazz), (k, v) -> {
            if (v == null) {
                try {
                    return allowAccess(clazz.getDeclaredField(fieldName));
                } catch (NoSuchFieldException e) {
                    log.error(StringUtils.format("类[{}]中不存在字段[{}]", clazz, fieldName));
                    throw new ReflectException(
                        StringUtils.format("类[{}]中不存在字段[{}]", clazz, fieldName), e);
                }
            } else {
                return v;
            }
        });
    }

    /**
     * 获取指定Class的所有field（包含父类的）
     * @param clazz Class
     * @return 所有field数组
     */
    public static Field[] getAllFields(Class<?> clazz) {
        Assert.notNull(clazz, "clazz不能为空");
        return ALL_FIELD_CACHE.compute(clazz, (k, v) -> {
            if (v == null) {
                List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));

                //查找是否存在父类，如果存在且不是Object那么查找父类的field
                Class<?> superClass = clazz.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    fields.addAll(Arrays.asList(getAllFields(superClass)));
                }

                // 遍历设置访问权限，同时加入单个field的缓存
                fields.stream().map(ReflectUtil::allowAccess).forEach(
                    f -> FIELD_CACHE.compute(new FieldKey(f.getName(), clazz), (fk, fv) -> {
                        if (fv == null) {
                            return f;
                        } else {
                            return fv;
                        }
                    }));

                return fields.toArray(new Field[0]);
            } else {
                return v;
            }
        });
    }

    /**
     * 更改AccessibleObject的访问权限
     * @param object AccessibleObject
     * @param <T> AccessibleObject的具体类型
     * @return AccessibleObject
     */
    public static <T extends AccessibleObject> T allowAccess(T object) {
        try {
            object.setAccessible(true);
        } catch (SecurityException e) {
            log.warn("无法更改[{}]的访问权限", object);
        }
        return object;
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
     * 判断字段是否是final的
     *
     * @param field 字段
     * @return 返回true表示是final
     */
    public static boolean isFinal(Field field) {
        int modifier = field.getModifiers();
        return isFinal(modifier);
    }

    /**
     * 判断方法、构造器是否是final
     *
     * @param executable 方法、构造器对象
     * @return 返回true表示是final
     */
    public static boolean isFinal(Executable executable) {
        int modifier = executable.getModifiers();
        return isFinal(modifier);
    }

    /**
     * 判断字段是否是public
     *
     * @param field 字段
     * @return 返回true表示是public
     */
    public static boolean isPublic(Field field) {
        int modifier = field.getModifiers();
        return isPublic(modifier);
    }

    /**
     * 判断方法、构造器是否是public
     *
     * @param executable 方法、构造器对象
     * @return 返回true表示是public
     */
    public static boolean isPublic(Executable executable) {
        int modifier = executable.getModifiers();
        return isPublic(modifier);
    }

    /**
     * 判断方法是否是static
     * @param method 方法
     * @return true表示方法是静态的
     */
    public static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * 判断方法是否是抽象的
     * @param method 方法
     * @return 返回true表示方法是抽象的
     */
    public static boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    /**
     * 判断字段是否是static
     * @param field 字段
     * @return true表示字段是静态的
     */
    public static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
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
                CustomPropertyDescriptor[] props = BeanUtils
                    .getPropertyDescriptors(baseType.getType());
                for (CustomPropertyDescriptor prop : props) {
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
     * 判断Class对象是否为八大基本类型的封装类型，这六种类型将会认为该类型是一个复杂类型）
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
     * 判断指定数组名称是否是java八大基本类型（int[]、short[]等，不包含对应的封装类型）
     *
     * @param clazz 指定数组名称
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException 当传入Class对象为空时抛出该异常
     */
    public static boolean isGeneralArrayType(Class<?> clazz) throws NullPointerException {
        Assert.notNull(clazz, "clazz不能为null");
        String name = clazz.getName();
        return isGeneralArrayType(name);
    }

    /**
     * 判断指定数组是否是java八大基本类型（int[]、short[]等，不包含对应的封装类型）
     *
     * @param name 指定数组名称
     * @return 如果是基本类型则返回<code>true</code>
     */
    public static boolean isGeneralArrayType(String name) {
        return "byte[]".equals(name) || "short[]".equals(name) || "int[]".equals(name)
               || "long[]".equals(name) || "double[]".equals(name) || "float[]".equals(name)
               || "boolean[]".equals(name) || "char[]".equals(name);
    }

    /**
     * 处理泛型名称
     *
     * @param fullName 泛型全名
     * @return 泛型的名称
     */
    private static String dealName(String fullName) {
        Matcher matcher = superPattern.matcher(fullName);
        String name;
        if (matcher.find()) {
            name = matcher.group(1);
        } else {
            matcher = extendsPattern.matcher(fullName);
            if (matcher.find()) {
                name = matcher.group(1);
            } else {
                name = fullName;
            }
        }
        return name;
    }

    /**
     * 判断修饰符是否是final
     *
     * @param modifier 修饰符
     * @return 返回true表示是final类型
     */
    private static boolean isFinal(int modifier) {
        return Modifier.isFinal(modifier);
    }

    /**
     * 判断修饰符是否是public
     *
     * @param modifier 修饰符
     * @return 返回true表示是public
     */
    private static boolean isPublic(int modifier) {
        return Modifier.isPublic(modifier);
    }
}
