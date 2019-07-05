package com.joe.utils.serialize.xml.converter;

import org.dom4j.Element;

import com.joe.utils.common.string.StringUtils;

/**
 * int转换器
 *
 * @author joe
 * @version 2018.01.30 14:30
 */
public class IntConverter extends AbstractXmlTypeConvert<Integer> {
    @Override
    public Integer read(Element element, String attrName) {
        String data = StringUtils.isEmpty(attrName) ? element.getText()
            : element.attributeValue(attrName);
        if (StringUtils.isEmpty(data)) {
            return 0;
        } else {
            return Integer.valueOf(data);
        }
    }
}
