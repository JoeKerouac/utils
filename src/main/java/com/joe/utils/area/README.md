## 说明
本目录下是行政区划，文件名是截止日期，内容是截止日期时的行政区划，json格式；
数据来源：
http://www.mca.gov.cn/article/sj/xzqh/2019/

相关解析代码，注意，需要引入jsoup依赖和Spider依赖
```java
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.joe.utils.common.Assert;
import com.joe.utils.common.IOUtils;
import com.joe.utils.common.string.StringUtils;
import com.joe.utils.pattern.PatternUtils;
import com.joe.utils.serialize.json.JsonParser;

import lombok.Data;

/**
 * 将行政区划代码保存到本地
 * 
 * @author JoeKerouac
 * @version 2019年10月10日 16:59
 */
public class Main {

    private static final String     BASE_DIR    = "/Users/joekerouac/workspace/OneDrive/code/resource/area/";

    private static final Spider     SPIDER      = new Spider(100);

    private static final JsonParser JSON_PARSER = JsonParser.getInstance();

    public static void main(String[] args) {
        Assert.notBlank(BASE_DIR);
        Map<String, String> map = new HashMap<>();

        map.put("1980.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708040959.html");
        map.put("1981.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708041004.html");
        map.put("1982.12.31",
            "http://www.mca.gov.cn/article/sj/tjbz/a/1980-2000/201707141125.html");
        map.put("1983.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708160821.html");
        map.put("1984.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220856.html");
        map.put("1985.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220858.html");
        map.put("1986.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220859.html");
        map.put("1987.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220902.html");
        map.put("1988.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220903.html");
        map.put("1989.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708041017.html");
        map.put("1990.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708041018.html");
        map.put("1991.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708041020.html");
        map.put("1992.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220910.html");
        map.put("1993.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708041023.html");
        map.put("1994.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220911.html");
        map.put("1995.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220913.html");
        map.put("1996.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220914.html");
        map.put("1997.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220916.html");
        map.put("1998.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220918.html");
        map.put("1999.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220921.html");
        map.put("2000.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220923.html");
        map.put("2001.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220925.html");
        map.put("2002.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220927.html");
        map.put("2003.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220928.html");
        map.put("2004.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220930.html");
        map.put("2005.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220935.html");
        map.put("2006.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220936.html");
        map.put("2007.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220939.html");
        map.put("2008.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220941.html");
        map.put("2009.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220943.html");
        map.put("2010.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220946.html");
        map.put("2011.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201707271552.html");
        map.put("2012.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201707271556.html");
        map.put("2013.12.31", "http://files2.mca.gov.cn/cws/201404/20140404125552372.htm");
        map.put("2014.12.31", "http://files2.mca.gov.cn/cws/201502/20150225163817214.html");
        map.put("2015.12.31", "http://www.mca.gov.cn/article/sj/tjbz/a/2015/201706011127.html");
        map.put("2016.12.31",
            "http://www.mca.gov.cn/article/sj/xzqh/1980/201705/201705311652.html");
        map.put("2017.12.31",
            "http://www.mca.gov.cn/article/sj/xzqh/1980/201803/201803131454.html");
        map.put("2018.12.31",
            "http://www.mca.gov.cn/article/sj/xzqh/1980/201903/201903011447.html");
        map.put("2019.06.01",
            "http://www.mca.gov.cn/article/sj/xzqh/2019/201901-06/201908050812.html");

        map.forEach((fileName, url) -> {
            try {
                SPIDER.addTask(url, html -> {
                    Map<String, String> codeMap = parse(html);
                    if (codeMap.size() == 0) {
                        System.out.println("从URL[" + url + "]处没有获取到数据");
                        return;
                    }
                    Map<String, Area> allArea = new HashMap<>();

                    codeMap.forEach((code, name) -> {
                        Area area = new Area();
                        allArea.put(code, area);

                        area.setCode(code);
                        area.setName(name);

                        if (code.endsWith("0000")) {
                            area.setParent("000000");
                        } else if (code.endsWith("00")) {
                            String parent = code.substring(0, 2) + "0000";
                            if (codeMap.containsKey(parent)) {
                                area.setParent(parent);
                            } else {
                                area.setParent("000000");
                            }
                        } else {
                            String parent;

                            if (codeMap.containsKey(parent = code.substring(0, 4) + "00")) {
                                area.setParent(parent);
                            } else if (codeMap
                                .containsKey(parent = code.substring(0, 2) + "0000")) {
                                area.setParent(parent);
                            } else {
                                area.setParent("000000");
                            }
                        }
                    });

                    allArea.forEach((code, area) -> {
                        if (!area.getParent().equals("000000")) {
                            Area parent = allArea.get(area.getParent());
                            List<Area> childList = parent.getChildList();
                            if (childList == null) {
                                childList = new ArrayList<>();
                                parent.setChildList(childList);
                            }
                            childList.add(area);
                        }
                    });

                    List<String> remove = allArea.values().stream()
                        .filter(area -> !area.getParent().equals("000000")).map(Area::getCode)
                        .collect(Collectors.toList());
                    remove.forEach(allArea::remove);

                    String data = JSON_PARSER.toJson(allArea);
                    // 持久化保存
                    try {
                        System.out.println("保存：" + BASE_DIR + fileName);
                        IOUtils.saveAsFile(data, "UTF8", BASE_DIR + fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 从网页解析区划代码
     * @param html 网页
     * @return 区划代码集合
     */
    private static Map<String, String> parse(String html) {
        // code和name的映射
        Map<String, String> codeMap = new HashMap<>();
        boolean start = false;
        int codeIndex = -1;
        int nameIndex = -1;
        Document doc = Jsoup.parse(html);
        Element element = doc.getElementsByTag("tbody").get(0);
        Elements trs = element.getElementsByTag("tr");

        // 遍历解析
        for (Element tr : trs) {
            Elements tds = tr.getElementsByTag("td");

            if (start) {
                // 先进行越界判断
                if (tds.size() > codeIndex && tds.size() > nameIndex) {
                    String code = tds.get(codeIndex).text();
                    String name = tds.get(nameIndex).text();
                    // code必须是6位数字，并且名字不能为空
                    if (code.trim().length() == 6 && PatternUtils.isNumberStr(code)
                        && StringUtils.isNotEmpty(name)) {
                        codeMap.put(code.trim(), name.trim());
                    }
                }
            } else {
                for (int i = 0; i < tds.size(); i++) {
                    Element td = tds.get(i);
                    if (td.text().equals("行政区划代码")) {
                        codeIndex = i;
                        start = true;
                    } else if (td.text().equals("单位名称") || td.text().equals("行政区划名称")) {
                        // 老版本的叫单位名称，新版的叫行政区划名称
                        nameIndex = i;
                        start = true;
                    }
                }
            }
        }
        return codeMap;
    }

    @Data
    static class Area {

        /**
         * 当前地区代码
         */
        private String     code;

        /**
         * 父地区代码
         */
        private String     parent;

        /**
         * 地区名
         */
        private String     name;

        /**
         * 子地区
         */
        private List<Area> childList;
    }
}

```