package com.houjiang.bffweb.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * H5 前台资源 URL 与装修数据补强（BFF 层，不依赖 mall 服务是否已发布新版本）。
/**
 * @author Amos
 */
@Component
public class MallFrontMediaHelper {

    @Value("${houcloud.mall.public-domain:https://bbc.houjit.com/}")
    private String defaultPublicDomain;

    public String resolvePublicDomain() {
        return ensureTrailingSlash(defaultPublicDomain);
    }

    /**
     * 补齐 config.domain，并转换 tabbar 图标为绝对地址。
     */
    @SuppressWarnings("unchecked")
    public void enrichIndexConfig(Map<String, Object> data) {
        if (data == null) {
            return;
        }
        Object domain = data.get("domain");
        if (domain == null || !StringUtils.hasText(domain.toString())) {
            data.put("domain", resolvePublicDomain());
        }
        Object tabbarObj = data.get("tabbar");
        if (tabbarObj instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    Map<String, Object> tab = (Map<String, Object>) map;
                    tab.put("selected", toAbsoluteUrl(str(tab.get("selected"))));
                    tab.put("unselected", toAbsoluteUrl(str(tab.get("unselected"))));
                }
            }
        }
    }

    /**
     * 归一化首页装修数据中的组件字段，并转换图片为绝对 URL。
     */
    @SuppressWarnings("unchecked")
    public void enrichIndexData(Map<String, Object> data) {
        if (data == null) {
            return;
        }
        Object pageObj = data.get("page");
        if (!(pageObj instanceof Map<?, ?> pageMap)) {
            return;
        }
        Object pageDataObj = ((Map<String, Object>) pageMap).get("data");
        if (!(pageDataObj instanceof List<?> pageData)) {
            return;
        }
        normalizePageList((List<Object>) pageData);
        absolutizePageList((List<Object>) pageData);
    }

    @SuppressWarnings("unchecked")
    private void normalizePageList(List<Object> data) {
        for (Object item : data) {
            if (item instanceof List<?> group) {
                normalizePageList((List<Object>) group);
            } else if (item instanceof Map<?, ?> map) {
                Map<String, Object> comp = (Map<String, Object>) map;
                String name = str(comp.get("name"));
                Object contentObj = comp.get("content");
                if (!(contentObj instanceof Map<?, ?>)) {
                    continue;
                }
                Map<String, Object> content = (Map<String, Object>) contentObj;
                if ("banner".equals(name) && content.get("image_type") == null) {
                    content.put("image_type", content.getOrDefault("style", 1));
                }
                if ("nav".equals(name)) {
                    content.putIfAbsent("type", 1);
                    content.putIfAbsent("show_type", 1);
                    if (!content.containsKey("show_nums")) {
                        Object perLine = content.get("per_line");
                        int pl = perLine != null ? Integer.parseInt(perLine.toString()) : 5;
                        content.put("show_nums", pl <= 3 ? 1 : pl == 4 ? 2 : 3);
                    }
                    content.putIfAbsent("show_rows", content.getOrDefault("show_line", 2));
                }
                if ("search".equals(name) && content.isEmpty()) {
                    content.put("enabled", 1);
                    content.put("text_type", 1);
                    content.put("text", "请输入搜索关键字");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void absolutizePageList(List<Object> data) {
        for (Object item : data) {
            if (item instanceof List<?> group) {
                absolutizePageList((List<Object>) group);
            } else if (item instanceof Map<?, ?> map) {
                Object contentObj = ((Map<String, Object>) map).get("content");
                if (contentObj instanceof Map<?, ?> contentMap) {
                    absolutizeContent((Map<String, Object>) contentMap);
                }
            }
        }
    }

    private void absolutizeContent(Map<String, Object> content) {
        for (String key : List.of("bg_img", "bg_image", "titlebg_img", "scroll_img", "titile_img", "image", "qrcode")) {
            Object value = content.get(key);
            if (value instanceof String str && StringUtils.hasText(str)) {
                content.put(key, toAbsoluteUrl(str));
            }
        }
        Object dataList = content.get("data");
        if (!(dataList instanceof List<?> rows)) {
            return;
        }
        for (Object row : rows) {
            if (!(row instanceof Map<?, ?> rowMap)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) rowMap;
            Object image = item.get("image");
            if (image instanceof String img && StringUtils.hasText(img)) {
                item.put("image", toAbsoluteUrl(img));
            }
            Object bg = item.get("bg");
            if (bg instanceof String bgStr && StringUtils.hasText(bgStr)) {
                item.put("bg", toAbsoluteUrl(bgStr));
            }
        }
    }

    /**
     * 对齐 likeshop UrlUtils：uploads、resource/image、/adminapi/static 等。
     */
    public String toAbsoluteUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        String domain = resolvePublicDomain().replaceAll("/$", "");
        if (url.startsWith("/uploads/") || url.startsWith("/adminapi/")) {
            return domain + url;
        }
        if (url.startsWith("/")) {
            return domain + url;
        }
        if (url.startsWith("uploads/")) {
            return domain + "/" + url;
        }
        if (url.startsWith("resource/image")) {
            return domain + "/api/" + url;
        }
        return domain + "/api/" + url;
    }

    private static String str(Object v) {
        return v == null ? "" : v.toString();
    }

    private static String ensureTrailingSlash(String domain) {
        if (!StringUtils.hasText(domain)) {
            return "https://bbc.houjit.com/";
        }
        return domain.endsWith("/") ? domain : domain + "/";
    }
}
