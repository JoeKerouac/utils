package com.joe.utils.ext;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

/**
 * DocumentRootHelper，帮助查找doc-root，spring自带的在IDEA中运行时有问题，不能正确找到doc-root，扩展spring
 *
 * @author joe
 * @version 2018.05.24 16:40
 */
@Slf4j
public class DocumentRootHelper {
    /**
     * classpath下的doc-root
     */
    private static final File     DEFAULT_DOC_ROOT;
    /**
     * 本地工作空间的doc-root
     */
    private static final File     LOCAL_DOC_ROOT;
    private static final String[] COMMON_DOC_ROOTS = { "src/main/webapp", "public", "static" };

    static {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        if (url != null) {
            String path = url.getFile();
            DEFAULT_DOC_ROOT = new File(path);
            LOCAL_DOC_ROOT = new File(path.replace("target/classes", "src/main/webapp"));
        } else {
            DEFAULT_DOC_ROOT = null;
            LOCAL_DOC_ROOT = null;
        }
    }

    /**
     * 获取当前系统的doc-root
     *
     * @return 当前系统的doc-root
     */
    public static final File getValidDocumentRoot() {
        // If document root not explicitly set see if we are running from IDE
        File file = getIDEDocumentRoot();
        // If document root not explicitly set see if we are running from a war archive
        file = file != null ? file : getWarOrJarFileDocumentRoot();
        // If not a war archive maybe it is an exploded war
        file = file != null ? file : getExplodedWarFileDocumentRoot();
        if (file == null) {
            log.debug("None of the document roots " + Arrays.asList(COMMON_DOC_ROOTS)
                      + " point to a directory and will be ignored.");
        } else {
            log.debug("Document root: " + file);
        }
        return file;
    }

    /**
     * 获取war文件
     *
     * @return war文件
     */
    private static File getWarOrJarFileDocumentRoot() {
        return getArchiveFileDocumentRoot(".war");
    }

    /**
     * 获取war解压后运行状态的doc-root
     *
     * @return doc-root
     */
    private static File getExplodedWarFileDocumentRoot() {
        return getExplodedWarFileDocumentRoot(getCodeSourceArchive());
    }

    /**
     * 当用户在IDE中运行系统时该方法会生效
     *
     * @return doc-root
     */
    private static File getIDEDocumentRoot() {
        if (LOCAL_DOC_ROOT != null && LOCAL_DOC_ROOT.exists()) {
            log.debug("当前在IDE中运行，并且找到了工作空间");
            return LOCAL_DOC_ROOT;
        } else if (DEFAULT_DOC_ROOT != null && DEFAULT_DOC_ROOT.exists()) {
            log.debug("当前在IDE中运行，没有找到了工作空间，但是找到了classpath下的doc-root");
            return DEFAULT_DOC_ROOT;
        } else {
            log.debug("当前没有在IDE中运行");
            return null;
        }
    }

    private static File getArchiveFileDocumentRoot(String extension) {
        File file = getCodeSourceArchive();
        log.debug("Code archive: " + file);
        if (file != null && file.exists() && !file.isDirectory()
            && file.getName().toLowerCase().endsWith(extension)) {
            return file.getAbsoluteFile();
        }
        return null;
    }

    /**
     * 获取解压后的代码对应的doc-root目录
     *
     * @param codeSourceFile 当前代码所处的文件
     * @return 解压后的代码对应的doc-root目录
     */
    private static File getExplodedWarFileDocumentRoot(File codeSourceFile) {
        log.debug("Code archive: " + codeSourceFile);
        if (codeSourceFile != null && codeSourceFile.exists()) {
            String path = codeSourceFile.getAbsolutePath();
            int webInfPathIndex = path.indexOf(File.separatorChar + "WEB-INF" + File.separatorChar);
            if (webInfPathIndex >= 0) {
                path = path.substring(0, webInfPathIndex);
                return new File(path);
            }
        }
        return null;
    }

    /**
     * 获取当前代码的文件
     *
     * @return 当前代码所处的文件
     */
    private static File getCodeSourceArchive() {
        try {
            CodeSource codeSource = DocumentRootHelper.class.getProtectionDomain().getCodeSource();
            URL location = (codeSource == null ? null : codeSource.getLocation());
            if (location == null) {
                return null;
            }
            String path = location.getPath();
            URLConnection connection = location.openConnection();
            if (connection instanceof JarURLConnection) {
                path = ((JarURLConnection) connection).getJarFile().getName();
            }
            if (path.indexOf("!/") != -1) {
                path = path.substring(0, path.indexOf("!/"));
            }
            return new File(path);
        } catch (IOException ex) {
            return null;
        }
    }
}
