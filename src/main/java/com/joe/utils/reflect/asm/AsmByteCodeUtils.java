package com.joe.utils.reflect.asm;

import static com.joe.utils.reflect.ByteCodeUtils.convert;
import static com.joe.utils.reflect.ByteCodeUtils.getMethodDesc;
import static org.objectweb.asm.Opcodes.*;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.joe.utils.reflect.ByteCodeUtils;
import com.joe.utils.reflect.MethodConst;

/**
 * Asm byte code 工具
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月20日 10:41 JoeKerouac Exp $
 */
public class AsmByteCodeUtils {

    /**
     * 动态类计数器，用于生成动态类的名字
     */
    private static final AtomicInteger COUNTER        = new AtomicInteger(0);

    /**
     * 生成的动态类的前缀
     */
    private static final String        PRE_CLASS_NAME = "com.joe.reflect.asm.Proxy$";

    /**
     * 生成类名
     * @return 类名
     */
    public static String createClassName() {
        return PRE_CLASS_NAME + COUNTER.getAndIncrement();
    }

    /**
     * byte code执行指定方法
     * @param mv MethodVisitor
     * @param method 要执行的方法
     * @param load 加载方法需要的数据
     */
    public static void invokeMethod(MethodVisitor mv, Method method, Runnable load) {
        // 先加载数据
        load.run();
        // 执行方法
        if (method.getDeclaringClass().isInterface()) {
            mv.visitMethodInsn(INVOKEINTERFACE, convert(method.getDeclaringClass()),
                method.getName(), getMethodDesc(method), true);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, convert(method.getDeclaringClass()), method.getName(),
                getMethodDesc(method), false);
        }
        if (method.getReturnType() == void.class) {
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
        } else {
            // 返回结果
            mv.visitInsn(ARETURN);
        }
    }

    /**
     * if分支调用String的equals方法，失败则跳转到指定位置
     * @param mv MethodVisitor
     * @param load 加载要比较的字符串
     * @param content 常量字符串
     * @param next 比较失败后跳转的位置
     * @param success 比较一致后执行的操作
     */
    public static void stringEquals(MethodVisitor mv, Runnable load, String content, Label next,
                                    Runnable success) {
        // 加载String
        load.run();
        // 加载实际方法名
        mv.visitLdcInsn(content);
        // 调用String的equals方法
        mv.visitMethodInsn(INVOKEVIRTUAL, convert(String.class), "equals",
            ByteCodeUtils.getMethodDesc(MethodConst.EQUALAS_METHOD), false);
        // 如果结果为false则跳转到下一个
        mv.visitJumpInsn(IFEQ, next);
        success.run();
    }
}
