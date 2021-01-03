package com.joe.utils.serialize.xml;

import java.lang.annotation.*;

/**
 * XML忽略未知元素配置
 *
 * @author joe
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface XmlIgnoreProperties {
    /**
     * 是否忽略未知元素
     *
     * @return 返回true表示忽略未知元素，默认为false
     */
    boolean ignoreUnknown() default false;
}
