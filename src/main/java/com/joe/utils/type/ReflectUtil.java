package com.joe.utils.type;

import com.joe.utils.common.BeanUtils;
import com.joe.utils.common.BeanUtils.CustomPropertyDescriptor;
import com.joe.utils.scan.ClassScanner;
import com.joe.utils.scan.MethodScanner;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * java类型相关工具类
 *
 * @author joe
 */
@Slf4j
public class ReflectUtil {
    private static final Logger logger = LoggerFactory.getLogger(ReflectUtil.class);
    private static final Pattern superPattern = Pattern.compile("(.*) super.*");
    private static final Pattern extendsPattern = Pattern.compile("(.*) extends.*");
    private static final ClassScanner CLASS_SCANNER = ClassScanner.getInstance();
    private static final MethodScanner METHOD_SCANNER = MethodScanner.getInstance();

    private ReflectUtil() {
    }

    /**
     * 获取指定包中所有带指定注解的类
     *
     * @param annotation 注解
     * @param packages   包集合
     * @return 包集合中所有带指定注解的类
     */
    public static List<Class<?>> getAllAnnotationPresentClass(Class<? extends Annotation> annotation, String...
            packages) {
        if (packages == null || packages.length == 0) {
            return Collections.emptyList();
        }

        log.debug("开始扫描包{}下的带注解[{}]的列表", packages, annotation);
        List<Class<?>> classes = CLASS_SCANNER.scan(Collections.singletonList(clazz -> {
            return !clazz.isAnnotationPresent(annotation);
        }), packages);
        return classes;
    }

    /**
     * 获取指定类型内所有带有指定注解的方法的集合
     *
     * @param type       指定类型
     * @param annotation 指定注解
     * @return 带有指定注解的方法集合
     */
    public static List<Method> getAllAnnotationPresentMethod(Class<?> type, Class<? extends Annotation> annotation) {
        Method[] methods = type.getDeclaredMethods();
        if (methods.length == 0) {
            return Collections.emptyList();
        }
        return Stream.of(methods).filter(method -> method.isAnnotationPresent(annotation)).collect(Collectors.toList());
    }


    /**
     * 获取指定对象中指定字段名对应的字段的值
     *
     * @param obj       对象
     * @param fieldName 字段名
     * @param <T>       字段类型
     * @return 指定对象中指定字段名对应字段的值
     * @throws NoSuchFieldException 当给定对象不存在指定字段的时候抛出该异常
     */
    public static <T extends Object> T getFieldValue(Object obj, String fieldName) throws NoSuchFieldException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        try {
            return (T) field.get(obj);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            //不可能有这种情况
            throw new RuntimeException(e);
        }
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
    public static <K, V, M extends Map<K, V>> JavaType createMapType(Class<M> m, Class<K> k, Class<V> v) {
        BaseType mapType = new BaseType();
        BaseType keyType = new BaseType();
        BaseType valueType = new BaseType();
        keyType.setType(k);
        valueType.setType(v);
        JavaType[] generics = {keyType, valueType};
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
    public static <T, K extends Collection<T>> JavaType createCollectionType(Class<K> k, Class<T> t) {
        BaseType collectionType = new BaseType();
        BaseType genericType = new BaseType();
        genericType.setType(t);
        JavaType[] generics = {genericType};
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
            logger.debug("类型{}是不确定的泛型", typeName);
            WildcardType wildcardTypeImpl = (WildcardType) type;
            Type[] child = wildcardTypeImpl.getLowerBounds();// 子类
            Type[] parent = wildcardTypeImpl.getUpperBounds();// 父类
            javaType = new GenericType();
            GenericType genericType = (GenericType) javaType;
            if (child.length > 0) {
                logger.debug("类型{}必须是{}的父类型", typeName, child[0]);
                genericType.setChild(createJavaType(child[0]));
            } else {
                logger.debug("类型{}必须是{}的子类型", typeName, parent[0]);
                genericType.setParent(createJavaType(parent[0]));
            }
            genericType.setName(dealName(typeName));
        } else if (type instanceof ParameterizedType) {
            // 该类型存在泛型
            logger.debug("类型{}存在泛型", typeName);
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
            logger.debug("类型{}是泛型", typeName);
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
            if (!isSimple(baseType.getType())) {
                // 是自定义类型，对自定义类型的各个属性进行描述
                Map<String, JavaType> params = new TreeMap<>();
                // 获取该自定义类型的各个属性
                CustomPropertyDescriptor[] props = BeanUtils.getPropertyDescriptors(baseType.getType());
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
                        annotations = new Annotation[methodAnnotations.length + fieldAnnotations.length];
                        System.arraycopy(methodAnnotations, 0, annotations, 0, methodAnnotations.length);
                        System.arraycopy(fieldAnnotations, 0, annotations, methodAnnotations.length,
                                fieldAnnotations.length);
                    } catch (Exception e) {
                        annotations = new Annotation[methodAnnotations.length];
                        System.arraycopy(methodAnnotations, 0, annotations, 0, methodAnnotations.length);
                    }
                    JavaType filedType = createJavaType(prop.getReadMethod().getGenericReturnType());
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
     * 类、String、Collection的子类、Map的子类、Enum，如果不是这六种类型将会认为该类型是一个复杂类型（pojo类型））
     *
     * @param clazz Class对象，不能为null
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException 当传入Class对象为null时抛出该异常
     */
    public static boolean isSimple(Class<?> clazz) throws NullPointerException {
        if (clazz == null)
            throw new NullPointerException("Class不能为null");
        else
            return Boolean.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)
                    || Number.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)
                    || String.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz)
                    || Enum.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz) || char.class
                    .isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz) || short.class.isAssignableFrom
                    (clazz)
                    || int.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz) || double.class
                    .isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz);
    }

    /**
     * 判断Class对象是否为八大基本类型的封装类型，这六种类型将会认为该类型是一个复杂类型）
     *
     * @param clazz Class对象，不能为null
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException 当传入Class对象为null时抛出该异常
     */
    public static boolean isBasic(Class<?> clazz) throws NullPointerException {
        if (clazz == null)
            throw new NullPointerException("Class不能为null");
        else
            return Boolean.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz) || Byte.class
                    .isAssignableFrom(clazz) || Short.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom
                    (clazz) || Long.class.isAssignableFrom(clazz) || Double.class.isAssignableFrom(clazz) || Float.class
                    .isAssignableFrom(clazz);
    }

    /**
     * 从JavaType中抽取真实的基类
     *
     * @param type 指定的JavaType
     * @return 该指定JavaType对应的基类
     */
    public static Class<?> getRealType(JavaType type) {
        if (type instanceof BaseType) {
            logger.debug("参数不是泛型的");
            return ((BaseType) type).getType();
        } else {
            logger.debug("参数是泛型的");
            JavaType parent = ((GenericType) type).getParent();
            JavaType child = ((GenericType) type).getChild();
            return parent == null ? getRealType(child) : getRealType(parent);
        }
    }

    /**
     * 判断指定Class是否是8大基本类型（int、short等，不包含对应的封装类型）
     *
     * @param clazz class对象
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException 当传入Class对象为空时抛出该异常
     */
    public static boolean isGeneralType(Class<?> clazz) throws NullPointerException {
        if (clazz == null) {
            throw new NullPointerException("Class不能为null");
        }
        return clazz.isPrimitive();
    }

    /**
     * 判断指定数组名称是否是java八大基本类型（int[]、short[]等，不包含对应的封装类型）
     *
     * @param clazz 指定数组名称
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException 当传入Class对象为空时抛出该异常
     */
    public static boolean isGeneralArrayType(Class<?> clazz) throws NullPointerException {
        if (clazz == null) {
            throw new NullPointerException("Class不能为null");
        }
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
        return "byte[]".equals(name) || "short[]".equals(name) || "int[]".equals(name) || "long[]".equals(name)
                || "double[]".equals(name) || "float[]".equals(name) || "boolean[]".equals(name)
                || "char[]".equals(name);
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
