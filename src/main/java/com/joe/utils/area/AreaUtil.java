package com.joe.utils.area;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import com.joe.utils.collection.CollectionUtil;
import com.joe.utils.common.Assert;
import com.joe.utils.common.FileUtils;
import com.joe.utils.common.IDCard;
import com.joe.utils.common.IOUtils;
import com.joe.utils.common.string.StringUtils;
import com.joe.utils.serialize.json.JsonParser;

/**
 * 地区工具
 *
 * @author JoeKerouac
 * @version 2019年10月10日 15:36
 */
public class AreaUtil {

    /**
     * 所有年份的区域集合，key是年份，value是当年最新区域集合
     */
    private static TreeMap<String, Map<String, Area>> ALL_AREA_MAP = new TreeMap<>();

    /**
     * json解析
     */
    private static final JsonParser                   JSON_PARSER  = JsonParser.getInstance();

    static {
        init();
    }

    /**
     * 清除缓存
     */
    public static void clearCache() {
        ALL_AREA_MAP.clear();
    }

    /**
     * 获取区域说明
     * @param code 区域代码
     * @param date 日期，因为同一个代码在不同时期可能对应的区域名不同，所以需要传入日期
     * @return 区域说明
     */
    public static Area getArea(String code, String date) {
        Assert.notBlank(code);
        Area area = getArea(date).get(code);
        if (area == null) {
            return null;
        }
        // deep copy
        return JSON_PARSER.read(JSON_PARSER.toJson(area), Area.class);
    }

    /**
     * 获取指定时间最新的区域说明
     * @param date 日期，因为同一个代码在不同时期可能对应的区域名不同，所以需要传入日期
     * @return 区域说明
     */
    public static Map<String, Area> getArea(String date) {
        if (ALL_AREA_MAP.isEmpty()) {
            init();
        }
        Assert.notBlank(date);
        Assert.isTrue(Pattern.matches("[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}", date), "日期格式不对");

        String descDate = ALL_AREA_MAP.firstKey();

        // 计算当前时间下最新的区划代码
        for (String arg : ALL_AREA_MAP.keySet()) {
            if (arg.compareTo(date) >= 0) {
                break;
            }
            descDate = arg;
        }

        if (StringUtils.isEmpty(descDate)) {
            return Collections.emptyMap();
        }

        // deep copy
        return JSON_PARSER.readAsMap(JSON_PARSER.toJson(ALL_AREA_MAP.get(descDate)), Map.class,
            String.class, Area.class);
    }

    /**
     * 获取指定区域所在的省份
     * @param area 区域
     * @param areaMap 所有区域集合
     * @return 区域所在的省份，有可能为空，为空说明区域集合不全或者区域有问题
     */
    public static Area getProvince(Area area, Map<String, Area> areaMap) {
        Assert.notNull(area);
        Assert.notNull(areaMap);
        while (area != null && !Area.DEFAULT.equals(area.getParent())) {
            area = areaMap.get(area.getParent());
        }
        return area;
    }

    /**
     * 获取指定区域全称
     * @param area 区域
     * @param areaMap 区域集合
     * @return 区域全称
     */
    public static String getFullName(Area area, Map<String, Area> areaMap) {
        Assert.notNull(area);
        Assert.notNull(areaMap);
        StringBuilder name = new StringBuilder();
        while (area != null) {
            name.insert(0, area.getName());
            area = areaMap.get(area.getParent());
        }
        return name.toString();
    }

    /**
     * 初始化
     */
    private static void init() {
        try {
            URL areaDir = IDCard.class.getClassLoader().getResource("area");
            if (areaDir == null) {
                throw new NullPointerException("没有找到区域文件");
            }
            File file = new File(areaDir.getFile());
            List<File> areaFiles = FileUtils.findAllFile(file);

            for (File areaFile : areaFiles) {
                Map<String, Area> map = read(areaFile);
                ALL_AREA_MAP.put(areaFile.getName(), map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从文件中读取区域信息
     * @param areaFile 文件
     * @return 文件中的区域信息
     * @throws IOException IO异常
     */
    private static Map<String, Area> read(File areaFile) throws IOException {
        String json = IOUtils.read(areaFile, "UTF8");
        Map<String, Area> map = JSON_PARSER.readAsMap(json, Map.class, String.class, Area.class);
        Map<String, Area> allMap = new HashMap<>();
        map.values().forEach(area -> putArea(allMap, area));
        return allMap;
    }

    /**
     * 将区域放入所有区域map，同时递归将该区域的子区域也放入
     * @param areaMap 区域map
     * @param area 区域
     */
    private static void putArea(Map<String, Area> areaMap, Area area) {
        areaMap.put(area.getCode(), area);
        if (!CollectionUtil.safeIsEmpty(area.getChildList())) {
            area.getChildList().forEach(childArea -> putArea(areaMap, childArea));
        }
    }
}
