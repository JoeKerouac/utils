package com.joe.utils.serialize.xml.converter;

import org.dom4j.Element;

import com.joe.utils.common.string.StringUtils;

/**
 * float转换器
 *
 * @author joe
 * @version 2018.01.30 14:34
 */
public class FloatConverter extends AbstractXmlTypeConvert<Float> {
    @Override
    public Float read(Element element, String attrName) {
        String data = StringUtils.isEmpty(attrName) ? element.getText() : element.attributeValue(attrName);
        if (StringUtils.isEmpty(data)) {
            return 0.0F;
        } else {
            return Float.valueOf(data);
        }
    }
}
