package com.joe.utils.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author joe
 * @version 2018.06.13 18:18
 */
public class FileUtils {
    /**
     * 遍历指定文件夹下所有文件
     *
     * @param file 文件夹
     * @return 指定文件夹下所有文件（不包括本文件夹，不包括里边所有文件夹，只有文件）
     */
    public static List<File> findAllFile(File file) {
        if (file == null || !file.exists() || file.isFile()) {
            return Collections.emptyList();
        }

        List<File> fileList = new ArrayList<>();
        File[] files = file.listFiles();
        for (File f : files) {
            fileList.addAll(findAllFile(f));
        }

        return fileList;
    }
}
