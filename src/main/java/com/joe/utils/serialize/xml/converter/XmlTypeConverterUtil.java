package com.joe.utils.serialize.xml.converter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joe.utils.reflect.BeanUtils;
import com.joe.utils.serialize.xml.XmlNode;
import com.joe.utils.serialize.xml.XmlTypeConvert;

/**
 * XmlTypeConverter工具
 *
 * @author joe
 * @version 2018.02.01 10:57
 */
public class XmlTypeConverterUtil {
    private static final Logger logger = LoggerFactory.getLogger(XmlTypeConverterUtil.class);
    public static final Map<String, XmlTypeConvert> converters;
    public static final XmlTypeConvert DEFAULT_CONVERTER = new StringConverter();

    static {
        converters = new HashMap<>();
        converters.put("byte", new ByteConverter());
        converters.put("short", new ShortConverter());
        converters.put("int", new IntConverter());
        converters.put("long", new LongConverter());
        converters.put("double", new DoubleConverter());
        converters.put("float", new FloatConverter());
        converters.put("boolean", new BooleanConverter());
        converters.put("char", new CharConverter());

        converters.put(Byte.class.getName(), new ByteConverter());
        converters.put(Short.class.getName(), new ShortConverter());
        converters.put(Integer.class.getName(), new IntConverter());
        converters.put(Long.class.getName(), new LongConverter());
        converters.put(Double.class.getName(), new DoubleConverter());
        converters.put(Float.class.getName(), new FloatConverter());
        converters.put(Boolean.class.getName(), new BooleanConverter());
        converters.put(Character.class.getName(), new CharConverter());
    }

    /**
     * 确定converter
     *
     * @param attrXmlNode
     *            字段的注释
     * @param filed
     *            字段说明
     * @return 字段对应的converter
     */
    public static XmlTypeConvert resolve(XmlNode attrXmlNode, BeanUtils.CustomPropertyDescriptor filed) {
        XmlTypeConvert convert;
        if (attrXmlNode != null) {
            Class<? extends XmlTypeConvert> fieldConverterClass;
            fieldConverterClass = attrXmlNode.converter();
            // 判断用户是否指定converter
            if (XmlTypeConvert.class.equals(fieldConverterClass)) {
                // 用户没有指定converter
                convert = resolve(filed);
            } else {
                try {
                    convert = fieldConverterClass.newInstance();
                } catch (Exception e) {
                    convert = resolve(filed);
                    logger.warn("指定的xml转换器[{}]无法实例化，请为该转换器增加公共无参数构造器，当前将使用默认转换器[{}]", fieldConverterClass,
                        convert.getClass(), e);
                }
            }
        } else {
            convert = resolve(filed);
        }
        return convert;
    }

    /**
     * 根据字段说明自动推断使用什么转换器
     *
     * @param filed
     *            字段说明
     * @return 根据字段说明推断出来的转换器
     */
    public static XmlTypeConvert resolve(BeanUtils.CustomPropertyDescriptor filed) {
        XmlTypeConvert convert;
        if (filed.isGeneralType() || filed.isBasic()) {
            convert = XmlTypeConverterUtil.converters.get(filed.getTypeName());
        } else if (String.class.equals(filed.getRealType())) {
            convert = DEFAULT_CONVERTER;
        } else if (Collection.class.isAssignableFrom(filed.getRealType())) {
            // 到这里的只有两种可能，一、用户没有指定converter；二、用户没有加注解XmlNode
            XmlNode xmlnode = filed.getAnnotation(XmlNode.class);
            if (xmlnode == null) {
                // 用户没有添加xmlnode注解，使用默认converter
                convert = DEFAULT_CONVERTER;
            } else {
                // 用户指定了xmlnode注解但是没有指定converter，使用general字段确定集合中的数据类型
                convert = (XmlConverter)xmlnode::general;
            }
        } else {
            // 字段不是基本类型，假设是pojo，使用xml转换器
            convert = (XmlConverter)filed::getRealType;
        }
        return convert;
    }

}
