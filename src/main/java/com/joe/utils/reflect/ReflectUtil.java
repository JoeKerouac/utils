package com.joe.utils.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import com.joe.utils.collection.CollectionUtil;
import com.joe.utils.collection.LRUCacheMap;
import com.joe.utils.common.Assert;
import com.joe.utils.common.string.StringFormater;
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
    private static final ClassScanner CLASS_SCANNER = ClassScanner.getInstance();

    /**
     * 方法缓存
     */
    private static final Map<MethodKey, Method> METHOD_CACHE = new LRUCacheMap<>();
    /**
     * field缓存
     */
    private static final Map<FieldKey, Field> FIELD_CACHE = new LRUCacheMap<>();

    /**
     * 所有field缓存
     */
    private static final LRUCacheMap<Class<?>, Field[]> ALL_FIELD_CACHE = new LRUCacheMap<>();

    /**
     * 所有方法缓存
     */
    private static final LRUCacheMap<Class<?>, List<Method>> ALL_METHOD_CACHE = new LRUCacheMap<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class MethodKey {

        /**
         * 方法名
         */
        private String methodName;

        /**
         * 方法声明类
         */
        private Class<?> clazz;

        /**
         * 参数类型
         */
        private Class<?>[] parameterTypes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class FieldKey {
        private String fieldName;
        private Class<?> clazz;
    }

    private ReflectUtil() {}

    /**
     * 获取指定Class的指定参数构造器
     * 
     * @param type
     *            指定Class
     * @param parameterTypes
     *            构造器参数列表
     * @param <T>
     *            构造器类型
     * @return 构造器
     */
    public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>... parameterTypes) {
        try {
            return type.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            log.error(StringFormater.simpleFormat("类[{0}]中不存在参数列表为[{1}]的构造器", type,
                parameterTypes == null ? "null" : Arrays.toString(parameterTypes)));
            throw new ReflectException(StringFormater.simpleFormat("类[{0}]中不存在参数列表为[{1}]的构造器", type,
                parameterTypes == null ? "null" : Arrays.toString(parameterTypes)), e);
        }
    }

    /**
     * 调用指定对象的指定无参数方法
     * 
     * @param obj
     *            指定对象，不能为空，如果要调用的方法为静态方法那么传入Class对象
     * @param methodName
     *            要调用的方法名，不能为空
     * @param <R>
     *            结果类型
     * @return 调用结果
     */
    public static <R> R invoke(Object obj, String methodName) {
        return invoke(obj, methodName, null);
    }

    /**
     * 调用指定对象的指定方法
     * 
     * @param obj
     *            指定对象，不能为空，如果要调用的方法为静态方法那么传入Class对象
     * @param methodName
     *            要调用的方法名，不能为空
     * @param parameterTypes
     *            方法参数类型，方法没有参数请传null
     * @param args
     *            参数
     * @param <R>
     *            结果类型
     * @return 调用结果
     */
    public static <R> R invoke(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) {
        Assert.isFalse(CollectionUtil.safeIsEmpty(parameterTypes) ^ CollectionUtil.safeIsEmpty(args),
            "方法参数类型列表必须和方法参数列表一致");
        Assert.isTrue(parameterTypes == null || parameterTypes.length == args.length, "方法参数类型列表长度必须和方法参数列表长度一致");
        Method method;
        if (obj instanceof Class) {
            method = getMethod((Class)obj, methodName, parameterTypes);
        } else {
            method = getMethod(obj.getClass(), methodName, parameterTypes);
        }
        return invoke(obj, method, args);
    }

    /**
     * 调用指定对象的指定方法
     * 
     * @param obj
     *            指定对象，不能为空，如果要调用的方法为静态方法那么传入Class对象
     * @param method
     *            要调用的方法
     * @param args
     *            参数
     * @param <R>
     *            结果类型
     * @return 调用结果
     */
    @SuppressWarnings("unchecked")
    public static <R> R invoke(Object obj, Method method, Object... args) {
        try {
            if (obj instanceof Class) {
                return (R)method.invoke(null, args);
            } else {
                return (R)method.invoke(obj, args);
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new ReflectException("调用方法[" + method + "]失败", e);
        } catch (InvocationTargetException e) {
            throw new ReflectException("调用方法[" + method + "]失败", e.getTargetException());
        }
    }

    /**
     * 获取指定类型和其父类型、接口中声明的所有方法（除了Object中声明的方法），如果对于方法M，类A中对方法M实现了M1，类B中 对方法M实现了M2，类A继承了类B，传入参数为类A，那么返回的列表中将包含M1而不包含M2
     * 
     * @param clazz
     *            指定类型
     * @return 指定类型和其父类型、接口中声明的所有方法（除了Object中声明的方法）
     */
    public static List<Method> getAllMethod(Class<?> clazz) {
        return ALL_METHOD_CACHE.compute(clazz, (key, value) -> {
            if (value == null) {
                List<Method> methods = new ArrayList<>();
                Map<String, Method> methodMap = getAllMethod(key, new HashMap<>());

                methodMap.forEach((k, v) -> methods.add(v));
                return methods;
            } else {
                return value;
            }
        });
    }

    /**
     * 获取指定类的所有方法（不包含接口方法）
     * 
     * @param clazz
     *            Class类型
     * @param methods
     *            方法集合
     * @return 方法集合
     */
    private static Map<String, Method> getAllMethod(Class<?> clazz, Map<String, Method> methods) {
        if (clazz == null || clazz == Object.class) {
            return methods;
        }

        Arrays.stream(clazz.getDeclaredMethods()).forEach(method -> {
            String key = String.format("%s:%s", method.getName(), ByteCodeUtils.getMethodDesc(method));
            Method m = methods.get(key);
            if (m == null || (m.getDeclaringClass().isInterface() && !method.getDeclaringClass().isInterface())) {
                methods.put(key, method);
            }
        });

        Arrays.stream(clazz.getInterfaces()).forEach(c -> getAllMethod(c, methods));

        return getAllMethod(clazz.getSuperclass(), methods);
    }

    /**
     * 过滤方法，选出最靠近声明类的方法，例如对于方法M，类A中对方法M实现了M1，类A继承了类B，类B中对方法M实现了M2，使用类A作 为declareClass，集合中包含M1和M2，那么最终将返回M1而不是M2
     * 
     * @param methods
     *            方法集合
     * @param declareClass
     *            声明类（最底层的子类）
     * @return 方法集合中最接近声明类的方法
     */
    public static Method filter(List<Method> methods, Class<?> declareClass) {
        if (declareClass == null) {
            return null;
        }
        Optional<Method> optional =
            methods.stream().filter(method -> method.getDeclaringClass() == declareClass).limit(1).findFirst();
        return optional.orElse(filter(methods, declareClass.getSuperclass()));
    }

    /**
     * 获取指定类型中指定的方法（无法获取本类未覆写过的父类方法）
     * 
     * @param clazz
     *            类型
     * @param methodName
     *            方法名
     * @param parameterTypes
     *            方法参数类型
     * @throws ReflectException
     *             指定方法获取不到时会抛出异常
     * @return 指定方法，获取不到时会抛出异常
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes)
        throws ReflectException {
        Assert.notNull(clazz, "类型不能为空");
        Assert.notNull(methodName, "方法名不能为空");

        return METHOD_CACHE.compute(new MethodKey(methodName, clazz, parameterTypes), (k, v) -> {
            if (v == null) {
                try {
                    return allowAccess(clazz.getDeclaredMethod(methodName, parameterTypes));
                } catch (NoSuchMethodException e) {
                    log.error(StringFormater.simpleFormat("类[{0}]中不存在方法名为[{1}]、方法列表为[{2}]的方法", clazz, methodName,
                        parameterTypes == null ? "null" : Arrays.toString(parameterTypes)));
                    throw new ReflectException(StringFormater.simpleFormat("类[{0}]中不存在方法名为[{1}]、方法列表为[{2}]的方法", clazz,
                        methodName, parameterTypes == null ? "null" : Arrays.toString(parameterTypes)), e);
                }
            } else {
                return v;
            }
        });
    }

    /**
     * 执行方法
     * 
     * @param method
     *            要调用的方法
     * @param target
     *            方法所在Class的实例，对于静态方法要传入null或者Class对象
     * @param params
     *            调用方法的参数
     * @param <T>
     *            方法返回类型
     * @return 方法调用结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T execMethod(Method method, Object target, Object... params) {
        allowAccess(method);
        try {
            return (T)method.invoke(target, params);
        } catch (Throwable e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 获取指定包中所有带指定注解的类
     *
     * @param annotation
     *            注解
     * @param packages
     *            包集合
     * @return 包集合中所有带指定注解的类
     */
    public static List<Class<?>> getAllAnnotationPresentClass(Class<? extends Annotation> annotation,
        String... packages) {
        if (packages == null || packages.length == 0) {
            return Collections.emptyList();
        }

        log.debug("开始扫描包{}下的带注解[{}]的列表", packages, annotation);
        List<Class<?>> classes = CLASS_SCANNER
            .scanByFilter(Collections.singletonList(clazz -> !clazz.isAnnotationPresent(annotation)), packages);
        log.debug("包{}下的带注解{}的列表为：[{}]", packages, annotation, classes);
        return classes;
    }

    /**
     * 获取指定类型内所有带有指定注解的方法的集合（包含父类里的方法，如果父类中的方法带有指定注解，但是子类覆写后没有那么该方法不会被添加）
     *
     * @param type
     *            指定类型
     * @param annotation
     *            指定注解
     * @return 带有指定注解的方法集合
     */
    public static List<Method> getAllAnnotationPresentMethod(Class<?> type, Class<? extends Annotation> annotation) {
        List<Method> methods = getAllMethod(type);

        if (methods.isEmpty()) {
            return Collections.emptyList();
        }

        return methods.stream().filter(method -> method.isAnnotationPresent(annotation)).collect(Collectors.toList());
    }

    /**
     * 获取指定对象中指定字段名对应的字段的值
     *
     * @param obj
     *            对象，如果要获取的字段是静态字段那么需要传入Class
     * @param fieldName
     *            字段名
     * @param <T>
     *            字段类型
     * @return 指定对象中指定字段名对应字段的值，字段不存在时返回null而不是抛异常
     */
    public static <T> T getFieldValue(Object obj, String fieldName) {
        Field field = getField(obj, fieldName, true);
        if (field == null) {
            return null;
        }
        return getFieldValue(obj, field);
    }

    /**
     * 获取指定对象中指定字段对应的字段的值
     * 
     * @param obj
     *            对象，不能为空
     * @param field
     *            字段
     * @param <T>
     *            字段类型
     * @return 字段值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object obj, Field field) {
        Assert.notNull(obj, "obj不能为空");
        Assert.notNull(field, "field不能为空");

        try {
            return (T)field.get(obj);
        } catch (IllegalArgumentException e) {
            throw new ReflectException(e);
        } catch (IllegalAccessException e) {
            String msg = StringFormater.simpleFormat("类型[{0}]的字段[{1}]不允许访问", obj.getClass(), field.getName());
            log.error(msg);
            throw new ReflectException(msg, e);
        }
    }

    /**
     * 设置指定对象指定对象名的字段值
     * 
     * @param obj
     *            对象，如果要设置的字段是静态字段那么请传入静态字段所在的Class对象
     * @param fieldName
     *            字段名
     * @param fieldValue
     *            字段值
     * @param <T>
     *            字段值的类型
     * @return fieldValue参数原样返回（无论当前字段是否有值都会返回调用本方法传入的fieldValue）
     */
    public static <T> T setFieldValue(Object obj, String fieldName, T fieldValue) {
        Field field = getField(obj, fieldName);

        return setFieldValue(obj, field, fieldValue);
    }

    /**
     * 设置field值
     * 
     * @param obj
     *            对象
     * @param field
     *            对象的字段
     * @param fieldValue
     *            字段值
     * @param <T>
     *            字段值泛型
     * @return 字段值
     */
    public static <T> T setFieldValue(Object obj, Field field, T fieldValue) {
        Assert.notNull(obj, "obj不能为空");
        Assert.notNull(field, "field不能为空");

        try {
            field.set(obj, fieldValue);
        } catch (IllegalAccessException e) {
            String msg = StringFormater.simpleFormat("类型[{0}]的字段[{1}]不允许设置", obj.getClass(), field.getName());
            log.error(msg);
            throw new ReflectException(msg, e);
        }
        return fieldValue;
    }

    /**
     * 从指定Class中获取指定Field，并尝试将其accessible属性设置为true（并不能获取到父类声明的字段）
     * 
     * @param obj
     *            字段所属的对象或者class
     * @param fieldName
     *            fieldName
     * @return Field，不会为null，只会抛出异常
     * @throws ReflectException
     *             字段不存在时抛出异常
     */
    public static Field getField(Object obj, String fieldName) throws ReflectException {
        Field field = getField(obj, fieldName, false);
        if (field == null) {
            throw new ReflectException(StringFormater.simpleFormat("[{0}]中不存在字段[{1}]", obj, fieldName));
        }
        return field;
    }

    /**
     * 从指定Class中获取指定Field，并尝试将其accessible属性设置为true
     * 
     * @param obj
     *            字段所属的对象或者class
     * @param fieldName
     *            字段名
     * @param isRecursive
     *            是否递归获取父类中的字段，为true时表示当前类查找不到指定字段时允许递归从父类获取
     * @return 要获取的Field，不存在时返回null
     */
    public static Field getField(Object obj, String fieldName, boolean isRecursive) {
        Assert.notNull(obj, "obj不能为空");
        Assert.notNull(fieldName, "fieldName不能为空");

        Class<?> clazz;
        if (obj instanceof Class) {
            clazz = (Class<?>)obj;
        } else {
            clazz = obj.getClass();
        }

        return FIELD_CACHE.compute(new FieldKey(fieldName, clazz), (k, v) -> {
            if (v == null) {
                try {
                    return allowAccess(clazz.getDeclaredField(fieldName));
                } catch (NoSuchFieldException e) {
                    Class<?> superClass = clazz.getSuperclass();
                    // 判断父类是否是Object
                    if (superClass.equals(Object.class) || !isRecursive) {
                        return null;
                    } else {
                        return getField(superClass, fieldName);
                    }
                }
            } else {
                return v;
            }
        });
    }

    /**
     * 获取指定Class的所有field（包含父类声明的字段）
     * 
     * @param clazz
     *            Class
     * @return 所有field数组
     */
    public static Field[] getAllFields(Class<?> clazz) {
        Assert.notNull(clazz, "clazz不能为空");
        return ALL_FIELD_CACHE.compute(clazz, (k, v) -> {
            if (v == null) {
                List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));

                // 查找是否存在父类，如果存在且不是Object那么查找父类的field
                Class<?> superClass = clazz.getSuperclass();

                // 遍历设置访问权限，同时加入单个field的缓存
                fields.stream().map(ReflectUtil::allowAccess)
                    .forEach(f -> FIELD_CACHE.compute(new FieldKey(f.getName(), clazz), (fk, fv) -> {
                        if (fv == null) {
                            return f;
                        } else {
                            return fv;
                        }
                    }));

                if (superClass != null && superClass != Object.class) {
                    fields.addAll(Arrays.asList(getAllFields(superClass)));
                }
                return fields.toArray(new Field[0]);
            } else {
                return v;
            }
        });
    }

    /**
     * 更改AccessibleObject的访问权限
     * 
     * @param object
     *            AccessibleObject
     * @param <T>
     *            AccessibleObject的具体类型
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

}
