package com.joe.utils.common;

import org.junit.Test;

/**
 * @author joe
 * @version 2018.06.01 17:53
 */
public class DateUtilTest {
    @Test
    public void doFormat() {
        String date = "2018-06-04 11:19";
        String format = "yyyy-MM-dd HH:mm";
        DateUtil.getFormatDate(DateUtil.BASE , DateUtil.parse(date, format));

        date = "11:19";
        format = "HH:mm";
        DateUtil.getFormatDate(DateUtil.BASE , DateUtil.parse(date, format));

        date = "2018-06-04";
        format = "yyyy-MM-dd";
        DateUtil.getFormatDate(DateUtil.BASE , DateUtil.parse(date, format));
    }
}
