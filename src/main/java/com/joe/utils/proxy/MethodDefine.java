package com.joe.utils.proxy;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 方法定义
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月08日 09:33 JoeKerouac Exp $
 */
@Data
@AllArgsConstructor
public class MethodDefine {

    /**
     * 方法名
     */
    private final String name;

    /**
     * 方法访权限，例如{@link java.lang.reflect.Modifier#PUBLIC public}
     */
    private final int    modifiers;

    /**
     * 方法实现
     */
    private Interception implemention;

}
