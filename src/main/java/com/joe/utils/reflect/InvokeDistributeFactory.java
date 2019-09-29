package com.joe.utils.reflect;

import com.joe.utils.collection.CollectionUtil;
import com.joe.utils.reflect.clazz.ClassUtils;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月21日 19:40 JoeKerouac Exp $
 */
public interface InvokeDistributeFactory {

    /**
     * 生成一个新的Class对象，使新的Class对象继承{@link InvokeDistribute}并且实现其{@link InvokeDistribute#invoke(String, String, String, Object[])}方法
     * @param clazz 给定Class对象，必须是public修饰的，并且有一个公共的无参构造器，并且不包含带有给定Class类型的参数的构造
     *              器，例如对于User这个Class来说，必须包含User()这个构造器，同时不能包含User(User)这个构造器
     * @return 生成的新的Class对象，包含一个带有给定Class类型的参数的构造器
     */
    default Class<InvokeDistribute> build(Class<?> clazz) {
        return build(clazz, null);
    }

    /**
     * 对target的Class对象调用{@link #build(Class)}生成一个新的Class，并且调用生成的构造器将target传入
     * @param target 指定对象
     * @return 对target生成的InvokeDistribute代理
     */
    default InvokeDistribute build(Object target) {
        return build(target, null);
    }

    /**
     * 生成一个新的Class对象，使新的Class对象继承{@link InvokeDistribute}并且实现其{@link InvokeDistribute#invoke(String, String, String, Object[])}方法
     * @param clazz 给定Class对象，必须是public修饰的，并且有一个公共的无参构造器，并且不包含带有给定Class类型的参数的构造
     *              器，例如对于User这个Class来说，必须包含User()这个构造器，同时不能包含User(User)这个构造器
     * @param className 生成的class将会使用该className
     * @return 生成的新的Class对象，包含一个带有给定Class类型的参数的构造器
     */
    default Class<InvokeDistribute> build(Class<?> clazz, String className) {
        return build(clazz, className, null);
    }

    /**
     * 对target的Class对象调用{@link #build(Class)}生成一个新的Class，并且调用生成的构造器将target传入
     * @param target 指定对象
     * @param className 生成的class将会使用该className
     * @return 对target生成的InvokeDistribute代理
     */
    default InvokeDistribute build(Object target, String className) {
        return build(target, className, null);
    }

    /**
     * 生成一个新的Class对象，使新的Class对象继承{@link InvokeDistribute}并且实现其{@link InvokeDistribute#invoke(String, String, String, Object[])}方法
     * @param clazz 给定Class对象，必须是public修饰的，并且有一个公共的无参构造器，并且不包含带有给定Class类型的参数的构造
     *              器，例如对于User这个Class来说，必须包含User()这个构造器，同时不能包含User(User)这个构造器
     * @param className 生成的class将会使用该className
     * @param classLoader 用于加载生成的动态类的classLoader
     * @return 生成的新的Class对象，包含一个带有给定Class类型的参数的构造器
     */
    Class<InvokeDistribute> build(Class<?> clazz, String className, DynamicClassLoader classLoader);

    /**
     * 对target的Class对象调用{@link #build(Class)}生成一个新的Class，并且调用生成的构造器将target传入
     * @param target 指定对象
     * @param className 生成的class将会使用该className，可以为null，为null时应由实现类生成
     * @param classLoader 用于加载生成的动态类的classLoader，可以为null，为null时应由实现类自主决策
     * @return 对target生成的InvokeDistribute代理
     */
    @SuppressWarnings("unchecked")
    default InvokeDistribute build(Object target, String className,
                                   DynamicClassLoader classLoader) {
        Class<InvokeDistribute> clazz = build(target.getClass(), className, classLoader);
        return ClassUtils.getInstance(clazz, CollectionUtil.array(target.getClass()),
            CollectionUtil.array(target));
    }
}
