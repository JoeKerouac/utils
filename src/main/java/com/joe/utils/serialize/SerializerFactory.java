package com.joe.utils.serialize;

import com.joe.utils.exception.NoSupportException;
import com.joe.utils.serialize.json.JsonParser;
import com.joe.utils.serialize.xml.XmlParser;

/**
 * 序列化工厂
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年04月08日 20:28 JoeKerouac Exp $
 */
public class SerializerFactory {

    public static Serializer getInstance(SerializerEnum serializerType) {
        switch (serializerType) {
            case XML:
                return XmlParser.getInstance();
            case JSON:
                return JsonParser.getInstance();
            case FORM:;
            default:
                throw new NoSupportException("不支持的Serializer类型：" + serializerType);
        }
    }
}
