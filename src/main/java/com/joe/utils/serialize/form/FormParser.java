package com.joe.utils.serialize.form;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.joe.utils.common.Assert;
import com.joe.utils.common.string.StringUtils;
import com.joe.utils.exception.NoSupportException;
import com.joe.utils.reflect.clazz.ClassUtils;
import com.joe.utils.reflect.type.JavaTypeUtil;
import com.joe.utils.reflect.ReflectUtil;
import com.joe.utils.serialize.SerializeException;
import com.joe.utils.serialize.Serializer;

/**
 * form格式数据解析，只能解析简单对象，如果要解析为map那么只能解析为key、value泛型均为String的map
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年04月08日 20:43 JoeKerouac Exp $
 */
public class FormParser implements Serializer {
    @Override
    public <T> byte[] write(T t) throws SerializeException {
        return new byte[0];
    }

    @Override
    public <T> String writeToString(T t) throws SerializeException {
        if (t == null) {
            return null;
        }

        Class<?> type = t.getClass();

        StringBuilder sb = new StringBuilder();

        if (JavaTypeUtil.isSimple(type)) {
            return String.valueOf(t);
        } else {
            Arrays.stream(ReflectUtil.getAllFields(type)).forEach(field -> {
                Object value = ReflectUtil.getFieldValue(t, field);
                if (value == null) {
                    value = "";
                }
                sb.append(field.getName()).append("=").append(String.valueOf(value)).append("&");
            });
        }

        return null;
    }

    @Override
    public <T> T read(byte[] data, Class<T> clazz) throws SerializeException {
        return read(new String(data), clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T read(String data, Class<T> clazz) throws SerializeException {
        Assert.notNull(clazz);
        if (data == null) {
            return null;
        }

        Map<String, String> params = readAsMap(data);

        if (Map.class.isAssignableFrom(clazz)) {
            Map<String, String> map = null;
            try {
                map = (Map<String, String>)ClassUtils.getInstance(clazz);
            } catch (Exception e) {

            }
            if (map == null) {
                return (T)params;
            } else {
                map.putAll(params);
                return (T)map;
            }
        }

        if (JavaTypeUtil.isSimple(clazz)) {
            Collection<String> values = params.values();
            for (String value : values) {
                try {
                    return (T)readValue(value, clazz);
                } catch (Exception e) {
                }
            }
            throw new NoSupportException("FormParser不支持的数据类型：" + clazz);
        } else {
            T t = ClassUtils.getInstance(clazz);
            if (StringUtils.isEmpty(data)) {
                return t;
            }

            Arrays.stream(ReflectUtil.getAllFields(clazz)).forEach(
                field -> ReflectUtil.setFieldValue(t, field, readValue(params.get(field.getName()), field.getType())));
            return t;
        }
    }

    /**
     * 将form格式数据读取为Map
     * 
     * @param data
     *            form格式数据，不能为空，外部调用自己检查
     * @return 读取后的数据
     */
    private Map<String, String> readAsMap(String data) {
        Map<String, String> params = new HashMap<>();

        Arrays.stream(data.split("&")).forEach(arg -> {
            if (StringUtils.isEmpty(arg)) {
                return;
            }
            String[] args = arg.split("=");
            if (args.length == 1) {
                params.put(args[0], null);
            } else {
                params.put(args[0], args[1]);
            }
        });
        return params;
    }

    /**
     * 读取值
     * 
     * @param value
     *            值
     * @param type
     *            值类型
     * @return 读取到的值
     */
    private Object readValue(String value, Class<?> type) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        if (byte.class.equals(type) || Byte.class.equals(type)) {
            return Byte.valueOf(value);
        } else if (short.class.equals(type) || Short.class.equals(type)) {
            return Short.valueOf(value);
        } else if (int.class.equals(type) || Integer.class.equals(type)) {
            return Integer.valueOf(value);
        } else if (long.class.equals(type) || Long.class.equals(type)) {
            return Long.valueOf(value);
        } else if (float.class.equals(type) || Float.class.equals(type)) {
            return Float.valueOf(value);
        } else if (double.class.equals(type) || Double.class.equals(type)) {
            return Double.valueOf(value);
        } else if (boolean.class.equals(type) || Boolean.class.equals(type)) {
            return Boolean.valueOf(value);
        } else if (char.class.equals(type) || Character.class.equals(type)) {
            return value.charAt(0);
        } else if (BigDecimal.class.equals(type)) {
            return new BigDecimal(value);
        } else if (String.class.equals(type)) {
            return value;
        } else if (Enum.class.equals(type)) {
            return ReflectUtil.invoke(type, "valueOf", new Class[] {String.class}, value);
        } else {
            throw new NoSupportException("FormParser不支持的数据类型：" + type);
        }
    }
}
