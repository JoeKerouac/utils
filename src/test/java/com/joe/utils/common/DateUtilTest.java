package com.joe.utils.common;

import org.junit.Test;

/**
 * @author joe
 * @version 2018.06.01 17:53
 */
public class DateUtilTest {
    @Test
    public void doFormat() {
        String date = "2018-06-04 11:19:13";
        String format = DateUtil.BASE;
        DateUtil.getFormatDate(DateUtil.BASE , DateUtil.parse(date, format));

        date = "11:19:13";
        format = DateUtil.TIME;
        DateUtil.getFormatDate(DateUtil.BASE , DateUtil.parse(date, format));

        date = "2018-06-04";
        format = DateUtil.SHORT;
        DateUtil.getFormatDate(DateUtil.BASE , DateUtil.parse(date, format));
    }
}
