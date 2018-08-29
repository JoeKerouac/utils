package com.joe.utils.common;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.08.29 11:47
 */
public class StringUtilsTest {
    @Test
    public void doCopy() {
        Assert.assertTrue("***".equals(StringUtils.copy("*", 3)));
    }

    @Test
    public void doFormat() {
        Assert.assertTrue("你好啊:JoeKerouac".equals(StringUtils.format("你好啊:{}", "JoeKerouac")));
    }

    @Test
    public void doReplaceBefor() {
        Assert.assertEquals("***Kerouac", StringUtils.replaceBefor("JoeKerouac", 2, "***"));
    }

    @Test
    public void doReplaceAfter() {
        Assert.assertEquals("Joe*******", StringUtils.replaceAfter("JoeKerouac", 3, "*******"));
    }

    @Test
    public void doHasLength() {
        Assert.assertTrue(StringUtils.hasLength(" "));
        Assert.assertFalse(StringUtils.hasLength(""));
    }

    @Test
    public void doTrim() {
        Assert.assertEquals("123", StringUtils.trim("***123**", "*"));
    }

    @Test
    public void doParseForm() {
        String data = "name=JoeKerouac&age=24";
        Map<String, String> map = StringUtils.parseForm(data);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("JoeKerouac", map.get("name"));
        Assert.assertEquals("24", map.get("age"));
    }

    @Test
    public void doIsEmpty() {
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertTrue(StringUtils.isEmpty("  "));
        Assert.assertFalse(StringUtils.isEmpty("1"));
    }

    @Test
    public void doIsEmptyAny() {
        Assert.assertTrue(StringUtils.isEmptyAny("", "123"));
        Assert.assertTrue(StringUtils.isEmptyAny("  ", "123"));
        Assert.assertFalse(StringUtils.isEmptyAny("1", "123"));
    }

    @Test
    public void doIsEmptyAll() {
        Assert.assertFalse(StringUtils.isEmptyAll("", "123"));
        Assert.assertFalse(StringUtils.isEmptyAll("  ", "123"));
        Assert.assertFalse(StringUtils.isEmptyAll("1", "123"));
        Assert.assertTrue(StringUtils.isEmptyAll("", "  "));
    }

    @Test
    public void doIsNumber() {
        Assert.assertTrue(StringUtils.isNumber("123"));
        Assert.assertTrue(StringUtils.isNumber("-123"));
        Assert.assertTrue(StringUtils.isNumber("123.1"));
        Assert.assertTrue(StringUtils.isNumber("-123.1"));
        Assert.assertTrue(StringUtils.isNumber("123.0"));
        Assert.assertTrue(StringUtils.isNumber("-123.0"));
        Assert.assertTrue(StringUtils.isNumber("123.100"));
        Assert.assertTrue(StringUtils.isNumber("-123.100"));
        Assert.assertTrue(StringUtils.isNumber("0.100"));
        Assert.assertTrue(StringUtils.isNumber("-0.100"));

        Assert.assertFalse(StringUtils.isNumber("0123"));
        Assert.assertFalse(StringUtils.isNumber("-0123"));
        Assert.assertFalse(StringUtils.isNumber("0123.1"));
        Assert.assertFalse(StringUtils.isNumber("-0123.1"));
        Assert.assertFalse(StringUtils.isNumber("0123.0"));
        Assert.assertFalse(StringUtils.isNumber("-0123.0"));
        Assert.assertFalse(StringUtils.isNumber("0123.100"));
        Assert.assertFalse(StringUtils.isNumber("-0123.100"));
        Assert.assertFalse(StringUtils.isNumber("00.100"));
        Assert.assertFalse(StringUtils.isNumber("-00.100"));

        Assert.assertFalse(StringUtils.isNumber("123a"));
        Assert.assertFalse(StringUtils.isNumber("12a3"));
        Assert.assertFalse(StringUtils.isNumber("a123"));
    }

    @Test
    public void doToFirstUpperCase() {
        Assert.assertEquals("JoeKerouac", StringUtils.toFirstUpperCase("joeKerouac"));
    }

    @Test
    public void doLcs() {
        Assert.assertEquals(3, StringUtils.lcs("123456", "456789"));
        Assert.assertEquals(3, StringUtils.lcs("123456", "256789"));
        Assert.assertEquals(2, StringUtils.lcs("123456", "556489"));
    }
}
