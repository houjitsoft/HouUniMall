package com.houjiang.merchant.common.util;

import com.houjiang.merchant.common.config.GlobalConfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件路径处理工具
 */
public class UrlUtils {

    /**
     * 访问前缀
     */
    private static final String uploadPrefix = GlobalConfig.publicPrefix;
    private static final String uploadAdminPrefix = GlobalConfig.adminPublicPrefix;
    private static final String uploadShopPrefix = GlobalConfig.shopPublicPrefix;


    /**
     * 转绝对路径
     * 转前: uploads/11.png
     * 转后: https://127.0.0.1/uploads/11.png
     *
     * @author Amos
     * @param url 相对路径
     * @return String
     */
    public static String toAbsoluteUrl(String url) {
        if (url == null || url.equals("")) {
            return "";
        }

        if(url.indexOf("/") != 0) {
            url = "/" + url;
        }

        if (url.startsWith("/api/static/")) {
            return getRequestUrl() + url;
        }

        if (url.startsWith("/resource/image")) {
            // + "/api/static" + url.replace("/resource/image", "");
            return getRequestUrl() + "/api" + url;
        }

        String engine = ConfigUtils.get("storage", "default", "local");
        engine = engine.equals("") ? "local" : engine;
        if (engine.equals("local")) {
            if (url.startsWith("/uploads/")) {
                url = url.replace("/uploads/", "");
            }
            return getRequestUrl() + "/" + uploadPrefix + "/" + url;
        }

        Map<String, String> config = ConfigUtils.getMap("storage", engine);
        if (config != null) {
            return config.getOrDefault("domain", "") + url;
        }
        return url;
    }

    /**
     * 转绝对路径
     * 转前: uploads/11.png
     * 转后: https://127.0.0.1/uploads/11.png
     *
     * @author Amos
     * @param url 相对路径
     * @return String
     */
    public static String toAdminAbsoluteUrl(String url) {
        if (url == null || url.equals("")) {
            return "";
        }

        if (url.startsWith("/") == false) {
            url = "/" + url;
        }

        if (url.startsWith("/adminapi/static/")) {
            return getRequestUrl() + url;
        }

        if (url.startsWith("/resource/image")) {
            return getRequestUrl() + "/adminapi/static" + url.replace("/resource/image", "");
        }

        String engine = ConfigUtils.get("storage", "default", "local");
        engine = engine.equals("") ? "local" : engine;
        if (engine.equals("local")) {
            if (url.startsWith("/uploads/")) {
                url = url.replace("/uploads/", "/");
            }
            return getRequestUrl() + "/" + uploadAdminPrefix + url;
        }

        Map<String, String> config = ConfigUtils.getMap("storage", engine);
        if (config != null) {
            return config.getOrDefault("domain", "") + url;
        }
        return url;
    }

    /**
     * 转绝对路径
     * 转前: uploads/11.png
     * 转后: https://127.0.0.1/uploads/11.png
     *
     * @author Amos
     * @param url 相对路径
     * @return String
     */
    public static String toShopAbsoluteUrl(String url) {
        if (url == null || url.equals("")) {
            return "";
        }

//        if(url.indexOf("/") != 0) {
//            url = "/" + url;
//        }
        if (url.startsWith("/") == false) {
            url = "/" + url;
        }

        if (url.startsWith("/merchant/static/")) {
            return getRequestUrl() + url;
        }

        if (url.startsWith("/resource/image")) {
            return getRequestUrl() + "/merchant/static" + url.replace("/resource/image", "");
        }
        String engine = ConfigUtils.get("storage", "default", "local");
        engine = engine.equals("") ? "local" : engine;
        if (engine.equals("local")) {
            if (url.startsWith("/uploads/")) {
                url = url.replace("/uploads/", "/");
            }
            return getRequestUrl() + "/" + uploadShopPrefix  + url;
        }

        Map<String, String> config = ConfigUtils.getMap("storage", engine);
        if (config != null) {
            return config.getOrDefault("domain", "") + url;
        }
        return url;
    }

    /**
     * 转相对路径
     * 转前: https://127.0.0.1/uploads/11.png
     * 转后: uploads/11.png
     *
     * @author Amos
     * @param url 绝对路径
     * @return String
     */
    public static String toAdminRelativeUrl(String url) {
        if (url == null || url.equals("")) {
            return "";
        }

        String engine = ConfigUtils.get("storage", "default", "local");
        engine = engine.equals("") ? "local" : engine;
        if (engine.equals("local")) {
            url = url.replace(RequestUtils.uri(), "");
            if (url.startsWith("/" + uploadAdminPrefix + "/")) {
                return url.replace("/" + uploadAdminPrefix + "/", "uploads/");
            } else if (url.startsWith("/adminapi/static/")) {
                return url.replace("/adminapi/static/", "uploads/");
            }
        }

        Map<String, String> config = ConfigUtils.getMap("storage", engine);
        if (config != null) {
            url = url.replace(config.getOrDefault("domain", ""), "");
            if (url.startsWith("/" + uploadAdminPrefix + "/")) {
                return url.replace("/" + uploadAdminPrefix + "/", "uploads/");
            } else if (url.startsWith("/adminapi/static/")) {
                return url.replace("/adminapi/static/", "uploads/");
            }
            //return url.replace( "/" + uploadAdminPrefix + "/", "uploads/");
        }

        return url;
    }
    /**
     * 转相对路径
     * 转前: https://127.0.0.1/uploads/11.png
     * 转后: uploads/11.png
     *
     * @author Amos
     * @param url 绝对路径
     * @return String
     */
    public static String toRelativeUrl(String url) {
        if (url == null || url.equals("")) {
            return "";
        }

        String engine = ConfigUtils.get("storage", "default", "local");
        engine = engine.equals("") ? "local" : engine;
        if (engine.equals("local")) {
            url = url.replace(RequestUtils.uri(), "");
            if (url.startsWith("/" + uploadPrefix + "/")) {
                return url.replace("/" + uploadPrefix + "/", "uploads/");
            } else if (url.startsWith("/api/static/")) {
                return url.replace("/api/static/", "uploads/");
            }
        }

        Map<String, String> config = ConfigUtils.getMap("storage", engine);
        if (config != null) {
            url = url.replace(config.getOrDefault("domain", ""), "");
            if (url.startsWith("/" + uploadPrefix + "/")) {
                return url.replace("/" + uploadPrefix + "/", "uploads/");
            } else if (url.startsWith("/api/static/")) {
                return url.replace("/api/static/", "uploads/");
            }
        }

        return url;
    }

    /**
     * 转相对路径
     * 转前: https://127.0.0.1/uploads/11.png
     * 转后: uploads/11.png
     *
     * @author Amos
     * @param url 绝对路径
     * @return String
     */
    public static String toShopRelativeUrl(String url) {
        if (url == null || url.equals("")) {
            return "";
        }

        String engine = ConfigUtils.get("storage", "default", "local");
        engine = engine.equals("") ? "local" : engine;
        if (engine.equals("local")) {
            url = url.replace(RequestUtils.uri(), "");
            if (url.startsWith("/" + uploadShopPrefix + "/")) {
                return url.replace("/" + uploadShopPrefix + "/", "uploads/");
            } else if (url.startsWith("/merchant/static/")) {
                return url.replace("/merchant/static/", "uploads/");
            }
        }

        Map<String, String> config = ConfigUtils.getMap("storage", engine);
        if (config != null) {
            url = url.replace(config.getOrDefault("domain", ""), "");
            if (url.startsWith("/" + uploadShopPrefix + "/")) {
                return url.replace("/" + uploadShopPrefix + "/", "uploads/");
            } else if (url.startsWith("/merchant/static/")) {
                return url.replace("/merchant/static/", "uploads/");
            }
        }

        return url;
    }
    /**
     * 获取存储域名
     * 示例: https://127.0.0.1/
     *
     * @author Amos
     * @return String
     */
    public static String domain() {
        String engine = ConfigUtils.get("storage", "default", "local");
        engine = engine.equals("") ? "local" : engine;
        if (engine.equals("local")) {
            //RequestUtils.uri() + "/";
            return getRequestUrl() + "/";
        }

        Map<String, String> config = ConfigUtils.getMap("storage", engine);
        if (config != null) {
            return config.getOrDefault("domain", "") + "/";
        }
        return "";
    }

    /**
     * 获取域名
     * 示例: https://127.0.0.1/
     *
     * @author Amos
     * @return String
     */
    public static String localDomain(String url){
        return RequestUtils.uri() + url;
    }

    /**
     * 为了避免负载均衡而导致的域名失效，增加该方法
     * 优先级：merchant.url 配置 > 当前请求 host
     * @return
     */
    public static String getRequestUrl() {
        if (StringUtils.isNull(YmlUtils.get("merchant.url"))) {
            return RequestUtils.uri();
        } else {
            return YmlUtils.get("merchant.url");
        }
    }

    /**
     * 判断是否为有效的url
     * @param url
     * @return
     */
    public static boolean isValidUrl(String url) {
        // 协议
        String urlRegex =  "^(https?|ftp)://" +
                        // 域名
                        "([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})" +
                        // 端口
                        "(:\\d+)?" +
                        // 路径
                        "(/[^\\s?#]*)?" +
                        // 查询参数
                        "(\\?[^\\s#]*)?" +
                        "(#[^\\s]*)?";
        Pattern URL_PATTERN = Pattern.compile(urlRegex);
        Matcher matcher = URL_PATTERN.matcher(url);
        return matcher.matches();
    }

    /**
     * 判断 URL 是否有效且匹配指定域名
     *
     * @param url    需要验证的 URL
     * @param domain 需要匹配的域名关键字（如 "taobao" 或 "jd"）
     * @return 如果 URL 有效且匹配域名，返回 true；否则返回 false
     */
    public static boolean isValidUrlAndDomain(String url, String domain) {
        // 1. 验证 URL 是否有效
        if (!isValidUrl(url)) {
            return false;
        }

        // 2. 提取域名
        String extractedDomain = extractDomain(url);
        if (extractedDomain == null) {
            return false;
        }

        // 3. 检查域名是否包含指定的关键字
        return extractedDomain.contains(domain);
    }

    /**
     * 从 URL 中提取域名
     *
     * @param url 需要提取域名的 URL
     * @return 提取的域名，如果提取失败返回 null
     */
    private static String extractDomain(String url) {
        try {
            URL parsedUrl = new URL(url);
            // 获取域名部分
            String host = parsedUrl.getHost();
            return host;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * 模仿PHP的parse_url函数，解析URL并返回各个部分
     *
     * @param urlStr 需要解析的URL字符串
     * @return 包含URL各部分的Map，键名为"scheme", "host", "port", "user", "pass", "path", "query", "fragment"
     */
    public static Map<String, String> parseUrl(String urlStr) {
        Map<String, String> result = new HashMap<>();
        try {
            URL url = new URL(urlStr);
            // 提取scheme
            result.put("scheme", url.getProtocol());
            // 提取host
            result.put("host", url.getHost());
            // 提取port
            int port = url.getPort();
            result.put("port", port == -1 ? "" : String.valueOf(port));
            // 提取user和pass
            String userInfo = url.getUserInfo();
            if (userInfo != null) {
                if (userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    result.put("user", parts[0]);
                    result.put("pass", parts[1]);
                } else {
                    result.put("user", userInfo);
                    result.put("pass", "");
                }
            } else {
                result.put("user", "");
                result.put("pass", "");
            }
            // 提取path
            result.put("path", url.getPath());
            // 提取query
            result.put("query", url.getQuery());
            // 提取fragment
            result.put("fragment", url.getRef());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // 处理异常，可能返回一个空Map或抛出异常
        }
        return result;
    }

    /**
     * 模仿PHP的pathinfo函数，处理URL并返回目录名、基础名、扩展名和文件名
     *
     * @param urlStr 需要处理的URL字符串
     * @return 包含dirname, basename, extension, filename的Map
     */
    public static Map<String, String> pathInfo(String urlStr) {
        Map<String, String> result = new HashMap<>();
        try {
            URL url = new URL(urlStr);
            String protocol = url.getProtocol();
            String host = url.getHost();
            String path = url.getPath();

            if (path.equals("/")) {
                // 根路径，包含尾部斜杠
                result.put("dirname", protocol + "://" + host + path);
                result.put("basename", "");
                result.put("extension", "");
                result.put("filename", "");
            } else if (path.endsWith("/")) {
                // 目录路径，去掉尾部斜杠
                String dirnamePath = path.substring(0, path.length() - 1);
                result.put("dirname", protocol + "://" + host + dirnamePath);
                result.put("basename", "");
                result.put("extension", "");
                result.put("filename", "");
            } else {
                // 包含文件名
                int lastSlash = path.lastIndexOf('/');
                String directory = path.substring(0, lastSlash + 1);
                String filenameWithExtension = path.substring(lastSlash + 1);
                int lastDot = filenameWithExtension.lastIndexOf('.');
                String filename;
                String extension;
                if (lastDot == -1) {
                    filename = filenameWithExtension;
                    extension = "";
                } else {
                    filename = filenameWithExtension.substring(0, lastDot);
                    extension = filenameWithExtension.substring(lastDot + 1);
                }
                if (directory.equals("/")) {
                    // 根目录，保持尾部斜杠
                    result.put("dirname", protocol + "://" + host + directory);
                } else {
                    // 去掉目录的尾部斜杠
                    result.put("dirname", protocol + "://" + host + directory.substring(0, directory.length() - 1));
                }
                result.put("basename", filenameWithExtension);
                result.put("extension", extension);
                result.put("filename", filename);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 解析查询字符串，返回参数名和参数值的Map
     *
     * @param queryString 查询字符串，例如"id=721843326532&spm=a21bo.jianhua/a.201876.d1.33622a89QvtfK4&scm=1007.40986.276750.0&pvid=56e86705-b137-4d30-9281-27f1d4ede3d7"
     * @return 包含参数名和参数值的Map
     */
    public static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }
        // 分割参数对
        String[] paramPairs = queryString.split("&");
        for (String paramPair : paramPairs) {
            // 分割键和值
            int idx = paramPair.indexOf("=");
            if (idx != -1) {
                String key = paramPair.substring(0, idx);
                String value = paramPair.substring(idx + 1);
                // 可选：处理URL编码
                // key = URLDecoder.decode(key, StandardCharsets.UTF_8);
                // value = URLDecoder.decode(value, StandardCharsets.UTF_8);
                params.put(key, value);
            } else {
                // 没有值的参数，假设值为空字符串
                params.put(paramPair, "");
            }
        }
        return params;
    }
}
