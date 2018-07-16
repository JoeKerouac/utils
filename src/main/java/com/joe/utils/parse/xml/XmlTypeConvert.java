package com.joe.utils.parse.xml;

import org.dom4j.Element;

/**
 * xml类型转换，将String类型转换为对应的类型
 *
 * @param <T> 要转换的类型
 * @author joe
 * @version 2018.01.30 14:10
 */
public interface XmlTypeConvert<T> {
    /**
     * 数据转换，将xml中的字符串数据转换为用户需要的指定类型数据
     *
     * @param element  节点
     * @param attrName 要获取的属性名，如果该值不为空则认为数据需要从属性中取而不是从节点数据中取
     * @return 转换后的数据
     */
    T read(Element element, String attrName);

    //    /**
    //     * 数据转换，将字段的值转换为xml中的内容
    //     *
    //     * @param obj
    //     *
    //     * @return
    //     */
    //    String write(Object obj);

    /**
     * 确定转换后的类型
     *
     * @return 转换后的类型
     */
    Class<T> resolve();
}
