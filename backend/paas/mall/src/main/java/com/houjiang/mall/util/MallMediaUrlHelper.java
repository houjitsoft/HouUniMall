package com.houjiang.mall.util;

import com.houjiang.mall.service.setting.CommonConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * H5 资源域名与路径解析（对齐 likeshop UrlUtils 行为）。
 * @author Amos
 */
@Component
public class MallMediaUrlHelper {

    private final CommonConfigService commonConfigService;

    @Value("${houcloud.mall.public-domain:https://bbc.houjit.com/}")
    private String defaultPublicDomain;

    public MallMediaUrlHelper(CommonConfigService commonConfigService) {
        this.commonConfigService = commonConfigService;
    }

    /**
     * 前端 appStore.config.domain，必须以 / 结尾。
     */
    public String resolvePublicDomain() {
        String fromWebsite = commonConfigService.getOrDefault("website", "domain", "");
        if (StringUtils.hasText(fromWebsite)) {
            return ensureTrailingSlash(fromWebsite);
        }
        String engine = commonConfigService.getOrDefault("storage", "default", "local");
        if (StringUtils.hasText(engine) && !"local".equals(engine)) {
            Map<String, String> storage = commonConfigService.getJsonMap("storage", engine);
            String domain = storage.get("domain");
            if (StringUtils.hasText(domain)) {
                return ensureTrailingSlash(domain);
            }
        }
        return ensureTrailingSlash(defaultPublicDomain);
    }

  /**
   * 将装修/导航中的相对路径转为可访问的绝对 URL（仅处理以 / 开头的站内路径）。
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

    private static String ensureTrailingSlash(String domain) {
        if (!StringUtils.hasText(domain)) {
            return "";
        }
        return domain.endsWith("/") ? domain : domain + "/";
    }
}
