package com.joe.utils.serialize.xml.converter;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joe.utils.common.string.StringUtils;
import com.joe.utils.reflect.type.JavaTypeUtil;
import com.joe.utils.serialize.xml.XmlParser;
import com.joe.utils.serialize.xml.XmlTypeConvert;

/**
 * xml解析器，只需要实现该类并且实现{@link #resolve()}方法即可解析pojo类型的字段
 *
 * @author joe
 * @version 2018.02.01 10:12
 */
public interface XmlConverter<T> extends XmlTypeConvert<T> {
    Logger    logger = LoggerFactory.getLogger(XmlConverter.class);
    XmlParser PARSER = XmlParser.buildInstance();

    @SuppressWarnings("unchecked")
    @Override
    default T read(Element element, String attrName) {
        String data = StringUtils.isEmpty(attrName) ? element.asXML()
            : element.attributeValue(attrName);
        Class<T> clazz = resolve();
        if (String.class.equals(clazz)) {
            logger.info("xml转换器确定的字段类型为String，转到String转换器");
            return (T) data;
        } else if (JavaTypeUtil.isBasic(clazz) || JavaTypeUtil.isGeneralType(clazz)) {
            logger.info("xml转换器确定的字段类型为" + clazz.getName() + "，转到基本类型转换器");
            return (T) XmlTypeConverterUtil.converters.get(clazz.getName()).read(element, attrName);
        }
        return PARSER.parse(data, clazz);
    }
}
