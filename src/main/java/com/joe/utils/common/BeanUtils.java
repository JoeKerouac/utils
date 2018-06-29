package com.joe.utils.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.joe.utils.collection.LRUCacheMap;
import com.joe.utils.common.exception.BeanException;
import com.joe.utils.parse.xml.XmlNode;
import com.joe.utils.type.ReflectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Bean常用操作
 *
 * @author joe
 */
@Slf4j
public class BeanUtils {
    private static final LRUCacheMap<Class<?>, CustomPropertyDescriptor[]> cache = new LRUCacheMap<>();
    private static final LRUCacheMap<Class<?>, Field[]> fieldCache = new LRUCacheMap<>();
    private static final LRUCacheMap<FieldCache, CustomPropertyDescriptor> fieldDescriporCache = new LRUCacheMap<>();

    /**
     * 将pojo的所有字段映射为map，默认包含null值
     *
     * @param pojo           pojo
     * @param annotationType 别名注解类型，支持{@link JsonProperty JsonProperty}和{@link XmlNode XmlNode}
     * @return map，当pojo为null时返回空map
     */
    public static Map<String, T> convert(Object pojo, Class<? extends Annotation> annotationType) {
        return convert(pojo, annotationType, true);
    }

    /**
     * 将pojo的所有字段映射为map
     *
     * @param pojo           pojo
     * @param annotationType 别名注解类型，支持{@link JsonProperty JsonProperty}和{@link XmlNode XmlNode}
     * @param hasNull        是否包含null值，true表示包含
     * @return map，当pojo为null时返回空map
     */
    public static <T> Map<String, T> convert(Object pojo, Class<? extends Annotation> annotationType, boolean
            hasNull) {
        log.debug("获取[{}]的字段映射，使用注解[{}]的值作为别名", pojo, annotationType);
        if (pojo == null) {
            return Collections.emptyMap();
        }

        if (annotationType != null && !JsonProperty.class.isAssignableFrom(annotationType) && !XmlNode.class
                .isAssignableFrom(annotationType)) {
            throw new IllegalArgumentException("不支持的注解类型：" + annotationType);
        }

        Field[] fields = getAllFields(pojo.getClass());
        Map<String, T> map = new HashMap<>();
        if (fields.length == 0) {
            return Collections.emptyMap();
        }
        for (Field field : fields) {
            log.debug("获取字段[{}]的值", field);
            JsonProperty jsonProperty = field.getDeclaredAnnotation(JsonProperty.class);
            XmlNode xmlNode = field.getDeclaredAnnotation(XmlNode.class);

            String name;
            if (annotationType == null) {
                name = getName(jsonProperty, xmlNode, field);
            } else if (JsonProperty.class.isAssignableFrom(annotationType)) {
                name = getName(jsonProperty, null, field);
            } else if (XmlNode.class.isAssignableFrom(annotationType)) {
                name = getName(null, xmlNode, field);
            } else {
                throw new IllegalArgumentException("不支持的注解类型：" + annotationType);
            }

            T value = getProperty(pojo, field.getName());
            if (value == null && !hasNull) {
                log.debug("字段[{}]值为null，当前不包含null值，忽略字段[{}]", name, name);
                continue;
            }
            map.put(name, value);
        }
        log.debug("获取[{}]的字段映射map为[{}]", pojo, map);
        return map;
    }

    /**
     * 为对象的属性注入指定值
     *
     * @param obj      指定对象
     * @param propName 属性名称
     * @param value    要注入的属性值
     * @return 如果注入成功则返回<code>true</code>
     */
    public static boolean setProperty(Object obj, String propName, Object value) {
        log.debug("开始为{}的{}字段写入值{}", obj, propName, value);
        if (obj == null || StringUtils.isEmpty(propName)) {
            log.warn("写入数值失败，参数存在空值");
            return false;
        }
        Class<?> clazz = obj.getClass();
        Field field = getField(clazz, propName);
        try {
            field.set(obj, value);
            log.debug("写入成功");
            return true;
        } catch (Exception e) {
            log.error("写入数值失败，写入过程中发生异常", e);
            return false;
        }
    }

    /**
     * 获取对象指定字段的值
     *
     * @param obj      指定对象
     * @param propName 要获取的字段的名称
     * @param <T>      字段的类型
     * @return 该字段的值
     */

    public static <T> T getProperty(Object obj, String propName) {
        log.debug("开始获取{}的{}字段的值", obj, propName);
        if (obj == null || StringUtils.isEmpty(propName)) {
            log.warn("获取字段值失败，参数存在空值");
            return null;
        }
        Field field = getField(obj.getClass(), propName);
        try {
            log.debug("成功获取到[{}]的[{}]字段", obj.getClass(), propName);
            T value = (T) field.get(obj);
            return value;
        } catch (Exception e) {
            log.error("获取{}的{}字段值失败", obj, propName, e);
            throw new BeanException("获取[" + obj.getClass() + "]的字段[" + propName + "]的值失败", e);
        }
    }

    /**
     * 将source中与targetClass同名的字段从source中复制到targetClass的实例中
     *
     * @param source      被复制的源对象
     * @param targetClass 要复制的目标对象的class对象
     * @param <E>         目标对象的实际类型
     * @return targetClass的实例，当targetClass或者source的class为接口、抽象类或者不是public时返回null
     */
    public static <E> E copy(Object source, Class<E> targetClass) {
        if (source == null || targetClass == null) {
            return null;
        }
        E target;
        String targetClassName = targetClass.getName();
        log.debug("生成{}的实例", targetClassName);
        try {
            // 没有权限访问该类或者该类（为接口、抽象类）不能实例化时将抛出异常
            target = targetClass.newInstance();
        } catch (Exception e) {
            log.error("target生成失败，请检查代码；失败原因：", e);
            return null;
        }

        return copy(target, source);
    }

    /**
     * 将sources中所有与dest同名的字段的值复制到dest中，如果dest中包含字段A，同时sources中多个对象都包含字段A，那么将
     * 以sources中最后一个包含字段A的对象的值为准
     *
     * @param dest    目标
     * @param sources 源
     * @param <E>     目标对象的实际类型
     * @return 复制后的目标对象
     */
    public static <E> E copy(E dest, Object... sources) {
        if (dest == null || sources == null || sources.length == 0) {
            return dest;
        }
        for (Object obj : sources) {
            if (dest == obj) {
                continue;
            }
            copy(dest, obj);
        }
        return dest;
    }

    /**
     * 将source中与dest的同名字段的值复制到dest中
     *
     * @param dest   目标
     * @param source 源
     * @param <E>    目标对象的实际类型
     * @return 复制后的目标对象
     */
    public static <E> E copy(E dest, Object source) {
        if (dest == null || source == null) {
            return dest;
        }
        if (dest == source) {
            return dest;
        }
        @SuppressWarnings("unchecked")
        Class<E> destClass = (Class<E>) dest.getClass();

        Class<?> sourceClass = source.getClass();
        String sourceName = sourceClass.getName();
        log.debug("开始获取{}的字段说明", sourceName);

        CustomPropertyDescriptor[] descriptors = getPropertyDescriptors(sourceClass);
        if (descriptors.length == 0) {
            log.debug("源{}中不存在已经声明的字段", sourceName);
            return dest;
        }

        String targetClassName = destClass.getName();
        for (CustomPropertyDescriptor descriptor : descriptors) {
            String name = descriptor.getName();
            try {
                Field field = descriptor.getField();
                // NoSuchFieldException, SecurityException
                CustomPropertyDescriptor propertyDescriptor = buildDescriptor(field, destClass);
                if (propertyDescriptor == null) {
                    log.debug("目标{}中不存在已经声明的字段[{}]", targetClassName, field.getName());
                    continue;
                }

                if (propertyDescriptor.getWriteMethod() == null) {
                    log.debug("目标{}中不存在字段[{}]的write方法", targetClassName, field.getName());
                } else if (descriptor.getReadMethod() == null) {
                    log.debug("源{}中不存在字段[{}]的read方法", sourceName, field.getName());
                } else {
                    // 调用反射复制
                    propertyDescriptor.getWriteMethod().invoke(dest, descriptor.getReadMethod().invoke(source));
                    log.info("copy {}.{} to {}.{}", source.getClass().getName(), name, targetClassName, name);
                }
            } catch (Exception e) {
                log.warn("copy中复制{}时发生错误，忽略该字段", name, e);
            }
        }
        return dest;
    }

    /**
     * 将sourceList中的对象与targetClass同名的字段从source中复制到targetClass的实例中，使用前请对参数进行非空校验
     *
     * @param sourceList  被复制的源对象的数组
     * @param targetClass 要复制的目标对象的class对象
     * @param <S>         数组中数据的实际类型
     * @param <E>         目标对象的实际类型
     * @return targetClass的实例的数组
     */
    public static <E, S> List<E> copy(List<S> sourceList, Class<E> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        List<E> list = new ArrayList<>(sourceList.size());

        if (!(sourceList instanceof ArrayList)) {
            sourceList = new ArrayList<>(sourceList);
        }

        for (S source : sourceList) {
            E e = copy(source, targetClass);
            if (e != null) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * 获取指定Class的字段说明
     *
     * @param clazz 指定的class
     * @return 指定class的字段说明（数组中不会有null）
     * @throws NullPointerException clazz为null时抛出该异常
     */
    public static CustomPropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws NullPointerException {
        if (clazz == null) {
            throw new NullPointerException("clazz为null");
        }
        // 首先从缓存中检查
        CustomPropertyDescriptor[] descriptors = cache.get(clazz);
        if (descriptors != null) {
            log.debug("获取Class {} 的说明时发现缓存中有，取出缓存中的说明返回", clazz);
            return descriptors;
        }

        Field[] fields = getAllFields(clazz);

        descriptors = new CustomPropertyDescriptor[fields.length];
        if (fields.length == 0) {
            log.debug("源{}中不存在已经声明的字段", clazz.getName());
            cache.put(clazz, descriptors);
            return descriptors;
        }

        int j = 0;
        for (Field field : fields) {
            CustomPropertyDescriptor descriptor = buildDescriptor(field, clazz);
            if (descriptor != null) {
                descriptors[j++] = descriptor;
            }
        }

        if (j < descriptors.length) {
            log.debug("构建构成中发生了异常，数组中有null，除去null");
            CustomPropertyDescriptor[] propertyDescriptor = new CustomPropertyDescriptor[j];
            System.arraycopy(descriptors, 0, propertyDescriptor, 0, j);
            cache.put(clazz, propertyDescriptor);
            return propertyDescriptor;
        } else {
            log.debug("构建没有异常，构建成功");
            cache.put(clazz, descriptors);
            return descriptors;
        }
    }

    /**
     * 获取指定类的所有字段（包含父类）
     *
     * @param clazz 指定类
     * @return 指定类的所有字段
     */
    public static Field[] getAllFields(Class<?> clazz) {
        Field[] result = fieldCache.get(clazz);
        if (result != null) {
            return result;
        }
        log.debug("获取类{}的字段", clazz);
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != null) {
            fields.addAll(Arrays.asList(getAllFields(clazz.getSuperclass())));
        }
        log.debug("类{}的字段为：{}", clazz, fields);
        result = fields.toArray(new Field[fields.size()]);
        fieldCache.put(clazz, result);
        for (Field field : result) {
            try {
                field.setAccessible(true);
            } catch (Throwable e) {
            }
        }
        return result;
    }

    /**
     * 根据字段名获取指定类的指定字段（会尝试将字段设置为可访问的）
     *
     * @param clazz 指定类
     * @param name  字段名
     * @return 根据字段名获取到的指定类的指定字段，不存在时返回null
     */
    public static Field getField(Class<?> clazz, String name) {
        Field[] fields = getAllFields(clazz);
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    /**
     * 构建指定字段的说明
     *
     * @param field 字段
     * @param clazz 该字段所属的class
     * @return 该字段的说明，构建异常时返回null
     */
    private static CustomPropertyDescriptor buildDescriptor(Field field, Class<?> clazz) {
        FieldCache fieldCache = new FieldCache(field, clazz);

        // 首先检查缓存
        if (fieldDescriporCache.containsKey(fieldCache)) {
            return fieldDescriporCache.get(fieldCache);
        }

        String name = field.getName();

        CustomPropertyDescriptor customPropertyDescriptor = null;
        try {
            if (ReflectUtil.isFinal(field)) {
                log.debug("字段{}是final类型，尝试为该字段创建说明", name);
                customPropertyDescriptor = tryBuildFinal(field, clazz);
            } else {
                log.debug("字段不是final类型，开始构建字段{}的说明", name);
                customPropertyDescriptor = convert(field, new PropertyDescriptor(name, clazz), clazz);
            }
        } catch (IntrospectionException e) {
            //挣扎一下，尝试自己构建（针对继承方法有效）
            log.info("尝试自定义构建PropertyDescriptor");
            Method readMethod;
            Method writeMethod = null;
            String methodName = StringUtils.toFirstUpperCase(name);
            readMethod = getMethod("get" + methodName, clazz);

            if (readMethod == null) {
                readMethod = getMethod("is" + methodName, clazz);
            }

            if (readMethod != null) {
                writeMethod = getMethod("set" + methodName, clazz, readMethod.getReturnType());
            }

            if (writeMethod == null) {
                log.warn("说明构建失败，忽略{}字段", field.getName(), e);
            } else {
                log.info("自定义构建PropertyDescriptor成功");
                try {
                    customPropertyDescriptor = convert(field, new PropertyDescriptor(name, readMethod, writeMethod),
                            clazz);
                } catch (IntrospectionException e1) {
                    log.info("构建失败，忽略字段[{}]", field.getName(), e1);
                }
            }
        }

        fieldDescriporCache.put(fieldCache, customPropertyDescriptor);
        return customPropertyDescriptor;
    }

    /**
     * 从注解获取字段名字
     *
     * @param jsonProperty json注解
     * @param xmlNode      xml注解
     * @param field        字段
     * @return 字段名
     */
    private static String getName(JsonProperty jsonProperty, XmlNode xmlNode, Field field) {
        String name;
        if (jsonProperty != null) {
            name = jsonProperty.value();
        } else if (xmlNode != null) {
            name = xmlNode.name();
        } else {
            name = field.getName();
        }
        return name;
    }

    /**
     * 获取指定方法
     *
     * @param methodName 方法名
     * @param clazz      Class
     * @param args       方法的参数类型
     * @return 方法名对应的方法，如果获取不到返回null
     */
    private static Method getMethod(String methodName, Class<?> clazz, Class<?>... args) {
        Method method;
        try {
            method = clazz.getMethod(methodName, args);
        } catch (NoSuchMethodException e1) {
            method = null;
        }
        return method;
    }

    /**
     * 尝试为final类型的字段构建说明
     *
     * @param field 字段
     * @param clazz 该字段所属的Class
     * @return final类型字段的说明，该值不会为null，构建失败时会抛出异常
     * @throws IntrospectionException 构建失败抛出异常，不会返回null
     */
    private static CustomPropertyDescriptor tryBuildFinal(Field field, Class<?> clazz) throws IntrospectionException {
        String name = field.getName();
        String readMethodName;
        log.debug("尝试为final类型的字段{}创建字段说明", name);

        if (Boolean.class.isAssignableFrom(field.getType())) {
            log.debug("字段是boolean类型");
            if (name.startsWith("is")) {
                readMethodName = name;
            } else {
                readMethodName = "is" + StringUtils.toFirstUpperCase(name);
            }
        } else {
            log.debug("字段不是boolean类型");
            readMethodName = "get" + StringUtils.toFirstUpperCase(name);
        }
        log.debug("猜测final类型的字段{}的read方法名为{}", name, readMethodName);
        return convert(field, new PropertyDescriptor(name, clazz, readMethodName, null), clazz);
    }

    /**
     * 类型转换，java系统类型转换为自定义字段说明类型
     *
     * @param field      对应的字段
     * @param descriptor 系统字段说明
     * @param clazz      字段所属的class
     * @return 自定义字段说明
     */
    private static CustomPropertyDescriptor convert(Field field, PropertyDescriptor descriptor, Class<?> clazz) {
        if (descriptor == null) {
            return null;
        }
        return new CustomPropertyDescriptor(descriptor.getName(), descriptor.getReadMethod(),
                descriptor.getWriteMethod(), clazz, field);
    }

    private final static class FieldCache {
        private final Field field;
        private final Class<?> clazz;

        /**
         * 构建fieldcache
         *
         * @param field 字段，不能为null
         * @param clazz 字段对应的class，不能为null
         */
        public FieldCache(Field field, Class<?> clazz) {
            if (field == null || clazz == null) {
                throw new NullPointerException("字段和对应的class不能为null");
            }
            this.field = field;
            this.clazz = clazz;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof FieldCache) {
                FieldCache fieldCache = (FieldCache) obj;
                return fieldCache.clazz.equals(this.clazz) && this.field.equals(fieldCache.field);
            }
            return false;
        }

    }

    /**
     * 自定义字段说明
     *
     * @author joe
     */
    @Data
    public final static class CustomPropertyDescriptor {
        // 字段名称
        private final String name;
        // 字段的写方法
        private final Method writeMethod;
        // 字段的读方法
        private final Method readMethod;
        // 字段所属的class（字段所在类的Class，不是字段本身的Class！！）
        private final Class<?> clazz;
        // 字段
        private final Field field;
        //字段的类型
        private Class<?> type;

        public CustomPropertyDescriptor(String name, Method readMethod, Method writeMethod, Class<?> clazz,
                                        Field field) {
            this.name = name;
            this.readMethod = readMethod;
            this.writeMethod = writeMethod;
            this.clazz = clazz;
            this.field = field;
            this.type = field.getType();
            if (readMethod != null)
                readMethod.setAccessible(true);
            if (writeMethod != null)
                writeMethod.setAccessible(true);
        }

        /**
         * 该字段是否是基本类型
         *
         * @return 返回true表示该字段是八大基本类型
         */
        public boolean isGeneralType() {
            return ReflectUtil.isGeneralType(type);
        }

        /**
         * 判断字段是否是八大基本类型的封装类型
         *
         * @return 返回true表示该字段是八大基本类型的封装类型
         */
        public boolean isBasic() {
            return ReflectUtil.isBasic(type);
        }

        /**
         * 获取字段的真实类型
         *
         * @return 字段的真实类型
         */
        public Class<?> getRealType() {
            return type;
        }

        /**
         * 获取字段的类型名
         *
         * @return 字段的类型名，如果字段是基本类型那么将会返回int、long等而不是java.util.String这种格式的类型名
         */
        public String getTypeName() {
            return type.getName();
        }

        /**
         * 获取字段的指定注解
         *
         * @param clazz 注解类型
         * @param <T>   注解类型
         * @return 不存在时返回null，否则返回该注解
         */
        public <T extends Annotation> T getAnnotation(Class<T> clazz) {
            return field.getAnnotation(clazz);
        }
    }
}
