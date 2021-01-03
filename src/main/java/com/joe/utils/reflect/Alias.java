package com.joe.utils.reflect;

import java.lang.annotation.*;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月06日 16:37 JoeKerouac Exp $
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface Alias {
    /**
     * 字段别名列表
     * 
     * @return 字段别名列表，如果为null或者空时默认使用字段名本身
     */
    String[] value();
}
