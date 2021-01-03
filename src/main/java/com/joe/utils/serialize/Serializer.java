package com.joe.utils.serialize;

/**
 * 序列化接口，提供序列化能力
 *
 * @author joe
 */
public interface Serializer {

    /**
     * 将对象序列化
     *
     * @param t
     *            要序列化的对象
     * @param <T>
     *            对象的实际类型
     * @return 对象序列化后的数据
     * @throws SerializeException
     *             序列化异常时应抛出该异常
     */
    <T> byte[] write(T t) throws SerializeException;

    /**
     * 将对象序列化
     *
     * @param t
     *            要序列化的对象
     * @param <T>
     *            对象的实际类型
     * @return 对象序列化后的数据
     * @throws SerializeException
     *             序列化异常时应抛出该异常
     */
    <T> String writeToString(T t) throws SerializeException;

    /**
     * 将数据序列化为对象
     *
     * @param data
     *            对象的数据
     * @param clazz
     *            对象的Class，不允许为空，为空时抛出异常
     * @param <T>
     *            对象的实际类型
     * @return 数据反序列化后的对象
     * @throws SerializeException
     *             序列化异常时应抛出该异常
     */
    <T> T read(byte[] data, Class<T> clazz) throws SerializeException;

    /**
     * 将数据序列化为对象
     *
     * @param data
     *            对象的数据
     * @param clazz
     *            对象的Class，不允许为空，为空时抛出异常
     * @param <T>
     *            对象的实际类型
     * @return 数据反序列化后的对象
     * @throws SerializeException
     *             序列化异常时应抛出该异常
     */
    <T> T read(String data, Class<T> clazz) throws SerializeException;
}
