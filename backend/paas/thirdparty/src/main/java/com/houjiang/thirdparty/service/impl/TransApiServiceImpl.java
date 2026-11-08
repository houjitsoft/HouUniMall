package com.houjiang.thirdparty.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.commons.config.ThreadPoolConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.thirdparty.service.TransApiService;
import com.houjiang.thirdparty.utils.TransApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Amos
 */
@Service
public class TransApiServiceImpl implements TransApiService {
    /** 引入线程池 */
    private static final ExecutorService executorService = ThreadPoolConfig.executorService;

    /** 在平台申请的APP_ID 详见 http://api.fanyi.baidu.com/api/trans/product/desktop?req=developer */
    @Value("${TRANS_APP_ID}")
    private String APP_ID;
    @Value("${TRANS_SECURITY_KEY}")
    private String SECURITY_KEY ;
    @Value("${TRANS_PC_i18nPath}")
    private String i18nPath;

    /**
     * 翻译功能
     *
     * @param map 包含翻译参数的映射：源文本和目标语言
     * @return 翻译结果的响应
     */
    @Override
    public ResponseResult trans(Map<String, String> map) {
        TransApi api = new TransApi(APP_ID, SECURITY_KEY);
        String query = map.get("src");
        String from = map.get("from") == null ? "auto" : map.get("from");
        String to = map.get("to");
        if (StrUtil.isEmpty(query)) {
            return ResponseResult.success("");
        }
        if (StrUtil.isEmpty(to)) {
            return ResponseResult.error("目标语言不能为空");
        }
        // 兼容前端 cn 与百度 zh
        if ("cn".equalsIgnoreCase(from)) {
            from = "zh";
        }
        JSONObject cached = checkIsTrans(query, to);
        if (null != cached) {
            return ResponseResult.success(cached);
        }
        String raw;
        try {
            raw = api.getTransResult(query, from, to);
        } catch (Exception e) {
            raw = null;
        }
        JSONObject parseObject = raw == null ? null : JSON.parseObject(raw);
        JSONArray trans_result = parseObject == null ? null : parseObject.getJSONArray("trans_result");
        if (trans_result == null || trans_result.isEmpty()) {
            // 百度欠费/失败时走免费兜底，保证商城多语言可用
            String fallbackDst = freeTranslate(query, from, to);
            if (StrUtil.isEmpty(fallbackDst)) {
                String err = parseObject != null && parseObject.get("error_code") != null
                        ? ("百度翻译失败[" + parseObject.getString("error_code") + "]: " + parseObject.getString("error_msg")
                            + "；免费兜底亦失败")
                        : "翻译服务无结果";
                return ResponseResult.error(err);
            }
            JSONObject result = new JSONObject();
            result.put("src", query);
            result.put("dst", fallbackDst);
            final String writeTo = to;
            final String writeDst = fallbackDst;
            executorService.submit(() -> writeInFile(query, writeTo, writeDst));
            return ResponseResult.success(result);
        }
        JSONObject result = trans_result.getJSONObject(0);
        String dst = result.getString("dst");
        final String writeTo = to;
        final String writeDst = dst;
        executorService.submit(() -> writeInFile(query, writeTo, writeDst));
        return ResponseResult.success(result);
    }

    /**
     * 免费翻译兜底（MyMemory）。百度欠费时仍可翻译常见 UI 文案。
     */
    private String freeTranslate(String query, String from, String to) {
        try {
            String src = "zh".equalsIgnoreCase(from) || "cn".equalsIgnoreCase(from) || "auto".equalsIgnoreCase(from) ? "zh-CN" : from;
            String dstLang = switch (String.valueOf(to).toLowerCase()) {
                case "en" -> "en";
                case "jp", "ja" -> "ja";
                case "kor", "ko" -> "ko";
                case "fra", "fr" -> "fr";
                case "de" -> "de";
                case "spa", "es" -> "es";
                default -> to;
            };
            String url = "https://api.mymemory.translated.net/get?q="
                    + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8)
                    + "&langpair=" + java.net.URLEncoder.encode(src + "|" + dstLang, java.nio.charset.StandardCharsets.UTF_8);
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .timeout(java.time.Duration.ofSeconds(8))
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
            JSONObject obj = JSON.parseObject(response.body());
            if (obj == null) return null;
            JSONObject responseData = obj.getJSONObject("responseData");
            if (responseData == null) return null;
            String translated = responseData.getString("translatedText");
            if (StrUtil.isEmpty(translated)) return null;
            translated = translated.replace("&quot;", "\"").replace("&#39;", "'");
            // 无效占位译文丢弃
            if ("null".equalsIgnoreCase(translated) || "undefined".equalsIgnoreCase(translated)) return null;
            return translated;
        } catch (Exception e) {
            System.err.println("[freeTranslate] failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public JSONObject checkIsTrans(String query,String to){
        // 将翻译后的语言写入语言包
        String filePath = i18nPath + "/pc/js/i18n/i18n_"+ to +".json";
        // String filePath = "D:\\java\\mall-ms-front\\i18n_en.json";
        try {
            // 读取JSON文件内容为字符串
            String jsonStr = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject  jsonObject = JSON.parseObject(jsonStr);
            if(null == jsonObject){
                jsonObject = new JSONObject();
            }
            String str = jsonObject.getString(query);
            if(null == str){
                return null;
            }
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("dst",str);
            jsonObject1.put("src",query);
            return jsonObject1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public synchronized void writeInFile(String query,String to,String  dst) {
        // 将翻译后的语言写入语言包
        String filePath = i18nPath + "/pc/js/i18n/i18n_"+ to +".json";
        // String filePath = "D:\\java\\mall-ms-front\\i18n_en.json";
        try {
            // 读取JSON文件内容为字符串
            String jsonStr = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject  jsonObject = JSON.parseObject(jsonStr);
            if(null == jsonObject){
                jsonObject = new JSONObject();
            }
            String str = jsonObject.getString(query);
            if(null == str){
                jsonObject.put(query,dst);
            }
            FileWriter file = new FileWriter(filePath);
            file.write(jsonObject.toString());
            file.close();
            System.out.println("Successfully wrote JSON object to file: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    /**
     * 查询所有语言选项
     * <p>
     * 本方法主要用于查询项目国际化(i18n)目录下的所有语言文件，并返回一个包含所有语言选项的列表
     * 它首先检查指定的i18n目录是否存在，然后获取该目录下的所有文件名基于文件名提取语言标识，
     * 并根据该标识构建一个语言选项的列表注意，中文(cn)不包含在返回的列表中
     *
     * @return 返回一个 {@link ResponseResult} 对象，其中包含一个语言选项列表
     */
    @Override
    public ResponseResult queryLanguageAll() {
        // 创建一个File对象，代表i18n目录
        File directory = new File(i18nPath + "/pc/js/i18n/");
        // 初始化一个列表来存储语言选项
        List<Map<String, String>> languagesList =  new ArrayList<>();

        // 检查目录是否存在且是一个目录
        if (directory.exists() && directory.isDirectory()) {
            // 获取目录下的所有文件名
            String[] files = directory.list();
            // 遍历文件数组
            if (files != null) {
                for (String fileName : files) {
                    // 从文件名中提取语言名
                    String language = getLanguageName(fileName);
                    // 确保语言不是中文
                    if(!language.equals("cn")) {
                        // 创建一个映射来存储语言标识和描述
                        Map<String, String> map = new HashMap<>();
                        map.put(language, CommonType.languageEnum.getDescByKey(language));
                        // 将映射添加到语言列表中
                        languagesList.add(map);
                    }
                }
            }
        }
        // 返回一个包含所有语言选项的响应结果
        return ResponseResult.success(languagesList);
    }

    /**
     * 从文件名中提取语言名称
     * 文件名的格式假设为带有下划线和.json扩展名的格式
     * 例如，"file_name_language.json"，其中"language"是语言名称
     *
     * @param fileName 文件名字符串，预计包含下划线和.json扩展名
     * @return 从下划线后到.json前的字符串，代表语言名称
     */
    public static String getLanguageName(String fileName) {
        // 找到下划线之后的位置
        int startIdx = fileName.indexOf("_") + 1;
        // 找到最后的.json之前的位置
        int endIdx = fileName.lastIndexOf(".json");
        return fileName.substring(startIdx, endIdx);
    }


    @Override
    /**
     * 查询语言键值对
     * 本方法用于根据语言类型和搜索关键字查询相应的语言键值对
     * @param map 包含搜索参数的映射，包括'key'（搜索关键字）、'language'（语言类型）、'page'（页码）和'limit'（每页数量）
     * @return 包含分页语言键值对的响应结果
     */
    public ResponseResult queryLanguageValue(Map<String, String> map) {
        // 从map中获取搜索关键字
        String key = map.get("key");
        // 从map中获取语言类型
        String language = map.get("language");
        // 从map中获取并解析页码
        Integer page = Integer.parseInt(map.get("page"));
        // 从map中获取并解析每页数量
        Integer limit = Integer.parseInt(map.get("limit"));
        // 构建语言文件的路径
        String filePath = i18nPath + "/pc/js/i18n/i18n_" + language + ".json";
        // 初始化存储语言键值对的列表
        List<Map.Entry<String, Object>> languagesList =  new ArrayList<>();
        try {
            // 读取JSON文件内容为字符串
            String jsonStr = new String(Files.readAllBytes(Paths.get(filePath)));
            // 将字符串内容解析为JSONObject
            JSONObject jsonObject = JSON.parseObject(jsonStr);

            // 获取JSONObject中的所有键值对
            List<Map.Entry<String, Object>> keyValuePairs = new ArrayList<>(jsonObject.entrySet());

            // 根据搜索关键字过滤键值对
            if(StringUtils.isEmpty(key)){
                // 如果关键字为空，则直接使用所有键值对
                languagesList.addAll(keyValuePairs);
            } else {
                // 如果有关键字，则过滤出包含关键字的键值对
                languagesList = keyValuePairs.stream().filter(pair -> pair.getKey().contains(key)).collect(Collectors.toList());
            }
        } catch (IOException e) {
            // 处理文件读取异常
            e.printStackTrace();
        }
        // 返回分页后的语言键值对
        return ResponseResult.success(PageUtils.getPage(languagesList, page, limit));
    }


    /**
     * 更新语言文件中的某个键值对
     *
     * @param map 包含键值对和语言信息的Map，其中键名为key，值为value，语言为language
     * @return ResponseResult 表示操作结果的响应对象
     */
    @Override
    public ResponseResult updateLanguageValue(Map<String, String> map) {
        // 从map中获取键名
        String key = map.get("key");
        // 从map中获取值
        String value = map.get("value");
        // 从map中获取语言代码
        String language = map.get("language");
        // 根据语言代码构建国际化文件的文件路径
        String filePath = i18nPath + "/pc/js/i18n/i18n_" + language + ".json";
        try {
            // 读取JSON文件内容为字符串
            String jsonStr = new String(Files.readAllBytes(Paths.get(filePath)));
            // 将JSON字符串解析为JSONObject对象
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            // 在JSONObject中更新键值对
            jsonObject.put(key, value);
            // 重新写入JSON文件
            FileWriter file = new FileWriter(filePath);
            file.write(jsonObject.toString());
            file.close();
        } catch (IOException e) {
            // 处理文件操作中的异常
            e.printStackTrace();
        }
        // 返回操作成功的响应结果
        return ResponseResult.success();
    }
}
