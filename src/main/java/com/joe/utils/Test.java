package com.joe.utils;

import com.joe.utils.parse.xml.XmlNode;
import com.joe.utils.parse.xml.XmlParser;
import lombok.Data;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Collections;
import java.util.List;

/**
 * @author joe
 * @version 2018.01.30 14:00
 */
public class Test {
    public static void main(String[] args) {
//        String buildRootName = "abc";
//        Element root = DocumentHelper.createElement(buildRootName);
//        Element e1 = DocumentHelper.createElement("a");
//        e1.addText("abc");
//        Element e2 = DocumentHelper.createElement("a");
//        e2.addText("def");
//        root.add(e1);
//        root.add(e2);
//        System.out.println(root.asXML());
//        System.exit(0);


        XmlParser parser = XmlParser.getInstance();
        A a = new A();
        B b = new B();
        a.b = Collections.singletonList(b);
        a.c = true;
        a.d = "你好啊";
        a.e = "这是一个A的属性";
        b.ba = "这是ba";
        b.bb = "这是bb";
        b.bba = 189;


        long l1 = System.currentTimeMillis();
        System.out.println(parser.parse(parser.toXml(a, "root", true), A.class));
        long l2 = System.currentTimeMillis();
        System.out.println(parser.parse(parser.toXml(a, "root", true), A.class));
        long l3 = System.currentTimeMillis();

        System.out.println("第一次耗时：" + (l2 - l1) + "毫秒");
        System.out.println("第二次耗时：" + (l3 - l2) + "毫秒");


//        String xml = "<root>\n" +
//                "\t<a e=\"123\">123</a>\n" +
//                "\t<b bba=\"123\" ><ba>this is ba</ba><bb>this is bb</bb></b>\n" +
//                "\t<b bba=\"234\" ><ba>这是第二个</ba><bb>这是第二个</bb></b>\n" +
//                "\t<c>true</c>\n" +
//                "\t<d>b</d>\n" +
//                "\t<c ca=\"1\" cb=\"true\">c</c>\n" +
//                "</root>";

//
//        System.out.println(a);
//        System.out.println(a.b.size());
    }


    @Data
    public static class A {
        @XmlNode(general = B.class)
        private List<B> b;
        private boolean c;
        private String d;
        @XmlNode(isAttribute = true, name = "a")
        private String e;
    }

    @Data
    public static class B {
        private String ba;
        private String bb;
        @XmlNode(isAttribute = true)
        private int bba;
    }
}
