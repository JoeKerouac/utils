package com.joe.utils.poi;

import java.lang.annotation.*;

/**
 * excel列注解
 *
 * @author joe
 * @version 2018.06.14 14:24
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
public @interface ExcelColumn {
    /**
     * 排序，会将所有带ExcelColumn的字段按照sort从小到大的顺序排，一样的随机排序
     *
     * @return 大小
     */
    int sort() default 100;

    /**
     * 对应的列标题，默认采用字段名
     *
     * @return 列标题
     */
    String value() default "";

    /**
     * 是否忽略
     *
     * @return true表示忽略
     */
    boolean ignore() default false;
}
