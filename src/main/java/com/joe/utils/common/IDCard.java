package com.joe.utils.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IDCard {
    private static final Logger logger = LoggerFactory.getLogger(IDCard.class);
    /**
     * 地区表
     */
    private static Map<String, String> AREA = new TreeMap<>((o1, o2) -> {
        Integer i1 = Integer.parseInt(o1);
        Integer i2 = Integer.parseInt(o2);
        return i1 - i2;
    });
    /**
     * 加权表
     */
    private static int[] POWER = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    /**
     * 加权因子
     */
    private static char[] DIVISOR = new char[]{'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    static {
        // 初始化地区
        try {
            InputStream input = IDCard.class.getClassLoader().getResourceAsStream("text");
            String areaStr = IOUtils.read(input, "UTF8");
            input.close();
            String data = areaStr.replaceAll("(\\s)+", ";");
            String[] areas = data.split(";");

            for (String str : areas) {
                String[] entity = str.split("=");
                AREA.put(entity[0], entity[1]);
            }
        } catch (Exception e) {
            logger.error("地区初始化失败", e);
        }
    }

    /**
     * 根据出生日期随机生成一个身份证号
     *
     * @param borthday 出生日期，格式为yyyyMMdd（本方法不严格验证参数准确性，只会验证长度和是否数字）
     * @return 根据指定出生日期生成的身份证号，参数格式错误时返回null
     */
    public static String create(String borthday) {
        if (!Pattern.matches("[0-9]{8}", borthday)) {
            return null;
        }
        // 随机挑出一个省市
        String card = "";
        int r1 = (int) (Math.random() * AREA.size());
        int i = 0;
        for (String befor : AREA.keySet()) {
            if (r1 == i) {
                card += befor;
                break;
            } else {
                i++;
            }
        }
        card += borthday;
        // 生成三位随机数
        int r2 = (int) (Math.random() * 899 + 100);
        card += r2;

        // 生成最后一位校验码
        byte[] idCardByte = card.getBytes();
        int sum = 0;
        for (int j = 0; j < 17; j++) {
            sum += (((int) idCardByte[j]) - 48) * POWER[j];
        }
        int mod = sum % 11;
        char calcLast = DIVISOR[mod];
        card += calcLast;
        return card;
    }

    /**
     * 检查身份证号是否符合格式
     *
     * @param idCard 身份证号
     * @return 如果身份证号符合身份证格式则返回<code>true</code>
     */
    public static boolean check(String idCard) {
        // 验证身份证格式
        Pattern pattern = Pattern.compile("[0-9]{17}[0-9|x|X]");
        Matcher matcher = pattern.matcher(idCard);
        if (!matcher.matches()) {
            // 格式不对
            logger.error("身份证格式不对{}", idCard);
            return false;
        }

        // 验证最后一位加权码
        byte[] idCardByte = idCard.getBytes();
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (((int) idCardByte[i]) - 48) * POWER[i];
        }
        int mod = sum % 11;
        int calcLast = DIVISOR[mod];
        char last;
        if (idCardByte[17] == 'x' || idCardByte[17] == 'X') {
            last = 'X';
        } else {
            last = (char) idCardByte[17];
        }
        if (last != calcLast) {
            // 格式不对
            logger.error("加权码错误{}", idCard);
            return false;
        }
        return true;
    }

    /**
     * 获取用户所属省份
     *
     * @param idCard 用户身份证号
     * @return 用户所属省份
     */
    public static String getProvince(String idCard) {
        return AREA.get(idCard.substring(0, 2) + "0000");
    }

    /**
     * 获取用户所属县市
     *
     * @param idCard 用户身份证号
     * @return 用户所属县市
     */
    public static String getArea(String idCard) {
        // 用户所属省份
        String province;
        // 用户所属地区
        String area;
        if (AREA.get(idCard.substring(0, 6)) == null) {
            logger.warn("地区不存在或者地区已不在最新行政区划代码中");
        }

        // 判断是否输入台湾省和特别行政区
        if (idCard.substring(0, 6).equals("710000") || idCard.substring(0, 6).equals("810000")
                || idCard.substring(0, 6).equals("820000")) {
            // 台湾省和特别行政区
            area = AREA.get(idCard.substring(0, 6) + "0000");
        } else {
            // 查询用户所属省份
            province = AREA.get(idCard.substring(0, 2) + "0000");
            // 判断用户所属地区是否是直辖市
            Pattern areaP = Pattern.compile("(.*)?市");
            Matcher areamM = areaP.matcher(province);
            StringBuilder sb = new StringBuilder();
            if (!areamM.matches()) {
                // 不是直辖市，加上用户所属市区名
                sb.append(AREA.get(idCard.substring(0, 4) + "00"));
            }

            sb.append(AREA.get(idCard.substring(0, 6)));
            // 用户的地址
            area = sb.toString();
        }
        return area;
    }

    /**
     * 获取用户性别，0是女，1是男
     *
     * @param idCard 用户身份证号
     * @return 用户性别
     */
    public static int getSex(String idCard) {
        // 判断身份证用户性别
        int sexInt = Integer.parseInt(idCard.substring(16, 17));
        return sexInt % 2;
    }

    /**
     * 获取用户年龄
     *
     * @param idCard 用户身份证号
     * @return 用户年龄
     */
    public static int getAge(String idCard) {
        int age;
        // 计算用户年龄
        int year = Integer.parseInt(idCard.substring(6, 10));
        int month_day = Integer.parseInt(idCard.substring(10, 14));
        Calendar calendar = Calendar.getInstance();
        int now_year = calendar.get(Calendar.YEAR);
        int now_month_day = calendar.get(Calendar.MONTH) * 100 + calendar.get(Calendar.DATE);
        if (now_month_day > month_day) {
            age = now_year - year;
        } else {
            age = now_year - year - 1;

        }
        if (age < 0) {
            logger.warn("身份证年龄应该大于等于0，但是实际年龄为{}", age);
        }
        return age;
    }
}
