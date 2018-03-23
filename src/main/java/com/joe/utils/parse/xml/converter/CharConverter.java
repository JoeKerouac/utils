package com.joe.utils.parse.xml.converter;

import com.joe.utils.common.StringUtils;
import org.dom4j.Element;

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
