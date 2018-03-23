package com.joe.utils.parse.xml.converter;

import com.joe.utils.common.StringUtils;
import org.dom4j.Element;

/**
 * Boolean转换器
 *
 * @author joe
 * @version 2018.01.30 14:25
 */
public class BooleanConverter extends AbstractXmlTypeConvert<Boolean> {
    @Override
    public Boolean read(Element element, String attrName) {
        String data = StringUtils.isEmpty(attrName) ? element.getText() : element.attributeValue(attrName);
        if (StringUtils.isEmpty(data)) {
            return false;
        } else {
            return Boolean.valueOf(data);
        }
    }

}
