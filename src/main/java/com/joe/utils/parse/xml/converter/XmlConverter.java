package com.joe.utils.parse.xml.converter;

import com.joe.utils.common.StringUtils;
import com.joe.utils.parse.xml.XmlParser;
import com.joe.utils.parse.xml.XmlTypeConvert;
import com.joe.utils.type.JavaTypeUtil;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * xml解析器，只需要实现该类并且实现{@link #resolve()}方法即可解析pojo类型的字段
 *
 * @author joe
 * @version 2018.02.01 10:12
 */
public interface XmlConverter<T> extends XmlTypeConvert<T> {
    Logger logger = LoggerFactory.getLogger(XmlConverter.class);
    XmlParser PARSER = XmlParser.getInstance();

    @Override
    default T read(Element element, String attrName) {
        String data = StringUtils.isEmpty(attrName) ? element.asXML() : element.attributeValue(attrName);
        if (String.class.equals(resolve())) {
            logger.info("xml转换器确定的字段类型为String，转到String转换器");
            return (T) data;
        } else if (JavaTypeUtil.isBasicObject(resolve()) || JavaTypeUtil.isInternalBasicType(resolve())) {
            logger.info("xml转换器确定的字段类型为" + resolve().getName() + "，转到基本类型转换器");
            return (T) XmlTypeConverterUtil.converters.get(resolve().getName()).read(element, attrName);
        }
        return PARSER.parse(data, resolve());
    }
}
