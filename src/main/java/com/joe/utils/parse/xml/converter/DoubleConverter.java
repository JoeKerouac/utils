package com.joe.utils.parse.xml.converter;

import org.dom4j.Element;

import com.joe.utils.common.StringUtils;

/**
 * Double转换器
 *
 * @author joe
 * @version 2018.01.30 14:34
 */
public class DoubleConverter extends AbstractXmlTypeConvert<Double> {
    @Override
    public Double read(Element element, String attrName) {
        String data = StringUtils.isEmpty(attrName) ? element.getText()
            : element.attributeValue(attrName);
        if (StringUtils.isEmpty(data)) {
            return 0.0;
        } else {
            return Double.valueOf(data);
        }
    }
}
