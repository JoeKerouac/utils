package com.joe.utils.common;

import com.joe.utils.collection.LRUCacheMap;
import com.joe.utils.type.JavaTypeUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Bean常用操作
 *
 * @author joe
 */
public class BeanUtils {
    private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);
    private static final LRUCacheMap<Class<?>, CustomPropertyDescriptor[]> cache = new LRUCacheMap<>();
    private static final LRUCacheMap<Class<?>, Field[]> fieldCache = new LRUCacheMap<>();
    private static final LRUCacheMap<FieldCache, CustomPropertyDescriptor> fieldDescriporCache = new LRUCacheMap<>();

    /**
     * 为对象的属性注入指定值
     *
     * @param obj      指定对象
     * @param propName 属性名称
     * @param value    要注入的属性值
     * @return 如果注入成功则返回<code>true</code>
     */
    public static boolean setProperty(Object obj, String propName, Object value) {
        logger.debug("开始为{}的{}字段写入值{}", obj, propName, value);
        if (obj == null || StringUtils.isEmpty(propName)) {
            logger.warn("写入数值失败，参数存在空值");
            return false;
        }
        Class<?> clazz = obj.getClass();
        try {
            Field field = getField(clazz, propName);
            CustomPropertyDescriptor propertyDescriptor = buildDescriptor(field, clazz);
            if (propertyDescriptor == null) {
                return false;
            }
            // 调用反射复制
            propertyDescriptor.getWriteMethod().invoke(obj, value);
            logger.debug("写入成功");
            return true;
        } catch (Exception e) {
            logger.error("写入数值失败，写入过程中发生异常", e);
            return false;
        }
    }

    /**
     * 获取对象指定字段的值
     *
     * @param obj      指定对象
     * @param propName 要获取的字段的名称
     * @return 该字段的值，发生异常时返回null（注意：不能以null来判断是否发生异常，因为当该字段的值也为null时结果也是null，同时当传进来的参数为null时也返回null）
     */
    public static Object getProperty(Object obj, String propName) {
        logger.debug("开始获取{}的{}字段的值", obj, propName);
        if (obj == null || StringUtils.isEmpty(propName)) {
            logger.warn("获取字段值失败，参数存在空值");
            return null;
        }
        Class<?> clazz = obj.getClass();
        try {
            Field field = getField(clazz, propName);
            CustomPropertyDescriptor propertyDescriptor = buildDescriptor(field, clazz);
            if (propertyDescriptor == null) {
                return false;
            }
            // 调用反射复制
            Object result = propertyDescriptor.getReadMethod().invoke(obj);
            logger.debug("获取{}的{}字段值成功，获取到的值为：{}", obj, propName, result);
            return result;
        } catch (Exception e) {
            logger.error("获取{}的{}字段值失败", obj, propName, e);
            return null;
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
        logger.debug("生成{}的实例", targetClassName);
        try {
            // 没有权限访问该类或者该类（为接口、抽象类）不能实例化时将抛出异常
            target = targetClass.newInstance();
        } catch (Exception e) {
            logger.error("target生成失败，请检查代码；失败原因：", e);
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
        logger.debug("开始获取{}的字段说明", sourceClass.getName());

        CustomPropertyDescriptor[] descriptors = getPropertyDescriptors(sourceClass);
        if (descriptors.length == 0) {
            logger.debug("源{}中不存在已经声明的字段", source);
            return dest;
        }

        String targetClassName = destClass.getName();
        for (CustomPropertyDescriptor descriptor : descriptors) {
            String name = descriptor.getName();
            try {
                Field field = descriptor.getField();
                // NoSuchFieldException, SecurityException
                CustomPropertyDescriptor propertyDescriptor = buildDescriptor(field, destClass);
                // 调用反射复制
                propertyDescriptor.getWriteMethod().invoke(dest, descriptor.getReadMethod().invoke(source));
                logger.info("copy {}.{} to {}.{}", source.getClass().getName(), name, targetClassName, name);
            } catch (Exception e) {
                logger.warn("copy中复制{}时发生错误，忽略该字段", name, e);
                continue;
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
        List<E> list = new ArrayList<E>(sourceList.size());

        if (!(sourceList instanceof ArrayList)) {
            sourceList = new ArrayList<S>(sourceList);
        }

        for (int i = 0; i < sourceList.size(); i++) {
            S source = sourceList.get(i);
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
            logger.debug("获取Class {} 的说明时发现缓存中有，取出缓存中的说明返回", clazz);
            return descriptors;
        }

        Field[] fields = getAllFields(clazz);

        descriptors = new CustomPropertyDescriptor[fields.length];
        if (fields.length == 0) {
            logger.debug("源{}中不存在已经声明的字段", clazz.getName());
            cache.put(clazz, descriptors);
            return descriptors;
        }

        int j = 0;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            CustomPropertyDescriptor descriptor = buildDescriptor(field, clazz);
            if (descriptor != null) {
                descriptors[j++] = descriptor;
            }
        }

        if (j < descriptors.length) {
            logger.debug("构建构成中发生了异常，数组中有null，除去null");
            CustomPropertyDescriptor[] propertyDescriptor = new CustomPropertyDescriptor[j];
            System.arraycopy(descriptors, 0, propertyDescriptor, 0, j);
            cache.put(clazz, propertyDescriptor);
            return propertyDescriptor;
        } else {
            logger.debug("构建没有异常，构建成功");
            cache.put(clazz, descriptors);
            return descriptors;
        }

    }

    /**
     * 判断字段是否是final
     *
     * @param field 要判断的字段
     * @return 返回true表示字段是final类型
     */
    public static boolean isFinal(Field field) {
        // getModifiers获取出来的各个修饰符的值（多个修饰符时需要相加）：
        // public: 1 0
        // private: 2 1
        // protected: 4 2
        // static: 8 3
        // final: 16 4
        // synchronized: 32 5
        // volatile: 64 6
        // transient: 128 7
        // native: 256 8
        // interface: 512 9
        // abstract: 1024 10
        // strictfp: 2048 11
        int modifier = field.getModifiers();
        return Modifier.isFinal(modifier);
    }

    /**
     * 获取指定类的所有字段（包含父类）
     *
     * @param clazz 指定类
     * @return 指定类的所有字段
     */
    public final static Field[] getAllFields(Class<?> clazz) {
        Field[] result = fieldCache.get(clazz);
        if (result != null) {
            return result;
        }
        logger.debug("获取类{}的字段", clazz);
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != null) {
            fields.addAll(Arrays.asList(getAllFields(clazz.getSuperclass())));
        }
        logger.debug("类{}的字段为：{}", clazz, fields);
        result = fields.toArray(new Field[fields.size()]);
        fieldCache.put(clazz, result);
        return result;
    }

    /**
     * 根据字段名获取指定类的指定字段
     *
     * @param clazz 指定类
     * @param name  字段名
     * @return 根据字段名获取到的指定类的指定字段，不存在时返回null
     */
    public final static Field getField(Class<?> clazz, String name) {
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
            if (isFinal(field)) {
                logger.debug("字段{}是final类型，尝试为该字段创建说明", name);
                customPropertyDescriptor = tryBuildFinal(field, clazz);
            } else {
                logger.debug("字段不是final类型，开始构建字段{}的说明", name);
                customPropertyDescriptor = convert(field, new PropertyDescriptor(name, clazz), clazz);
            }
        } catch (IntrospectionException e) {
            // 构建异常
            logger.warn("说明构建失败，忽略{}字段", field.getName(), e);
        }

        fieldDescriporCache.put(fieldCache, customPropertyDescriptor);
        return customPropertyDescriptor;
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
        logger.debug("尝试为final类型的字段{}创建字段说明", name);

        if (Boolean.class.isAssignableFrom(field.getType())) {
            logger.debug("字段是boolean类型");
            if (name.startsWith("is")) {
                readMethodName = name;
            } else {
                readMethodName = "is" + StringUtils.toFirstUpperCase(name);
            }
        } else {
            logger.debug("字段不是boolean类型");
            readMethodName = "get" + StringUtils.toFirstUpperCase(name);
        }
        logger.debug("猜测final类型的字段{}的read方法名为{}", name, readMethodName);
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
        Class<?> typeClass = field.getType();
        String typeName = typeClass.getName();
        //判断是否是基本数据类型
        if (JavaTypeUtil.isGeneralType(typeClass)) {
            logger.debug("类型[{}]是基本数据类型", typeClass);
            typeClass = JavaTypeUtil.getGeneralTypeByName(typeName);
        }
        return new CustomPropertyDescriptor(descriptor.getName(), descriptor.getReadMethod(),
                descriptor.getWriteMethod(), clazz, field, typeClass);
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
        //字段的类型（如果是基本类型如int、byte之类的那么使用自定义的封装类型来和Integer、Byte等区分）
        private Class<?> type;

        public CustomPropertyDescriptor(String name, Method readMethod, Method writeMethod, Class<?> clazz,
                                        Field field, Class<?> type) {
            this.name = name;
            this.readMethod = readMethod;
            this.writeMethod = writeMethod;
            this.clazz = clazz;
            this.field = field;
            this.type = type;
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
        public boolean isBasic() {
            return JavaTypeUtil.isInternalBasicType(type);
        }

        /**
         * 判断字段是否是八大基本类型的封装类型
         *
         * @return 返回true表示该字段是八大基本类型的封装类型
         */
        public boolean isBasicObject() {
            return JavaTypeUtil.isBasicObject(type);
        }

        /**
         * 获取字段的真实类型
         *
         * @return 字段的真实类型
         */
        public Class<?> getRealType() {
            return field.getType();
        }

        /**
         * 获取字段的类型名
         *
         * @return 字段的类型名，如果字段是基本类型那么将会返回int、long等而不是java.util.String这种格式的类型名
         */
        public String getTypeName() {
            return field.getType().getName();
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
