package com.joe.utils.reflect.asm;

import static com.joe.utils.reflect.ByteCodeUtils.*;
import static com.joe.utils.reflect.InvokeDistribute.DISTRIBUTE_METHOD_DESC;
import static com.joe.utils.reflect.InvokeDistribute.DISTRIBUTE_METHOD_NAME;
import static com.joe.utils.reflect.asm.AsmByteCodeUtils.*;
import static org.objectweb.asm.Opcodes.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.joe.utils.collection.CollectionUtil;
import com.joe.utils.common.Assert;
import com.joe.utils.common.IOUtils;
import com.joe.utils.common.string.StringFormater;
import com.joe.utils.common.string.StringUtils;
import com.joe.utils.exception.InvokeException;
import com.joe.utils.reflect.*;

/**
 * 使用ASM实现的InvokeDistributeFactory
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月19日 19:09 JoeKerouac Exp $
 */
public class AsmInvokeDistributeFactory implements InvokeDistributeFactory {

    /**
     * 默认classLoader实例
     */
    private static final DynamicClassLoader DEFAULT_CLASSLOADER = new AsmDynamicClassLoader();

    /**
     * 被代理对象在代理对象中的字段名
     */
    private static final String             TARGET_FIELD_NAME   = "target";

    /**
     * Error方法的带String的构造器
     */
    private static final Constructor<Error> ERROR_CONSTRUCTOR   = ReflectUtil
        .getConstructor(Error.class, String.class);

    private final int                       version;

    public AsmInvokeDistributeFactory() {
        String nowVersion = System.getProperty("java.version");
        Assert.notNull(nowVersion);
        if (nowVersion.startsWith("1.8")) {
            version = V1_8;
        } else {
            throw new InvokeException("不支持的java版本:" + nowVersion);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<InvokeDistribute> build(Class<?> clazz, String className,
                                         DynamicClassLoader classLoader) {
        Assert.notNull(clazz);

        // 校验给定的class
        {
            if (InvokeDistribute.class.isAssignableFrom(clazz)) {
                return (Class<InvokeDistribute>) clazz;
            }

            int modifier = clazz.getModifiers();
            if (!Modifier.isPublic(modifier)) {
                throw new InvokeException(
                    StringFormater.simpleFormat("给定class[{0}]不是public的", clazz.getName()));
            }
        }

        // 校验构造器
        {
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                if (!AccessorUtil.isPublic(constructor)) {
                    throw new InvokeException(
                        StringFormater.simpleFormat("给定class[{}]无参构造器不是public", clazz.getName()));
                }
            } catch (NoSuchMethodException e) {
                throw new InvokeException(
                    StringFormater.simpleFormat("给定class[{}]没有无参构造器", clazz.getName()), e);
            }

            try {
                clazz.getDeclaredConstructor(clazz);
                throw new InvokeException(StringFormater.simpleFormat(
                    "给定class[{}]不能包含只有一个[{}]类型参数的构造器", clazz.getName(), clazz.getName()));
            } catch (NoSuchMethodException e) {
                // 没有该构造器是正常的
            }
        }

        if (StringUtils.isEmpty(className)) {
            className = createClassName();
        }

        if (classLoader == null) {
            classLoader = DEFAULT_CLASSLOADER;
        }

        // 开始生成
        byte[] byteCode = buildByteCode(clazz, className);
        try {
            IOUtils.saveAsFile(byteCode, "/Users/joekerouac/workspace/code/Ab.class");
        } catch (Exception e) {

        }
        return classLoader.buildClass(className, byteCode, 0, byteCode.length);
    }

    /**
     * 构建byte code
     * @param parentClass 父类
     * @param className 生成的class名
     * @return 生成的class的byte code数据
     */
    public byte[] buildByteCode(Class<?> parentClass, String className) {

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        cw.visit(version, // Java version
            ACC_PUBLIC, // public class
            convert(className), // package and name
            null, // signature (null means not generic)
            convert(parentClass), // superclass
            new String[] { convert(InvokeDistribute.class) });

        // 声明保存实际Target的字段
        cw.visitField(ACC_PRIVATE + ACC_FINAL, TARGET_FIELD_NAME, getByteCodeType(parentClass),
            null, null).visitEnd();

        /* 为类构建默认构造器（编译器会自动生成，但是此处要手动生成bytecode就只能手动生成无参构造器了） */
        generateDefaultConstructor(cw, parentClass, className);

        // 构建分发方法
        buildMethod(cw, className, parentClass);
        //        buildMethod(cw);

        // finish the class definition
        cw.visitEnd();
        return cw.toByteArray();
    }

    /**
     * 构建{@link InvokeDistribute#invoke(String, String, String, Object[]) invoke}方法
     * @param cw ClassWriter
     * @param className 生成的类名
     * @param parentClass 父类
     */
    private static void buildMethod(ClassWriter cw, String className, Class<?> parentClass) {
        /* Build method */
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, // public method
            DISTRIBUTE_METHOD_NAME, // name
            DISTRIBUTE_METHOD_DESC, // descriptor
            null, // signature (null means not generic)
            CollectionUtil.array(convert(NoSuchMethodException.class))); // exceptions (array of strings)
        // 开始方法区
        mv.visitCode();

        // 判断要调用那个方法，然后将动态调用转化为对应的本地调用
        {
            List<Method> allMethod = ReflectUtil.getAllMethod(parentClass);

            Label next = new Label();
            Label start = new Label();
            for (Method method : allMethod) {
                // 只处理非静态的public方法和protected方法
                if (Modifier.isStatic(method.getModifiers())
                    || (!AccessorUtil.isPublic(method) && !AccessorUtil.isProtected(method))) {
                    continue;
                }

                createIf(mv, method, next, start, className, parentClass);
                start = next;
                next = new Label();
            }
            // 结束位置标记
            mv.visitLabel(start);
            mv.visitFrame(F_SAME, 0, null, 0, null);
        }

        // throw new NoSuchMethodException(String.format("method [%s:%s:%s] not found", owner, methodName, desc));
        {
            // 默认抛出Error，不应该有这种情况
            mv.visitTypeInsn(NEW, convert(NoSuchMethodException.class));
            mv.visitInsn(DUP);
            mv.visitLdcInsn("method [%s:%s:%s] not found");
            mv.visitInsn(ICONST_3);
            mv.visitTypeInsn(ANEWARRAY, convert(Object.class));
            mv.visitInsn(DUP);

            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitInsn(AASTORE);
            mv.visitInsn(DUP);

            mv.visitInsn(ICONST_1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitInsn(AASTORE);
            mv.visitInsn(DUP);

            mv.visitInsn(ICONST_2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitInsn(AASTORE);

            mv.visitMethodInsn(INVOKESTATIC, convert(String.class),
                MethodConst.FORMAT_METHOD.getName(), getMethodDesc(MethodConst.FORMAT_METHOD),
                false);

            mv.visitMethodInsn(INVOKESPECIAL, convert(NoSuchMethodException.class), INIT,
                getConstructorDesc(ERROR_CONSTRUCTOR), false);
            mv.visitInsn(ATHROW);
            mv.visitMaxs(0, 0);
        }

        mv.visitEnd();
    }

    /**
     * 创建if分支的byte code
     * @param mv MethodVisitor
     * @param method Method
     * @param next 下一个分支的起始位置
     * @param start 该分支的结束位置
     */
    private static void createIf(MethodVisitor mv, Method method, Label next, Label start,
                                 String className, Class<?> parentClass) {
        // 标记分支开始位置
        mv.visitLabel(start);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        // 比较方法声明类
        stringEquals(mv, () -> mv.visitVarInsn(ALOAD, 1), convert(method.getDeclaringClass()), next,
            () -> {
                // 比较方法名
                stringEquals(mv, () -> mv.visitVarInsn(ALOAD, 2), method.getName(), next, () -> {
                    // 方法名一致再比较方法说明
                    stringEquals(mv, () -> mv.visitVarInsn(ALOAD, 3),
                        ByteCodeUtils.getMethodDesc(method), next, () -> {
                            // 方法说明也一致后执行方法
                            invokeMethod(mv, method, () -> {
                                // 调用代理对象对应的方法而不是本代理的方法
                                mv.visitVarInsn(ALOAD, 0);
                                mv.visitFieldInsn(GETFIELD, convert(className), TARGET_FIELD_NAME,
                                    getByteCodeType(parentClass));
                                // 获取参数数量，用于载入参数
                                int count = method.getParameterCount();
                                Class<?>[] types = method.getParameterTypes();

                                // 循环载入参数
                                for (int i = 0; i < count; i++) {
                                    mv.visitVarInsn(Opcodes.ALOAD, 4);
                                    // 这里注意，访问数组下标0-5和6-无穷是不一样的
                                    // 访问0-5对应的byte code：  aload | iconst_[0-5] | aaload
                                    // 访问下标大于5的byte code: aload | bipush [6-无穷] aaload
                                    if (i <= 5) {
                                        mv.visitInsn(ICONST_0 + i);
                                    } else {
                                        mv.visitIntInsn(BIPUSH, i);
                                    }
                                    mv.visitInsn(Opcodes.AALOAD);
                                    mv.visitTypeInsn(CHECKCAST, convert(types[i]));
                                }
                            });
                        });
                });
            });
    }

    /**
     * 为类构建默认构造器（正常编译器会自动生成，但是此处要手动生成bytecode就只能手动生成无参构造器了
     * @param cw ClassWriter
     * @param parentClass 构建类的父类，会自动调用父类的构造器
     * @param className 生成的新类名
     */
    private static void generateDefaultConstructor(ClassWriter cw, Class<?> parentClass,
                                                   String className) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, // public method
            INIT, // method name
            getDesc(void.class, parentClass), // descriptor
            null, // signature (null means not generic)
            null); // exceptions (array of strings)

        mv.visitCode(); // Start the code for this method

        // 调用父类构造器
        mv.visitVarInsn(ALOAD, 0); // Load "this" onto the stack
        mv.visitMethodInsn(INVOKESPECIAL, // Invoke an instance method (non-virtual)
            convert(parentClass), // Class on which the method is defined
            INIT, // Name of the method
            getDesc(void.class), // Descriptor
            false); // Is this class an interface?

        // 设置字段值，相当于this.target = target;
        mv.visitVarInsn(ALOAD, 0); // 加载this
        mv.visitVarInsn(ALOAD, 1); // 加载参数
        mv.visitFieldInsn(PUTFIELD, convert(className), TARGET_FIELD_NAME,
            getByteCodeType(parentClass));

        mv.visitMaxs(2, 1);
        mv.visitInsn(RETURN); // End the constructor method
        mv.visitEnd();
    }
}
