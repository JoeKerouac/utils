package com.joe.utils.parse.xml.converter;

import org.dom4j.Element;

import com.joe.utils.common.StringUtils;

/**
 * String类型转换器
 *
 * @author joe
 * @version 2018.01.30 14:18
 */
public class StringConverter extends AbstractXmlTypeConvert<String> {
    @Override
    public String read(Element element, String attrName) {
        return StringUtils.isEmpty(attrName) ? element.getText() : element.attributeValue(attrName);
    }
}
