package com.plakhotnikov.cloud_storage_engine.util;

import java.util.List;

public class FileUtil {
    public static List<String> separateFileName(String fileName) {
        int i = fileName.lastIndexOf('.');
        return List.of(fileName.substring(0, i), fileName.substring(i + 1));
    }
}
