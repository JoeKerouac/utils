package com.joe.utils.serialize.xml.converter;

import org.dom4j.Element;

import com.joe.utils.common.string.StringUtils;

/**
 * long转换器
 *
 * @author joe
 * @version 2018.01.30 14:33
 */
public class LongConverter extends AbstractXmlTypeConvert<Long> {
    @Override
    public Long read(Element element, String attrName) {
        String data = StringUtils.isEmpty(attrName) ? element.getText() : element.attributeValue(attrName);
        if (StringUtils.isEmpty(data)) {
            return 0L;
        } else {
            return Long.valueOf(data);
        }
    }
}
