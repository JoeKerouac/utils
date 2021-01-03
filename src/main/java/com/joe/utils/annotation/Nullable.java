package com.joe.utils.annotation;

import java.lang.annotation.*;

/**
 * @author joe
 * @version 2018.06.13 11:46
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@Documented
public @interface Nullable {}
