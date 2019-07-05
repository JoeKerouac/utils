package com.joe.utils.common.string;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.08.29 11:47
 */
public class StringUtilsTest {

    @Test
    public void doPatternCollect0() {
        String data = "#123#,#456#,#789#";
        List<String> result = StringUtils.patternCollect(data, ".*?#([0-9]*)#.*?");
        List<String> expectResult = Arrays.asList("123", "456", "789");

        Assert.assertArrayEquals(expectResult.toArray(), result.toArray());
    }

    @Test
    public void doPatternCollect1() {
        String data = "#123#,#456#,#789#";
        List<String> result = StringUtils.patternCollect(data, ".*?#([0-9]*)#.*?",
            args -> args.get(0));
        List<String> expectResult = Arrays.asList("123", "456", "789");

        Assert.assertArrayEquals(expectResult.toArray(), result.toArray());
    }

    @Test
    public void doPatternCollect2() {
        String data = "#123#,#456#,#789#";
        List<String> result = StringUtils.patternCollect(data, ".*?#([0-9]*)#.*?", new int[] { 1 });
        List<String> expectResult = Arrays.asList("123", "456", "789");

        Assert.assertArrayEquals(expectResult.toArray(), result.toArray());
    }

    @Test
    public void doPatternCollect3() {
        String data = "#123#,#456#,#789#";
        List<String> result = StringUtils.patternCollect(data, ".*?#([0-9]*)#.*?", new int[] { 1 });
        List<String> expectResult = Arrays.asList("123", "456", "789");

        Assert.assertArrayEquals(expectResult.toArray(), result.toArray());
    }

    @Test
    public void doPatternCollect4() {
        String data = "#123#,#456#,#789#";
        List<Integer> result = StringUtils.patternCollect(data, ".*?#([0-9]*)#.*?", new int[] { 1 },
            args -> Integer.parseInt(args.get(0)));
        List<Integer> expectResult = Arrays.asList(123, 456, 789);

        Assert.assertArrayEquals(expectResult.toArray(), result.toArray());
    }

    @Test
    public void doPatternCollect5() {
        String data = "#123,12#,#456,45#,#789,78#";
        List<Integer> result = StringUtils.patternCollect(data, ".*?#([0-9]*),([0-9]*)#.*?",
            new int[] { 1, 2 },
            args -> Integer.parseInt(args.get(0)) * Integer.parseInt(args.get(1)));
        List<Integer> expectResult = Arrays.asList(123 * 12, 456 * 45, 789 * 78);

        Assert.assertArrayEquals(expectResult.toArray(), result.toArray());
    }

    @Test
    public void patternCollectAndReduce() {
        String data = "#123#,#456#,#789#";
        String result = StringUtils.patternCollectAndReduce(data, ".*?#([0-9]*)#.*?", ",");
        String expectResult = "123,456,789";

        Assert.assertEquals(expectResult, result);
    }

    @Test
    public void doCopy() {
        Assert.assertTrue("***".equals(StringUtils.copy("*", 3)));
    }

    @Test
    public void doFormat() {
        Assert.assertTrue("你好啊:JoeKerouac".equals(StringUtils.format("你好啊:{0}", "JoeKerouac")));
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
