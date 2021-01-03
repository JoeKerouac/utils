package com.joe.utils.common;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author JoeKerouac
 * @version 2019年10月10日 19:05
 */
public class IDCardTest {

    /**
     * 预设身份证
     */
    private static final String ID_CARD = "362528199002168308";

    /**
     * 预设身份证的省份
     */
    private static final String PROVINCE = "江西省";

    /**
     * 预设身份证的地区
     */
    private static final String AREA = "江西省抚州地区金溪县";

    @Test
    public void doTest() {
        String idCard = IDCard.create("19900216");
        Assert.assertNotNull(idCard);

        // 使用预设的身份证
        idCard = ID_CARD;
        // 年龄
        int age;

        Calendar calendar = Calendar.getInstance();
        int nowYear = calendar.get(Calendar.YEAR);
        int nowMonthDay = calendar.get(Calendar.MONTH) * 100 + calendar.get(Calendar.DATE);

        if (nowMonthDay > 216) {
            age = nowYear - 1990 + 1;
        } else {
            age = nowYear - 1990;
        }

        Assert.assertTrue(IDCard.check(idCard));
        Assert.assertEquals(IDCard.getProvince(idCard), PROVINCE);
        Assert.assertEquals(IDCard.getArea(idCard), AREA);
        Assert.assertEquals(IDCard.getBirthday(idCard), "19900216");
        Assert.assertEquals(IDCard.getSex(idCard), 0);
        Assert.assertEquals(IDCard.getAge(idCard), age);
    }
}
