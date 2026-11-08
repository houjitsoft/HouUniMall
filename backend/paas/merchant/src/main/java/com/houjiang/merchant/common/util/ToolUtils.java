package com.houjiang.merchant.common.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.config.ProjectConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用工具集合
 */
public class ToolUtils {

    /**
     * 制作UUID
     *
     * @author Amos
     * @return String
     */
    public static String makeUUID(){
        return UUID.randomUUID().toString().replaceAll("-","").substring(0,32);
    }

    /**
     * 制作MD5
     *
     * @author Amos
     * @param data 需加密的数据
     * @return String
     */
    public static String makeMd5(String data){
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte [] array = md5.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 制作MD5
     *
     * @author Amos
     * @param data 需加密的数据
     * @return String
     */
    public static String makePassword(String password){
        // 盐键与 Nacos merchant.yaml 对齐：merchant.unique-identification
        String salt = YmlUtils.get("merchant.unique-identification");
        String newPWd = password + (salt == null ? "" : salt);
        String md5Pwd = ToolUtils.makeMd5(ToolUtils.makeMd5(newPWd));
        return md5Pwd;
    }

    /**
     * 生成唯一Token
     *
     * @author Amos
     * @return String
     */
    public static String makeToken() {
        long millisecond =  System.currentTimeMillis();
        String randStr =  ToolUtils.randomString(8);
        String secret  = GlobalConfig.secret;
        String token   = ToolUtils.makeMd5(ToolUtils.makeUUID() + millisecond + randStr);
        return ToolUtils.makeMd5(token + secret) + ToolUtils.randomString(6);
    }

    /**
     * 返回随机字符串
     *
     * @author Amos
     * @param length 要生成的长度
     * @return String
     */
    public static String randomString(int length) {
        Random random = new Random();
        StringBuilder stringBuffer = new StringBuilder();
        String str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int strLength = str.length();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(strLength);
            stringBuffer.append(str.charAt(index));
        }
        return stringBuffer.toString();
    }

    /**
     * 返回随机数字字符串
     *
     * @author Amos
     * @param length 要生成的长度
     * @return String
     */
    public static String randomInt(int length) {
        Random random = new Random();
        StringBuilder stringBuffer = new StringBuilder();
        String str = "0123456789";
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(10);
            stringBuffer.append(str.charAt(index));
        }
        return stringBuffer.toString();
    }

    /**
     * 转换存储单位: KB MB GB TB
     *
     * @author Amos
     * @return String
     */
    public static String storageUnit(Long size) {
        if (size == null) {
            return "0B";
        }
        if (size < 1024) {
            return size + "B";
        } else {
            size = size / 1024;
        }
        if (size < 1024) {
            return size + "KB";
        } else {
            size = size / 1024;
        }
        if (size < 1024) {
            size = size * 100;
            return (size / 100) + "." + (size % 100) + "MB";
        } else {
            size = size * 100 / 1024;
            return (size / 100) + "." + (size % 100) + "GB";
        }
    }

    /**
     * 下载文件
     *
     * @author Amos
     * @param urlStr   (文件网址)
     * @param savePath (保存路径,如: /www/uploads/aa.png)
     * @throws IOException IO异常
     */
    public static void download(String urlStr, String savePath) throws IOException {
        ByteArrayOutputStream bos = null;
        FileOutputStream fos = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5*1000);
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            inputStream = conn.getInputStream();

            // 获取数组数据
            byte[] buffer = new byte[4*1024];
            int len;
            bos = new ByteArrayOutputStream();
            while((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            byte[] getData = bos.toByteArray();

            // 新创建文件夹
            String fileName = StringUtils.substringAfterLast(savePath, "/");
            String path = savePath.replace("/"+fileName, "");
            File saveDir = new File(path);
            if(!saveDir.exists()) {
                if (!saveDir.mkdirs()) {
                    throw new IOException("创建存储文件夹失败");
                }
            }
            // 保存文件数据
            File file = new File(savePath);
            fos = new FileOutputStream(file);
            fos.write(getData);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ignored) {}
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {}
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {}
            }
        }
    }

    public static JSONObject getExportData(Long total, Integer pageSize, Integer pageStart, Integer pageEnd, String fileName) {
        JSONObject ret = new JSONObject();
        Integer cpPageSize = StringUtils.isNull(pageSize) ? ProjectConfig.lists.get("pageSize") : pageSize;
        Integer sumPage = getMaxCeil(total.intValue(), cpPageSize);
        Integer pageSizeMax = ProjectConfig.lists.get("pageSizeMax");
        //所有数据记录数
        ret.put("count", total);
        //每页记录数
        ret.put("page_size", cpPageSize);
        //一共多少页
        ret.put("sum_page", sumPage);
        ret.put("max_page", Math.ceil(pageSizeMax / cpPageSize));
        ret.put("all_max_size", pageSizeMax);
        ret.put("page_end", Math.min(sumPage, StringUtils.isNull(pageEnd) ? 200 : pageEnd));
        ret.put("page_start", StringUtils.isNull(pageStart) ? 1 : pageStart);
        ret.put("file_name", fileName);
        return ret;
    }

    /**
     * 与1比较，返回更大的值
     * @param count
     * @param pageSize
     * @return
     */
    public static Integer getMaxCeil(Integer count, Integer pageSize) {
        return Math.max((int)Math.ceil((double)count / (double)pageSize), 1);
    }

    /**
     * 校验HTTP
     * @param url
     * @return
     */
    public static String checkHttp(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        } else if (url.startsWith("//")) {
            url = url.replaceFirst("^//+", "");
            url = "http://" + url;
            return url;
        } else {
            url = "http://" + url;
            return url;
        }
    }

    public static void downloadImage(String fileName, String remoteUrl, String savePath) throws IOException {
        URL url = new URL(remoteUrl);
        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        if (!saveDir.isDirectory()) {
            throw new IOException("Save path is not a directory.");
        }
        File file = new File(saveDir, fileName);
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    public static List<String> getContentImage(String str) {
        List<String> result = new ArrayList<>();
        if (str != null && !str.isEmpty()) {
            // 正则表达式匹配src属性值，支持双引号、单引号或无引号
            String regex = "<img[^>]*?src=(?:\\s*([\"'])(.*?)\\1|\\s*([^\\s>]*))";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(str);
            while (matcher.find()) {
                // 优先使用带引号的组，如果没有则使用无引号的组
                if (matcher.group(2) != null) {
                    result.add(matcher.group(2));
                } else if (matcher.group(3) != null) {
                    result.add(matcher.group(3));
                }
            }
        }
        return result;
    }

    /**
     * 把文字
     * @param input
     * @return
     */
    public static List<String> mbStrSplit(String input, int length) {
        List<String> result = new ArrayList<>();
        if (input == null || input.isEmpty() || length <= 0) {
            return result;
        }

        // 获取字符总数
        int totalLength = input.codePointCount(0, input.length());
        int offset = 0;

        while (offset < totalLength) {
            // 计算当前组的结束位置
            int end = Math.min(offset + length, totalLength);
            String group = input.substring(
                    // 起始位置
                    input.offsetByCodePoints(0, offset),
                    // 结束位置
                    input.offsetByCodePoints(0, end)
            );
            result.add(group);
            // 移动到下一组的起始位置
            offset = end;
        }

        return result;
    }

    /**
     * 将 JSON 对象中的所有键从驼峰形式转换为下划线形式
     *
     * @param jsonObject 输入的 JSON 对象
     * @return 转换后的 JSON 对象
     */
    public static JSONObject convertKeysToSnakeCase(JSONObject jsonObject) {
        JSONObject result = new JSONObject();
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            // 转换键名
            String newKey = camelToSnakeCase(key);
            // 递归处理嵌套的 JSONObject 或 JSONArray
            if (value instanceof JSONObject) {
                value = convertKeysToSnakeCase((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = convertKeysInArray((JSONArray) value);
            }
            result.put(newKey, value);
        }
        return result;
    }

    /**
     * 递归处理 JSONArray 中的每个元素
     *
     * @param jsonArray 输入的 JSONArray
     * @return 转换后的 JSONArray
     */
    private static JSONArray convertKeysInArray(JSONArray jsonArray) {
        JSONArray result = new JSONArray();
        for (Object item : jsonArray) {
            if (item instanceof JSONObject) {
                result.add(convertKeysToSnakeCase((JSONObject) item));
            } else if (item instanceof JSONArray) {
                result.add(convertKeysInArray((JSONArray) item));
            } else {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 将驼峰形式的字符串转换为下划线形式
     *
     * @param camelCase 驼峰形式的字符串
     * @return 下划线形式的字符串
     */
    private static String camelToSnakeCase(String camelCase) {
        StringBuilder snakeCase = new StringBuilder();
        for (char ch : camelCase.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                snakeCase.append('_').append(Character.toLowerCase(ch));
            } else {
                snakeCase.append(ch);
            }
        }
        return snakeCase.toString();
    }
}
