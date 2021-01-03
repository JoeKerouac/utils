package com.joe.utils.serialize.xml.converter;

import org.dom4j.Element;

import com.joe.utils.common.string.StringUtils;

/**
 * char转换器
 *
 * @author joe
 * @version 2018.01.30 14:34
 */
public class CharConverter extends AbstractXmlTypeConvert<Character> {
    @Override
    public Character read(Element element, String attrName) {
        String data = StringUtils.isEmpty(attrName) ? element.getText() : element.attributeValue(attrName);
        if (StringUtils.isEmpty(data)) {
            return null;
        } else {
            return data.toCharArray()[0];
        }
    }
}
