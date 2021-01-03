package com.joe.utils.serialize.xml.converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joe.utils.serialize.xml.XmlTypeConvert;

/**
 * @author joe
 * @version 2018.02.01 10:02
 */
public abstract class AbstractXmlTypeConvert<T> implements XmlTypeConvert<T> {
    private Class<T> type;
    protected static final Logger logger = LoggerFactory.getLogger(AbstractXmlTypeConvert.class);

    @SuppressWarnings("unchecked")
    public AbstractXmlTypeConvert() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        // 只检查一层Repository泛型参数，不检查父类
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            type = (Class<T>)actualTypeArguments[0];
        } else {
            logger.warn("请检查[{}]类的泛型", this.getClass());
        }
    }

    @Override
    public Class<T> resolve() {
        return type;
    }
}
