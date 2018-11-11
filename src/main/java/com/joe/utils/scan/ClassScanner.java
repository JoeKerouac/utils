package com.joe.utils.scan;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joe.utils.reflect.ClassUtils;

/**
 * Class类扫描
 *
 * @author joe
 */
public class ClassScanner implements Scanner<Class<?>, ClassFilter> {
    private static final Logger                   logger          = LoggerFactory
        .getLogger(ClassScanner.class);
    private static Map<ClassLoader, ClassScanner> classScannerMap = new ConcurrentHashMap<>();

    /**
     * 用于加载class
     */
    private ClassLoader                           classLoader;

    private ClassScanner() {
    }

    /**
     * 使用默认ClassLoader获取一个ClassScanner实例
     *
     * @return ClassScanner实例
     */
    public static ClassScanner getInstance() {
        return getInstance(ClassUtils.getDefaultClassLoader());
    }

    /**
     * 使用指定ClassLoader获取一个ClassScanner实例
     *
     * @param classLoader 用于加载查找到的class的ClassLoader
     * @return ClassScanner实例
     */
    public static ClassScanner getInstance(ClassLoader classLoader) {
        ClassScanner scanner = classScannerMap.putIfAbsent(classLoader, new ClassScanner());
        if (scanner == null) {
            scanner = classScannerMap.get(classLoader);
            scanner.classLoader = classLoader;
        }

        return scanner;
    }

    /**
     * 参数必须为String数组，该参数为要扫描的包名
     */
    @Override
    public List<Class<?>> scan(Object... args) {
        if (args == null || args.length == 0) {
            return Collections.emptyList();
        }
        List<Class<?>> result = null;
        for (Object obj : args) {
            List<Class<?>> list = scan((String) obj);
            if (result == null) {
                result = list;
            } else {
                result.addAll(list);
            }
        }
        return result;
    }

    /**
     * 扫描指定的包中的所有class
     *
     * @param excludeFilters 过滤器，不能为null，filter返回true时扫描出的class将被过滤
     * @param args           参数（String类型，要扫描的包的集合）
     * @return 扫描结果
     * @throws ScannerException 扫描异常
     */
    @Override
    public List<Class<?>> scanByFilter(List<ClassFilter> excludeFilters,
                                       Object... args) throws ScannerException {
        logger.debug("搜索扫描所有的类，过滤器为：{}，参数为：{}", excludeFilters, args);

        if (args == null || args.length == 0) {
            return Collections.emptyList();
        }

        for (Object arg : args) {
            if (!(arg instanceof String)) {
                logger.debug("参数类型为：{}", args.getClass());
                throw new ScannerException("参数类型为：" + args.getClass());
            }
        }

        List<Class<?>> result = null;
        for (Object obj : args) {
            List<Class<?>> list = scanByFilter((String) obj, excludeFilters);
            if (result == null) {
                result = list;
            } else {
                result.addAll(list);
            }
        }
        return result;
    }

    /**
     * 根据包名扫描类，同时使用过滤器过滤
     *
     * @param pack    包名
     * @param filters 过滤器组合，不能为null，filter返回true时扫描出的class将被过滤
     * @return 过滤后的所有Class
     */
    public List<Class<?>> scanByFilter(String pack, List<ClassFilter> filters) {
        logger.debug("开始扫描包{}下的所有类的集合", pack);
        // 第一个class类的集合
        List<Class<?>> classes = new ArrayList<>();
        // 获取包的名字 并进行替换
        String packageDirName = pack.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
        } catch (IOException e) {
            logger.error("扫描包{}资源出错", packageDirName);
            throw new RuntimeException(e);
        }
        // 循环迭代下去
        while (dirs.hasMoreElements()) {
            try {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    logger.debug("file类型扫描");
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(),
                        Charset.defaultCharset().name());
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    classes.addAll(findAndAddClassesInPackageByFile(pack, filePath, true));
                } else if ("jar".equals(protocol)) {
                    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    classes
                        .addAll(findAndAddClassesInPackageByFile(jar, packageDirName, pack, true));
                }
            } catch (Throwable e) {
                logger.debug("扫描{}时出错", pack, e);
            }
        }

        if (!filters.isEmpty()) {
            logger.debug("扫描完毕，扫描出来的Class集合为：{}，发现过滤器，使用过滤器过滤", classes);
            classes = classes.stream().filter(clazz -> filter(clazz, filters))
                .collect(Collectors.toList());
            logger.debug("过滤后的Class为：{}", classes);
        }

        return classes;
    }

    /**
     * 根据包名扫描类
     *
     * @param packageName 包名
     * @return 扫描出来的所有类
     */
    private List<Class<?>> scan(String packageName) {
        return scanByFilter(packageName, Collections.emptyList());
    }

    /**
     * 从jar文件中扫描指定的包
     *
     * @param jar            jar文件
     * @param packageDirName 文件路径
     * @param packageName    报名
     * @param recursive      是否递归查找
     * @return jar文件中指定包名下的所有class
     */
    public Set<Class<?>> findAndAddClassesInPackageByFile(JarFile jar, String packageDirName,
                                                          String packageName, boolean recursive) {
        logger.debug("尝试扫描jar文件{}");
        Set<Class<?>> classes = new LinkedHashSet<>();
        logger.debug("jar类型的扫描");
        // 获取jar
        // 从此jar包 得到一个枚举类
        Enumeration<JarEntry> entries = jar.entries();
        // 同样的进行循环迭代
        while (entries.hasMoreElements()) {
            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            // 如果是以/开头的
            if (name.charAt(0) == '/') {
                // 获取后面的字符串
                name = name.substring(1);
            }
            // 如果前半部分和定义的包名相同
            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf('/');
                // 如果以"/"结尾 是一个包
                if (idx != -1) {
                    // 获取包名 把"/"替换成"."
                    packageName = name.substring(0, idx).replace('/', '.');
                }
                // 如果可以迭代下去 并且是一个包
                if ((idx != -1) || recursive) {
                    // 如果是一个.class文件 而且不是目录
                    if (name.endsWith(".class") && !entry.isDirectory()) {
                        // 去掉后面的".class" 获取真正的类名
                        String className = name.substring(packageName.length() + 1,
                            name.length() - 6);
                        className = packageName + "." + className;
                        try {
                            logger.debug("尝试构建class：{}", className);
                            // 添加到classes
                            classes.add(classLoader.loadClass(className));
                        } catch (Throwable e) {
                            logger.warn("找不到{}文件", className + ".class", e);
                        }
                    }
                }
            }
        }

        return classes;
    }

    /**
     * 使用指定过滤器组合过滤类
     *
     * @param clazz   要过滤的类
     * @param filters 过滤器组合，不能为null，filter返回true时扫描出的class将被过滤
     * @return 返回true说明指定的类通过了过滤器组合的过滤
     */
    private boolean filter(Class<?> clazz, List<ClassFilter> filters) {
        boolean flag = true;
        for (ClassFilter filter : filters) {
            if (filter.filter(clazz)) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    /**
     * 扫描指定目录下的所有class
     *
     * @param packageName 指定目录
     * @param packagePath 包名
     * @param recursive   是否递归查找
     * @return 指定目录下的所有class
     */
    public Set<Class<?>> findAndAddClassesInPackageByFile(String packageName, String packagePath,
                                                          final boolean recursive) {
        logger.debug("扫描包{}下、目录{}下的所有class", packageName, packagePath);
        Set<Class<?>> classes = new LinkedHashSet<>();
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            logger.debug("用户定义包名 " + packageName + " 下没有任何文件");
            return Collections.emptySet();
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(file -> (recursive && file.isDirectory())
                                                || (file.getName().endsWith("" + "" + ".class")));

        if (dirfiles == null || dirfiles.length == 0) {
            return Collections.emptySet();
        }

        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                classes.addAll(findAndAddClassesInPackageByFile(packageName + "/" + file.getName(),
                    file.getAbsolutePath(), recursive));
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    packageName = packageName.replaceAll("/", ".");
                    className = packageName + '.' + className;
                    classes.add(classLoader.loadClass(className));
                } catch (Throwable e) {
                    logger.error("添加用户自定义视图类错误 找不到此类的.class文件", e);
                }
            }
        }
        return classes;
    }
}
