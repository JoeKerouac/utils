package com.joe.utils.serialize.json;

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.joe.utils.common.Assert;
import com.joe.utils.common.StringUtils;
import com.joe.utils.exception.ExceptionWraper;
import com.joe.utils.serialize.SerializeException;
import com.joe.utils.serialize.Serializer;

import lombok.extern.slf4j.Slf4j;

/**
 * json解析工具（需要注意的是对于byte数组的处理，该实现采用的是jackson的实现，所以对于byte数组会将其转换为BASE64的字符串）
 *
 * @author joe
 */
@Slf4j
public class JsonParser implements Serializer {
    private static final JsonParser   JSON_PARSER;
    private static final ObjectMapper MAPPER_IGNORE_NULL;
    private static final ObjectMapper MAPPER;

    private JsonParser() {
    }

    static {
        MAPPER = new ObjectMapper();
        MAPPER_IGNORE_NULL = new ObjectMapper();

        MAPPER.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        MAPPER_IGNORE_NULL.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        JSON_PARSER = new JsonParser();
    }

    public static JsonParser getInstance() {
        return JSON_PARSER;
    }

    @Override
    public <T> byte[] write(T t) throws SerializeException {
        return ExceptionWraper.runWithResult(() -> toJson(t).getBytes(), SerializeException::new);
    }

    @Override
    public <T> String writeToString(T t) throws SerializeException {
        return ExceptionWraper.runWithResult(() -> toJson(t), SerializeException::new);
    }

    @Override
    public <T> T read(byte[] data, Class<T> clazz) throws SerializeException {
        return ExceptionWraper.runWithResult(() -> readAsObject(data, clazz),
            SerializeException::new);
    }

    @Override
    public <T> T read(String data, Class<T> clazz) throws SerializeException {
        return ExceptionWraper.runWithResult(() -> readAsObject(data, clazz),
            SerializeException::new);
    }

    /**
     * 将Object序列化（对于byte[]，会将byte[]用Base64编码一下，然后返回，相当于对该byte调用Base64的encode方法然后将结果返回，对于String
     * 会直接将String返回）
     *
     * @param obj 要序列化的数据
     * @return 序列化失败将返回空字符串
     */
    public String toJson(Object obj) {
        return toJson(obj, false);
    }

    /**
     * 将Object序列化（对于byte[]，会将byte[]用Base64编码一下，然后返回，相当于对该byte调用Base64的encode方法然后将结果返回，对于String
     * 会直接将String返回）
     *
     * @param obj        要序列化的对象
     * @param ignoreNull 是否忽略空元素 ，如果为true为忽略
     * @return 序列化失败将返回空字符串
     */
    public String toJson(Object obj, boolean ignoreNull) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            ObjectMapper mapper;
            if (ignoreNull) {
                mapper = MAPPER_IGNORE_NULL;
            } else {
                mapper = MAPPER;
            }
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("序列化失败，失败原因：", e);
            return "";
        }
    }

    /**
     * 解析json
     *
     * @param content json字符串
     * @param type    json解析后对应的实体类型
     * @param <T>     实体类型的实际类型
     * @return 解析失败将返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T readAsObject(String content, Class<T> type) {
        Assert.notNull(type);
        try {
            if (StringUtils.isEmpty(content)) {
                log.debug("content为空，返回null", content, type);
                return null;
            } else if (type.equals(String.class)) {
                return (T) content;
            }
            return MAPPER.readValue(content, type);
        } catch (Exception e) {
            log.error("json解析失败，失败原因：", e);
            return null;
        }
    }

    /**
     * 解析json
     *
     * @param content json字符串
     * @param type    json解析后对应的实体类型
     * @param <T>     实体类型的实际类型
     * @return 解析失败将返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T readAsObject(byte[] content, Class<T> type) {
        try {
            if (content == null || content.length == 0 || type == null) {
                log.debug("content为{}，type为：{}", content, type);
                return null;
            } else if (type.equals(String.class)) {
                return (T) new String(content);
            }
            return MAPPER.readValue(content, type);
        } catch (Exception e) {
            log.error("json解析失败，失败原因：", e);
            return null;
        }
    }

    /**
     * 将json数据读取为带泛型的map类型的数据
     *
     * @param content   json数据
     * @param mapType   要返回的map类型
     * @param keyType   map的key的泛型
     * @param valueType map的value的泛型
     * @param <T>       Map的实际类型
     * @param <K>       map中key的实际类型
     * @param <V>       map中value的实际类型
     * @return map 解析结果
     */
    public <T extends Map<K, V>, K, V> T readAsMap(String content, Class<? extends Map> mapType,
                                                   Class<K> keyType, Class<V> valueType) {
        try {
            MapType type = MAPPER.getTypeFactory().constructMapType(mapType, keyType, valueType);
            return MAPPER.readValue(content, type);
        } catch (Exception e) {
            log.error("json解析失败，失败原因：", e);
            return null;
        }
    }

    /**
     * 将json数据读取为带泛型的map类型的数据
     *
     * @param content   json数据
     * @param mapType   要返回的map类型
     * @param keyType   map的key的泛型
     * @param valueType map的value的泛型
     * @param <T>       Map的实际类型
     * @param <K>       map中key的实际类型
     * @param <V>       map中value的实际类型
     * @return map 解析结果
     */
    public <T extends Map<K, V>, K, V> T readAsMap(byte[] content, Class<? extends Map> mapType,
                                                   Class<K> keyType, Class<V> valueType) {
        return readAsMap(new String(content), mapType, keyType, valueType);
    }

    /**
     * 将json读取为collection类型的数据
     *
     * @param content        json数据
     * @param collectionType collection类型
     * @param elementsType   collection泛型
     * @param <T>            list的实际类型
     * @param <V>            list的泛型
     * @return 解析结果
     */
    public <T extends Collection<V>, V> T readAsCollection(String content,
                                                           Class<? extends Collection> collectionType,
                                                           Class<V> elementsType) {
        try {
            CollectionLikeType type = MAPPER.getTypeFactory()
                .constructCollectionLikeType(collectionType, elementsType);
            return MAPPER.readValue(content, type);
        } catch (Exception e) {
            log.error("json解析失败，失败原因：", e);
            return null;
        }
    }

    /**
     * 将json读取为collection类型的数据
     *
     * @param content        json数据
     * @param collectionType collection类型
     * @param elementsType   collection泛型
     * @param <T>            list的实际类型
     * @param <V>            list的泛型
     * @return 解析结果
     */
    public <T extends Collection<V>, V> T readAsCollection(byte[] content,
                                                           Class<? extends Collection> collectionType,
                                                           Class<V> elementsType) {
        return readAsCollection(new String(content), collectionType, elementsType);
    }
}
