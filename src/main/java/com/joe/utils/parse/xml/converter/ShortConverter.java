package com.joe.utils.parse.xml.converter;

import com.joe.utils.common.StringUtils;
import org.dom4j.Element;

/**
 * short转换器
 *
 * @author joe
 * @version 2018.01.30 14:33
 */
public class ShortConverter extends AbstractXmlTypeConvert<Short> {
    @Override
    public Short read(Element element, String attrName) {
        String data = StringUtils.isEmpty(attrName) ? element.getText() : element.attributeValue(attrName);
        if (StringUtils.isEmpty(data)) {
            return 0;
        } else {
            return Short.valueOf(data);
        }
    }
}
