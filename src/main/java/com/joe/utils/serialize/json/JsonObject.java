package com.joe.utils.serialize.json;

import java.util.HashMap;
import java.util.Map;

/**
 * json对象，主要是为了简化简单json对象的构建
 * 
 * @author joe
 *
 */
public class JsonObject {
    private static final JsonParser parser = JsonParser.getInstance();
    private Map<Object, Object>     data;

    public JsonObject() {
        this.data = new HashMap<>();
    }

    public JsonObject(Object key, Object value) {
        this.data = new HashMap<>();
        data.put(key, value);
    }

    public JsonObject data(Object key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public String toJson() {
        return parser.toJson(data);
    }

    public String toString() {
        return parser.toJson(data);
    }
}
