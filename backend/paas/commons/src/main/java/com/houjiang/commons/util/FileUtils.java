package com.houjiang.commons.util;

import java.util.UUID;

/**
 * @ClassName FileUtils
 * @Description
 * @Author Amos
 * @Date 2024/9/11 14:37
 * @Version 1.0
 * @author Amos
 */

public class FileUtils {
    /** 辅助方法：验证是否为有效的本地文件路径 */
    public  static  boolean isValidLocalFilePath(String filePath) {
        // 简单的验证逻辑，可以根据实际需求进行调整
        return !filePath.contains("http") && !filePath.contains("https") && filePath.startsWith("/");
    }

    public static String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "_" + originalFileName;
    }

    /** 检查文件名是否合法 */
    public static boolean isValidFilename(String name) {
        // 禁止的字符列表
        String[] invalidChars = {"\\", "/", ":", "*", "?", "\"", "<", ">", "|"};
        for (String c : invalidChars) {
            if (name.contains(c)) {
                return false;
            }
        }
        return true;
    }

}
