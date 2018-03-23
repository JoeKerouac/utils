package com.joe.utils.parse.xml.converter;

import com.joe.utils.common.StringUtils;
import org.dom4j.Element;

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
