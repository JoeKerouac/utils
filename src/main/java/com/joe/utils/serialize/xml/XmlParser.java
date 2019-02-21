package com.joe.utils.serialize.xml;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.joe.utils.collection.CollectionUtil;
import com.joe.utils.common.StringUtils;
import com.joe.utils.exception.ExceptionWraper;
import com.joe.utils.reflect.BeanUtils;
import com.joe.utils.reflect.BeanUtils.CustomPropertyDescriptor;
import com.joe.utils.reflect.ReflectUtil;
import com.joe.utils.serialize.SerializeException;
import com.joe.utils.serialize.Serializer;
import com.joe.utils.serialize.xml.converter.XmlTypeConverterUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * XML解析（反向解析为java对象时不区分大小写），该解析器由于大量使用反射，所以在第一次解析
 * 某个类型的对象时效率较低，解析过一次系统会自动添加缓存，速度将会大幅提升（测试中提升了25倍）
 * <p>
 * XXE漏洞：https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet
 *
 * @author JoeKerouac
 */
@Slf4j
public class XmlParser implements Serializer {
    private static final XmlParser DEFAULT      = new XmlParser();
    private static final String    DEFAULT_ROOT = "root";
    private SAXReader              reader;

    private XmlParser() {
        this.reader = new SAXReader();
    }

    static {
        DEFAULT.enableDTD(false);
    }

    /**
     * 获取默认实例（禁用了外部DTD）
     *
     * @return 默认实例
     */
    public static XmlParser getInstance() {
        return DEFAULT;
    }

    /**
     * 构建一个新的XmlParser实例（默认禁用了外部DTD）
     *
     * @return 新的XmlParser实例
     */
    public static XmlParser buildInstance() {
        return buildInstance(Collections.emptyMap());
    }

    /**
     * 使用指定配置构建一个新的XmlParser实例（如果不做设置则默认禁用了外部DTD）
     *
     * @param prop 功能配置
     * @return 新的XmlParser实例
     */
    public static XmlParser buildInstance(Map<String, Boolean> prop) {
        XmlParser xmlParser = new XmlParser();
        if (!CollectionUtil.safeIsEmpty(prop)) {
            prop.forEach(xmlParser::setFeature);
        }
        return xmlParser;
    }

    /**
     * 设置DTD支持
     *
     * @param enable true表示支持DTD，false表示不支持
     */
    public void enableDTD(boolean enable) {
        //允许DTD会有XXE漏洞，关于XXE漏洞：https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet
        if (enable) {
            //不允许DTD
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            setFeature("http://xml.org/sax/features/external-general-entities", false);
            setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } else {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            setFeature("http://xml.org/sax/features/external-general-entities", true);
            setFeature("http://xml.org/sax/features/external-parameter-entities", true);
        }
    }

    /**
     * 配置SAXReader
     *
     * @param k      name
     * @param enable 是否允许，true表示允许
     */
    public void setFeature(String k, boolean enable) {
        try {
            reader.setFeature(k, enable);
        } catch (SAXException e) {
            throw new RuntimeException("设置属性失败:[" + k + ":" + enable + "]");
        }
    }

    /**
     * 将xml解析为Document
     *
     * @param text xml文本
     * @return Document
     * @throws DocumentException DocumentException
     */
    private Document parseText(String text) throws DocumentException {
        Document result;

        String encoding = getEncoding(text);

        InputSource source = new InputSource(new StringReader(text));
        source.setEncoding(encoding);

        result = reader.read(source);

        // if the XML parser doesn't provide a way to retrieve the encoding,
        // specify it manually
        if (result.getXMLEncoding() == null) {
            result.setXMLEncoding(encoding);
        }

        return result;
    }

    /**
     * 获取xml的编码方式
     *
     * @param text xml文件
     * @return xml的编码
     */
    private String getEncoding(String text) {
        String result = null;

        String xml = text.trim();

        if (xml.startsWith("<?xml")) {
            int end = xml.indexOf("?>");
            String sub = xml.substring(0, end);
            StringTokenizer tokens = new StringTokenizer(sub, " =\"\'");

            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();

                if ("encoding".equals(token)) {
                    if (tokens.hasMoreTokens()) {
                        result = tokens.nextToken();
                    }

                    break;
                }
            }
        }

        return result;
    }

    /**
     * 解析XML，将xml解析为map（注意：如果XML是&lt;a&gt;data&lt;b&gt;bbb&lt;/b&gt;&lt;/a&gt;
     * 这种格式那么data将不被解析，对于list可以正确解析）
     *
     * @param xml xml字符串
     * @return 由xml解析的Map，可能是String类型或者Map&lt;String, Object&gt;类型，其中Map的value有可能
     * 是Stirng类型，也有可能是List&lt;String&gt;类型
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parse(String xml) {
        try {
            Document document = parseText(xml);
            Element root = document.getRootElement();
            if (root.elements().size() == 0) {
                Map<String, Object> map = new HashMap<>();
                map.put(root.getName(), root.getText());
                return map;
            } else {
                return (HashMap<String, Object>) parse(root);
            }
        } catch (Exception e) {
            log.error("xml格式不正确", e);
            return null;
        }
    }

    /**
     * 将XML解析为POJO对象，暂时无法解析map，当需要解析的字段是{@link java.util.Collection}的子类时必须带有注
     * 解{@link XmlNode}，否则将解析失败。
     * <p>
     * PS：对象中的集合字段必须添加注解，必须是简单集合，即集合中只有一种数据类型，并且当类型不是String时需要指定
     * converter，否则将会解析失败。
     *
     * @param xml   XML源
     * @param clazz POJO对象的class
     * @param <T>   POJO的实际类型
     * @return 解析结果
     */
    @SuppressWarnings("unchecked")
    public <T> T parse(String xml, Class<T> clazz) {
        if (xml == null || clazz == null || xml.isEmpty()) {
            return null;
        }
        T pojo;
        Document document;

        // 获取pojo对象的实例
        try {
            // 没有权限访问该类或者该类（为接口、抽象类）不能实例化时将抛出异常
            pojo = clazz.newInstance();
        } catch (Exception e) {
            log.error("class对象生成失败，请检查代码；失败原因：", e);
            throw new RuntimeException(e);
        }

        // 解析XML
        try {
            document = parseText(xml);
        } catch (Exception e) {
            log.error("xml解析错误", e);
            return null;
        }

        // 获取pojo对象的说明
        CustomPropertyDescriptor[] propertyDescriptor = BeanUtils.getPropertyDescriptors(clazz);
        Element root = document.getRootElement();
        for (CustomPropertyDescriptor descript : propertyDescriptor) {
            XmlNode xmlNode = descript.getAnnotation(XmlNode.class);
            final String fieldName = descript.getName();
            //节点名
            String nodeName = null;
            //属性名
            String attributeName = null;
            boolean isParent = false;
            boolean ignore = false;
            if (xmlNode == null) {
                nodeName = fieldName;
            } else if (!xmlNode.ignore()) {
                //如果节点不是被忽略的节点那么继续
                //获取节点名称，优先使用注解，如果注解没有设置名称那么使用字段名
                nodeName = StringUtils.isEmpty(xmlNode.name()) ? fieldName : xmlNode.name();
                log.debug("字段[{}]对应的节点名为：{}", fieldName, nodeName);

                //判断节点是否是属性值
                if (xmlNode.isAttribute()) {
                    //如果节点是属性值，那么需要同时设置节点名和属性名，原则上如果是属性的话必须设置节点名，但是为了防止
                    //用户忘记设置，在用户没有设置的时候使用字段名
                    if (StringUtils.isEmpty(xmlNode.attributeName())) {
                        log.warn("字段[{}]是属性值，但是未设置属性名（attributeName字段），将采用字段名作为属性名",
                            descript.getName());
                        attributeName = fieldName;
                    } else {
                        attributeName = xmlNode.attributeName();
                    }
                    if (StringUtils.isEmpty(xmlNode.name())) {
                        log.debug("该字段是属性值，并且未设置节点名（name字段），设置isParent为true");
                        isParent = true;
                    }
                } else {
                    log.debug("字段[{}]对应的是节点");
                }
            } else {
                ignore = true;
            }

            if (!ignore) {
                //获取指定节点名的element
                List<Element> nodes = (!StringUtils.isEmpty(attributeName) && isParent)
                    ? Collections.singletonList(root)
                    : root.elements(nodeName);
                //判断是否为空
                if (nodes.isEmpty()) {
                    //如果为空那么将首字母大写后重新获取
                    nodes = root.elements(StringUtils.toFirstUpperCase(nodeName));
                }
                if (!nodes.isEmpty()) {
                    //如果还不为空，那么为pojo赋值
                    Class<?> type = descript.getRealType();

                    //开始赋值
                    //判断字段是否是集合
                    if (Collection.class.isAssignableFrom(type)) {
                        //是集合
                        setValue(nodes, attributeName, pojo, descript);
                    } else if (Map.class.isAssignableFrom(type)) {
                        //是Map
                        log.warn("当前暂时不支持解析map");
                    } else {
                        //不是集合，直接赋值
                        setValue(nodes.get(0), attributeName, pojo, descript);
                    }
                }
            }
        }
        return pojo;
    }

    /**
     * 将Object解析为xml，根节点为root，字段值为null的将不包含在xml中，暂时只能解析基本类型（可以正确解析list、map）
     *
     * @param source bean
     * @return 解析结果
     */
    public String toXml(Object source) {
        return toXml(source, null, false);
    }

    /**
     * 将Object解析为xml，暂时只能解析基本类型（可以正确解析list、map）
     *
     * @param source   bean
     * @param rootName 根节点名称，如果为null则会尝试使用默认值
     * @param hasNull  是否包含null元素（true：包含）
     * @return 解析结果
     */
    public String toXml(Object source, String rootName, boolean hasNull) {
        if (source == null) {
            log.warn("传入的source为null，返回null");
            return null;
        }

        if (rootName == null) {
            XmlNode xmlNode = source.getClass().getDeclaredAnnotation(XmlNode.class);
            rootName = xmlNode == null ? null : xmlNode.name();
        }

        if (rootName == null) {
            rootName = DEFAULT_ROOT;
        }

        Long start = System.currentTimeMillis();
        Element root = DocumentHelper.createElement(rootName);
        buildDocument(root, source, source.getClass(), !hasNull);
        Long end = System.currentTimeMillis();
        log.debug("解析xml用时" + (end - start) + "ms");
        return root.asXML();
    }

    /**
     * 根据pojo构建xml的document（方法附件参考附件xml解析器思路）
     *
     * @param parent     父节点
     * @param pojo       pojo，不能为空
     * @param clazz      pojo的Class
     * @param ignoreNull 是否忽略空元素
     */
    @SuppressWarnings("unchecked")
    private void buildDocument(Element parent, Object pojo, Class<?> clazz, boolean ignoreNull) {
        //字段描述，key是节点名，value是XmlData
        Map<String, XmlData> map = new HashMap<>();
        //构建字段描述
        if (pojo instanceof Map) {
            Map<?, ?> pojoMap = (Map<?, ?>) pojo;
            pojoMap.forEach((k, v) -> {
                if (k == null) {
                    log.debug("忽略map中key为null的值");
                } else {
                    if (ignoreNull && v == null) {
                        log.debug("当前配置为忽略空值，[{}]的值为空，忽略", k);
                    } else {
                        map.put(String.valueOf(k),
                            new XmlData(null, v, v == null ? null : v.getClass()));
                    }
                }
            });
        } else {
            CustomPropertyDescriptor[] propertyDescriptors = BeanUtils
                .getPropertyDescriptors(clazz == null ? pojo.getClass() : clazz);
            for (CustomPropertyDescriptor descriptor : propertyDescriptors) {
                XmlNode xmlNode = descriptor.getAnnotation(XmlNode.class);
                //字段值
                try {
                    Object valueObj = pojo == null ? null
                        : BeanUtils.getProperty(pojo, descriptor.getName());
                    //判断是否忽略
                    if ((ignoreNull && valueObj == null) || (xmlNode != null && xmlNode.ignore())) {
                        log.debug("忽略空节点或者节点被注解忽略");
                        continue;
                    }

                    //节点名
                    String nodeName = (xmlNode == null || StringUtils.isEmpty(xmlNode.name()))
                        ? descriptor.getName()
                        : xmlNode.name();
                    map.put(nodeName, new XmlData(xmlNode, valueObj, descriptor.getRealType()));
                } catch (Exception e) {
                    log.error("获取字段值时发生异常，忽略改值", e);
                }
            }
        }

        map.forEach((k, v) -> {
            XmlNode xmlNode = v.getXmlNode();
            Object valueObj = v.getData();

            //节点名
            String nodeName = k;
            //属性名
            String attrName = (xmlNode == null || StringUtils.isEmpty(xmlNode.attributeName()))
                ? nodeName
                : xmlNode.attributeName();
            //是否是cdata
            boolean isCDATA = xmlNode != null && xmlNode.isCDATA();
            //数据类型
            Class<?> type = v.getType();
            //构建一个对应的节点
            Element node = parent.element(nodeName);
            if (node == null) {
                //搜索不到，创建一个（在属性是父节点属性的情况和节点是list的情况需要将该节点删除）
                node = DocumentHelper.createElement(nodeName);
                parent.add(node);
            }

            //判断字段对应的是否是属性
            if (xmlNode != null && xmlNode.isAttribute()) {
                //属性值，属性值只能是简单值
                String attrValue = valueObj == null ? "" : String.valueOf(valueObj);
                //判断是否是父节点的属性
                if (StringUtils.isEmpty(xmlNode.name())) {
                    //如果是父节点那么删除之前添加的
                    parent.remove(node);
                    node = parent;
                }
                //为属性对应的节点添加属性
                node.addAttribute(attrName, attrValue);
            } else if (type == null) {
                log.debug("当前不知道节点[{}]的类型，忽略该节点", k);
            } else if (ReflectUtil.isNotPojo(type)) {
                //是简单类型或者集合类型
                if (Map.class.isAssignableFrom(type)) {
                    log.warn("当前字段[{}]是map类型", k);
                    buildDocument(node, v.getData(), type, ignoreNull);
                } else if (Collection.class.isAssignableFrom(type)) {
                    parent.remove(node);
                    //集合类型
                    //判断字段值是否为null
                    if (valueObj != null) {
                        String arrayNodeName;
                        Element root;
                        if (StringUtils.isEmpty(xmlNode.arrayRoot())) {
                            arrayNodeName = nodeName;
                            root = parent;
                        } else {
                            arrayNodeName = xmlNode.arrayRoot();
                            root = DocumentHelper.createElement(nodeName);
                            parent.add(root);
                        }
                        Collection collection = (Collection) valueObj;
                        collection.stream().forEach(obj -> {
                            Element n = DocumentHelper.createElement(arrayNodeName);
                            root.add(n);
                            buildDocument(n, obj, null, ignoreNull);
                        });
                    }
                } else {
                    String text = valueObj == null ? "" : String.valueOf(valueObj);
                    if (isCDATA) {
                        log.debug("内容[{}]需要CDATA标签包裹", text);
                        node.add(DocumentHelper.createCDATA(text));
                    } else {
                        node.setText(text);
                    }
                }
            } else {
                //猜测字段类型（防止字段的声明是一个接口，优先采用xmlnode中申明的类型）
                Class<?> realType = resolveRealType(type, xmlNode);
                //pojo类型
                buildDocument(node, valueObj, realType, ignoreNull);
            }
        });
    }

    /**
     * 确定字段的真实类型
     *
     * @param fieldType 字段类型
     * @param xmlNode   字段XmlNode注解
     * @return 字段实际类型而不是接口或者抽象类
     */
    private Class<?> resolveRealType(Class<?> fieldType, XmlNode xmlNode) {
        //猜测字段类型（防止字段的声明是一个接口，优先采用xmlnode中申明的类型）
        Class<?> type = (xmlNode == null || xmlNode.general() == null) ? fieldType
            : xmlNode.general();

        if (!fieldType.isAssignableFrom(type)) {
            type = fieldType;
        }
        return type;
    }

    /**
     * 解析element
     *
     * @param element element
     * @return 解析结果，可能是String类型或者Map&lt;String, Object&gt;类型，其中Map的value有可能是Stirng类型，也有可能
     * 是List&lt;String&gt;类型
     */
    @SuppressWarnings("unchecked")
    private Object parse(Element element) {
        List<Element> elements = element.elements();
        if (elements.size() == 0) {
            return element.getText();
        } else {
            Map<String, Object> map = new HashMap<>();
            for (Element ele : elements) {
                Object result = parse(ele);
                if (map.containsKey(ele.getName())) {
                    // 如果map中已经包含该key，说明该key有多个，是个list
                    Object obj = map.get(ele.getName());
                    List<String> list;
                    if (obj instanceof List) {
                        // 如果obj不等于null并且是list对象，说明map中存的已经是一个list
                        list = (List<String>) obj;
                    } else {
                        // 如果obj等于null或者不是list对象，那么新建一个list对象
                        list = new ArrayList<>();
                        list.add(obj == null ? null : String.valueOf(obj));
                    }
                    list.add(result == null ? null : String.valueOf(result));
                    map.put(ele.getName(), list);
                } else {
                    map.put(ele.getName(), result);
                }
            }
            return map;
        }
    }

    /**
     * 往pojo中指定字段设置值
     *
     * @param element  要设置的数据节点
     * @param attrName 要获取的属性名，如果该值不为空则认为数据需要从属性中取而不是从节点数据中取
     * @param pojo     pojo
     * @param field    字段说明
     */
    private void setValue(Element element, String attrName, Object pojo,
                          CustomPropertyDescriptor field) {
        XmlNode attrXmlNode = field.getAnnotation(XmlNode.class);
        log.debug("要赋值的fieldName为{}", field.getName());
        final XmlTypeConvert convert = XmlTypeConverterUtil.resolve(attrXmlNode, field);
        if (!BeanUtils.setProperty(pojo, field.getName(), convert.read(element, attrName))) {
            log.debug("copy中复制{}时发生错误，属性[{}]的值将被忽略", field.getName(), field.getName());
        }
    }

    /**
     * 往pojo中指定字段设置值（字段为Collection类型）
     *
     * @param elements 要设置的数据节点
     * @param attrName 要获取的属性名，如果该值不为空则认为数据需要从属性中取而不是从节点数据中取
     * @param pojo     pojo
     * @param field    字段说明
     */
    @SuppressWarnings("unchecked")
    private void setValue(List<Element> elements, String attrName, Object pojo,
                          CustomPropertyDescriptor field) {
        XmlNode attrXmlNode = field.getAnnotation(XmlNode.class);
        log.debug("要赋值的fieldName为{}", field.getName());
        final XmlTypeConvert convert = XmlTypeConverterUtil.resolve(attrXmlNode, field);

        Class<? extends Collection> collectionClass;
        Class<? extends Collection> real = (Class<? extends Collection>) field.getRealType();

        if (attrXmlNode != null) {
            collectionClass = attrXmlNode.arrayType();
            if (!collectionClass.equals(real) && !real.isAssignableFrom(collectionClass)) {
                log.warn("用户指定的集合类型[{}]不是字段的实际集合类型[{}]的子类，使用字段的实际集合类型", collectionClass, real);
                collectionClass = real;
            }
        } else {
            collectionClass = real;
        }

        if (!StringUtils.isEmpty(attrXmlNode.arrayRoot()) && !elements.isEmpty()) {
            elements = elements.get(0).elements(attrXmlNode.arrayRoot());
        }

        //将数据转换为用户指定数据
        List<?> list = elements.stream().map(d -> convert.read(d, attrName))
            .collect(Collectors.toList());

        if (!trySetValue(list, pojo, field, collectionClass)) {
            //使用注解标记的类型赋值失败并且注解的集合类型与实际字段类型不符时尝试使用字段实际类型赋值
            if (!trySetValue(list, pojo, field, real)) {
                log.warn("无法为字段[{}]赋值", field.getName());
            }
        }
    }

    /**
     * 尝试为list类型的字段赋值
     *
     * @param datas 转换后的数据
     * @param pojo  要赋值的pojo
     * @param field 要赋值的字段说明
     * @param clazz 集合的Class对象
     * @return 返回true表示赋值成功，返回false表示赋值失败
     */
    @SuppressWarnings("unchecked")
    private boolean trySetValue(List<?> datas, Object pojo, CustomPropertyDescriptor field,
                                Class<? extends Collection> clazz) {
        log.debug("要赋值的fieldName为{}", field.getName());

        Collection collection = tryBuildCollection(clazz);
        if (collection == null) {
            log.warn("无法为class[{}]构建实例", clazz);
            return false;
        }
        collection.addAll(datas);
        try {
            return BeanUtils.setProperty(pojo, field.getName(), collection);
        } catch (Exception e) {
            log.debug("字段[{}]赋值失败，使用的集合类为[{}]", field.getName(), clazz, e);
            return false;
        }
    }

    /**
     * 根据class尝试构建出集合实例，有可能返回null
     *
     * @param clazz 集合的Class对象
     * @return 集合实例
     */
    private Collection tryBuildCollection(Class<? extends Collection> clazz) {
        if (List.class.equals(clazz) || Collection.class.equals(clazz)) {
            return new ArrayList();
        } else if (Set.class.equals(clazz)) {
            return new HashSet();
        } else if (List.class.isAssignableFrom(clazz)) {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                log.warn("指定class[{}]无法创建对象，请为其添加公共无参数构造器，将使用默认实现ArrayList", clazz, e);
                return new ArrayList();
            }
        } else if (Set.class.isAssignableFrom(clazz)) {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                log.warn("指定class[{}]无法创建对象，请为其添加公共无参数构造器，将使用默认实现HashSet", clazz, e);
                return new HashSet();
            }
        } else {
            log.warn("未知集合类型：[{}]", clazz);
            return null;
        }
    }

    @Override
    public <T> byte[] write(T t) throws SerializeException {
        return writeToString(t).getBytes();
    }

    @Override
    public <T> String writeToString(T t) throws SerializeException {
        return ExceptionWraper.convert(() -> toXml(t), SerializeException::new);
    }

    @Override
    public <T> T read(byte[] data, Class<T> clazz) throws SerializeException {
        return read(new String(data), clazz);
    }

    @Override
    public <T> T read(String data, Class<T> clazz) throws SerializeException {
        return ExceptionWraper.convert(() -> parse(data, clazz), SerializeException::new);
    }

    /**
     * XML节点数据
     */
    @Data
    @AllArgsConstructor
    private static class XmlData {
        /**
         * 节点注解，可以为空
         */
        private XmlNode  xmlNode;
        /**
         * 节点数据
         */
        private Object   data;
        /**
         * 节点数据的实际类型，可以为空
         */
        private Class<?> type;

        public XmlData(Object data) {
            this.data = data;
        }
    }
}
