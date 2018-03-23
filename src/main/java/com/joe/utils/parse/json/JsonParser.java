package com.joe.utils.parse.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * json解析工具（高并发下String toJson(Object obj, boolean ignoreNull)存在问题）
 *
 * @author joe
 */
public class JsonParser {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(JsonParser.class);
    private static JsonParser jsonParser;

    private JsonParser() {
    }

    public static JsonParser getInstance() {
        if (jsonParser == null) {
            synchronized (logger) {
                if (jsonParser == null) {
                    jsonParser = new JsonParser();
                }
            }
        }
        return jsonParser;
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
            if (ignoreNull) {
                mapper.setSerializationInclusion(Include.NON_NULL);
            } else {
                mapper.setSerializationInclusion(Include.ALWAYS);
            }
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("序列化失败，失败原因：", e);
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
        try {
            if (content == null || content.isEmpty() || type == null) {
                logger.debug("content为{}，type为：{}", content, type);
                return null;
            } else if (type.equals(String.class)) {
                return (T) content;
            }
            return mapper.readValue(content, type);
        } catch (Exception e) {
            logger.error("json解析失败，失败原因：", e);
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
                logger.debug("content为{}，type为：{}", content, type);
                return null;
            } else if (type.equals(String.class)) {
                return (T) new String(content);
            }
            return mapper.readValue(content, type);
        } catch (Exception e) {
            logger.error("json解析失败，失败原因：", e);
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
    public <T extends Map<K, V>, K, V> T readAsMap(String content,
                                                   @SuppressWarnings("rawtypes") Class<? extends Map> mapType,
                                                   Class<K> keyType, Class<V> valueType) {
        try {
            MapType type = mapper.getTypeFactory().constructMapType(mapType, keyType, valueType);
            return mapper.readValue(content, type);
        } catch (Exception e) {
            logger.error("json解析失败，失败原因：", e);
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
    public <T extends Map<K, V>, K, V> T readAsMap(byte[] content,
                                                   @SuppressWarnings("rawtypes") Class<? extends Map> mapType,
                                                   Class<K> keyType, Class<V> valueType) {
        try {
            MapType type = mapper.getTypeFactory().constructMapType(mapType, keyType, valueType);
            return mapper.readValue(content, type);
        } catch (Exception e) {
            logger.error("json解析失败，失败原因：", e);
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
    public <T extends Collection<V>, V> T readAsCollection(String content,
                                                           @SuppressWarnings("rawtypes") Class<? extends Collection>
                                                                   collectionType, Class<V> elementsType) {
        try {
            CollectionLikeType type = mapper.getTypeFactory().constructCollectionLikeType(collectionType, elementsType);
            return mapper.readValue(content, type);
        } catch (Exception e) {
            logger.error("json解析失败，失败原因：", e);
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
                                                           @SuppressWarnings("rawtypes") Class<? extends Collection>
                                                                   collectionType, Class<V> elementsType) {
        try {
            CollectionLikeType type = mapper.getTypeFactory().constructCollectionLikeType(collectionType, elementsType);
            return mapper.readValue(content, type);
        } catch (Exception e) {
            logger.error("json解析失败，失败原因：", e);
            return null;
        }
    }
}
