package com.joe.utils.reflect.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import lombok.Data;

/**
 * java类型
 *
 * @author joe
 */
@Data
public class JavaType implements Type {

    /**
     * 类型名称，例如String（当该类型为泛型时该值为泛型名称，例如T，不是实际名称）
     */
    protected String name;

    /**
     * 类型注解说明
     */
    protected Annotation[] annotations;
}
