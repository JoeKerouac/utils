package com.joe.utils.parse.xml;

import com.joe.utils.common.BeanUtils;
import com.joe.utils.common.BeanUtils.CustomPropertyDescriptor;
import com.joe.utils.common.StringUtils;
import com.joe.utils.parse.xml.converter.XmlTypeConverterUtil;
import com.joe.utils.type.ReflectUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * XML解析（反向解析为java对象时不区分大小写），该解析器由于大量使用反射，所以在第一次解析
 * 某个类型的对象时效率较低，解析过一次系统会自动添加缓存，速度将会大幅提升（测试中提升了25倍）
 *
 * @author Administrator
 */
public class XmlParser {
    private static final Logger logger = LoggerFactory.getLogger(XmlParser.class);
    private static final XmlParser xmlParser = new XmlParser();

    public static XmlParser getInstance() {
        return xmlParser;
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
            Document document = DocumentHelper.parseText(xml);
            Element root = document.getRootElement();
            if (root.elements().size() == 0) {
                Map<String, Object> map = new HashMap<>();
                map.put(root.getName(), root.getText());
                return map;
            } else {
                return (HashMap<String, Object>) parse(root);
            }
        } catch (Exception e) {
            logger.error("xml格式不正确", e);
            return null;
        }
    }

    /**
     * 将XML解析为POJO对象，暂时无法解析map，当需要解析的字段是{@link java.util.Collection}的子类时必须带有注
     * 解{@link com.joe.utils.parse.xml.XmlNode}，否则将解析失败。
     * <p>
     * PS：对象中的集合字段必须添加注解，必须是简单集合，即集合中只有一种数据类型，并且当类型不是String时需要指定
     * converter，否则将会解析失败。
     *
     * @param xml   XML源
     * @param clazz POJO对象的class
     * @param <T>   POJO的实际类型
     * @return 解析结果
     */
    public <T extends Object> T parse(String xml, Class<T> clazz) {
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
            logger.error("class对象生成失败，请检查代码；失败原因：", e);
            throw new RuntimeException(e);
        }

        // 解析XML
        try {
            document = DocumentHelper.parseText(xml);
        } catch (Exception e) {
            logger.error("xml解析错误", e);
            return null;
        }

        // 获取pojo对象的说明
        CustomPropertyDescriptor[] propertyDescriptor = BeanUtils.getPropertyDescriptors(clazz);
        Element root = document.getRootElement();
        for (CustomPropertyDescriptor descript : propertyDescriptor) {
            XmlNode xmlNode = descript.getAnnotation(XmlNode.class);
            final String fieldName = descript.getName().toLowerCase();
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
                logger.debug("字段[{}]对应的节点名为：{}", fieldName, nodeName);

                //判断节点是否是属性值
                if (xmlNode.isAttribute()) {
                    //如果节点是属性值，那么需要同时设置节点名和属性名，原则上如果是属性的话必须设置节点名，但是为了防止
                    //用户忘记设置，在用户没有设置的时候使用字段名
                    if (StringUtils.isEmpty(xmlNode.attributeName())) {
                        logger.warn("字段[{}]是属性值，但是未设置属性名（attributeName字段），将采用字段名作为属性名", descript.getName());
                        attributeName = fieldName;
                    } else {
                        attributeName = xmlNode.attributeName();
                    }
                    if (StringUtils.isEmpty(xmlNode.name())) {
                        logger.debug("该字段是属性值，并且未设置节点名（name字段），设置isParent为true");
                        isParent = true;
                    }
                } else {
                    logger.debug("字段[{}]对应的是节点");
                }
            } else {
                ignore = true;
            }

            if (!ignore) {
                //获取指定节点名的element
                List<Element> nodes = (!StringUtils.isEmpty(attributeName) && isParent) ? Collections.singletonList
                        (root) : root.elements(nodeName);
                //判断是否为空
                if (nodes.isEmpty()) {
                    //如果为空那么将首字母大写后重新获取
                    nodes = root.elements(StringUtils.toFirstUpperCase(nodeName));
                }
                if (!nodes.isEmpty()) {
                    //如果还不为空，那么为pojo赋值
                    Class<?> type = descript.getType();

                    //开始赋值
                    //判断字段是否是集合
                    if (Collection.class.isAssignableFrom(type)) {
                        //是集合
                        setValue(nodes, attributeName, pojo, descript);
                    } else if (Map.class.isAssignableFrom(type)) {
                        //是Map
                        logger.warn("当前暂时不支持解析map");
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
     * 将Object解析为xml，字段值为null的将不包含在xml中，暂时只能解析基本类型（可以正确解析list、map）
     *
     * @param source   bean
     * @param rootName 根节点名称
     * @param hasNull  是否包含null元素（true：包含）
     * @return 解析结果
     */
    public String toXml(Object source, String rootName, boolean hasNull) {
        if (source == null) {
            logger.warn("传入的source为null，返回null");
            return null;
        }
        Long start = System.currentTimeMillis();
        Element root = DocumentHelper.createElement(rootName);
        buildDocument(root, source, source.getClass(), !hasNull);
        Long end = System.currentTimeMillis();
        logger.info("解析xml用时" + (end - start) + "ms");
        return root.asXML();
    }

    /**
     * 根据pojo构建xml的document（方法附件参考附件xml解析器思路）
     *
     * @param parent     父节点
     * @param pojo       pojo
     * @param ignoreNull 是否忽略空元素
     */
    private void buildDocument(Element parent, Object pojo, Class<?> clazz, boolean ignoreNull) {
        CustomPropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(clazz == null ? pojo
                .getClass() : clazz);

        for (CustomPropertyDescriptor descriptor : propertyDescriptors) {
            XmlNode xmlNode = descriptor.getAnnotation(XmlNode.class);
            //字段值
            Object attrValueObj = pojo == null ? null : BeanUtils.getProperty(pojo, descriptor.getName());
            //判断是否忽略
            if ((ignoreNull && attrValueObj == null) || (xmlNode != null && xmlNode.ignore())) {
                logger.debug("忽略空节点或者节点被注解忽略");
                continue;
            }

            //节点名
            String nodeName = (xmlNode == null || StringUtils.isEmpty(xmlNode.name())) ? descriptor.getName() :
                    xmlNode.name();
            //属性名
            String attrName = (xmlNode == null || StringUtils.isEmpty(xmlNode.attributeName())) ? descriptor.getName() :
                    xmlNode.attributeName();
            //是否是cdata
            boolean isCDATA = xmlNode == null ? false : xmlNode.isCDATA();

            //判断字段对应的是否是属性
            if (xmlNode != null && xmlNode.isAttribute()) {
                //属性值，属性值只能是简单值
                String attrValue = attrValueObj == null ? "" : String.valueOf(attrValueObj);
                Element node;
                //判断是否是父节点的属性
                if (StringUtils.isEmpty(xmlNode.name())) {
                    node = parent;
                } else {
                    //属性不是根节点的属性，先尝试从现有节点中搜索
                    node = parent.element(nodeName);
                    if (node == null) {
                        //搜索不到，创建一个
                        node = DocumentHelper.createElement(nodeName);
                        parent.add(node);
                    }
                }
                //为属性对应的节点添加属性
                node.addAttribute(attrName, attrValue);
                continue;
            }

            //判断是否是简单类型或者集合类型
            if (ReflectUtil.isSimple(descriptor.getRealType())) {
                //是简单类型或者集合类型
                if (Map.class.isAssignableFrom(descriptor.getRealType())) {
                    logger.warn("xml解析器不能处理map类型，该类型将被忽略");
                    continue;
                } else if (Collection.class.isAssignableFrom(descriptor.getRealType())) {
                    //集合类型
                    //判断字段值是否为null
                    if (attrValueObj != null) {
                        Collection collection = (Collection) attrValueObj;
                        collection.stream().forEach(obj -> {
                            Element node = DocumentHelper.createElement(nodeName);
                            parent.add(node);
                            buildDocument(node, obj, null, ignoreNull);
                        });
                    }
                } else {
                    Element node = parent.element(nodeName);
                    if (node == null) {
                        //搜索不到，创建一个
                        node = DocumentHelper.createElement(nodeName);
                        parent.add(node);
                    }

                    String text = attrValueObj == null ? "" : String.valueOf(attrValueObj);
                    if (isCDATA) {
                        logger.debug("内容[{}]需要CDATA标签包裹", text);
                        node.add(DocumentHelper.createCDATA(text));
                    } else {
                        node.setText(text);
                    }
                }
            } else {
                Element node = DocumentHelper.createElement(nodeName);
                parent.add(node);

                //猜测字段类型（防止字段的声明是一个接口，优先采用xmlnode中申明的类型）
                Class<?> type = resolveRealType(descriptor);

                //pojo类型
                buildDocument(node, attrValueObj, type, ignoreNull);
            }
        }
    }

    /**
     * 确定字段的真实类型
     *
     * @param descriptor 字段说明
     * @return 字段实际类型而不是接口或者抽象类
     */
    private Class<?> resolveRealType(CustomPropertyDescriptor descriptor) {
        XmlNode xmlNode = descriptor.getAnnotation(XmlNode.class);
        //猜测字段类型（防止字段的声明是一个接口，优先采用xmlnode中申明的类型）
        Class<?> type = (xmlNode == null || xmlNode.general() == null) ? descriptor.getRealType() : xmlNode
                .general();

        if (!descriptor.getRealType().isAssignableFrom(type) && !descriptor.getRealType().equals(type)) {
            type = descriptor.getRealType();
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
                    if (obj != null && obj instanceof List) {
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
    private void setValue(Element element, String attrName, Object pojo, CustomPropertyDescriptor field) {
        XmlNode attrXmlNode = field.getAnnotation(XmlNode.class);
        logger.debug("要赋值的fieldName为{}", field.getName());
        final XmlTypeConvert convert = XmlTypeConverterUtil.resolve(attrXmlNode, field);
        if (!BeanUtils.setProperty(pojo, field.getName(), convert.read(element, attrName))) {
            logger.warn("copy中复制{}时发生错误，属性[{}]的值将被忽略", field.getName(), field.getName());
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
    private void setValue(List<Element> elements, String attrName, Object pojo, CustomPropertyDescriptor field) {
        XmlNode attrXmlNode = field.getAnnotation(XmlNode.class);
        logger.debug("要赋值的fieldName为{}", field.getName());
        final XmlTypeConvert convert = XmlTypeConverterUtil.resolve(attrXmlNode, field);

        Class<? extends Collection> collectionClass;

        if (attrXmlNode != null) {
            collectionClass = attrXmlNode.arrayType();
        } else {
            collectionClass = (Class<? extends Collection>) field.getType();
        }

        //将数据转换为用户指定数据
        List<?> list = elements.stream().map(d -> {
            return convert.read(d, attrName);
        }).collect(Collectors.toList());

        if (!trySetValue(list, pojo, field, collectionClass) && !collectionClass.equals(field.getType())) {
            //使用注解标记的类型赋值失败并且注解的集合类型与实际字段类型不符时尝试使用字段实际类型赋值
            if (!trySetValue(list, pojo, field, (Class<? extends Collection>) field.getType())) {
                logger.warn("无法为字段[{}]赋值", field.getName());
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
    private boolean trySetValue(List<?> datas, Object pojo, CustomPropertyDescriptor field, Class<? extends
            Collection> clazz) {
        logger.debug("要赋值的fieldName为{}", field.getName());

        Collection collection = tryBuildCollection(clazz);
        if (collection == null) {
            logger.warn("无法为class[{}]构建实例", clazz);
            return false;
        }
        collection.addAll(datas);
        try {
            return BeanUtils.setProperty(pojo, field.getName(), collection);
        } catch (Exception e) {
            logger.debug("字段[{}]赋值失败，使用的集合类为[{}]", field.getName(), clazz, e);
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
        if (clazz.equals(List.class)) {
            return new ArrayList();
        } else if (clazz.equals(Set.class)) {
            return new HashSet();
        } else if (List.class.isAssignableFrom(clazz)) {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                logger.warn("指定class[{}]无法创建对象，请为其添加公共无参数构造器，将使用默认实现ArrayList", clazz, e);
                return new ArrayList();
            }
        } else if (Set.class.isAssignableFrom(clazz)) {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                logger.warn("指定class[{}]无法创建对象，请为其添加公共无参数构造器，将使用默认实现HashSet", clazz, e);
                return new HashSet();
            }
        } else {
            return null;
        }
    }
}
