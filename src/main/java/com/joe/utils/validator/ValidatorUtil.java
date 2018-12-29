package com.joe.utils.validator;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableValidator;

import com.joe.utils.common.Assert;

/**
 * bean校验工具，不符合规则的会抛出异常
 *
 * @author joe
 * @version 2018.07.02 15:15
 */
public class ValidatorUtil {
    /**
     * bean验证器
     */
    private static final Validator           validator;
    /**
     * 构造器、方法参数、方法响应验证器
     */
    private static final ExecutableValidator executableValidator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        executableValidator = validator.forExecutables();
    }

    /**
     * 校验bean是否符合规则
     *
     * @param bean 要校验的bean
     */
    public static void validate(@NotNull Object bean) {
        Assert.notNull(bean, "bean不能为null");
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(bean);
        check(constraintViolations);
    }

    /**
     * 验证方法参数是否符合规则
     *
     * @param instance 方法所在的类的实例
     * @param method   方法实例
     * @param params   参数
     */
    public static void validateParameters(Object instance, Method method, Object[] params) {
        Assert.notNull(instance, "instance不能为null");
        Assert.notNull(method, "method不能为null");
        Set<ConstraintViolation<Object>> constraintViolations = executableValidator
            .validateParameters(instance, method, params);
        check(constraintViolations);
    }

    /**
     * 检查是否有校验错误
     *
     * @param constraintViolations 校验结果
     */
    private static void check(Set<ConstraintViolation<Object>> constraintViolations) {
        Iterator<ConstraintViolation<Object>> iterator = constraintViolations.iterator();
        if (iterator.hasNext()) {
            throw new ValidationException(iterator.next().getMessage());
        }
    }
}
