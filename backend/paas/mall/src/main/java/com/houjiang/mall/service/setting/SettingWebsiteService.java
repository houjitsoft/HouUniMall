package com.houjiang.mall.service.setting;

import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 网站设置 Service。
 * 对应 like-admin SettingWebsiteService + SettingCopyrightService + SettingProtocolService。
 *
 * @author Amos
 */
public interface SettingWebsiteService {

    /** 网站基础信息详情（website） */
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Map<String, Object> getWebsite();

    /** 网站基础信息保存 */
    /**
     * TODO 补充方法说明
     * @param params TODO
     */
    void setWebsite(Map<String, Object> params);

    /** 备案信息（copyright.config 为 JSON 数组：[{key,value}]） */
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<Map<String, String>> getCopyright();

    /** 备案信息保存（params 含 config:[{key,value}]） */
    /**
     * TODO 补充方法说明
     * @param params TODO
     */
    void setCopyright(Map<String, Object> params);

    /** 政策协议详情（service_title/service_content/privacy_title/privacy_content） */
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Map<String, Object> getAgreement();

    /** 政策协议保存 */
    /**
     * TODO 补充方法说明
     * @param params TODO
     */
    void setAgreement(Map<String, Object> params);

    /** 站点统计配置（clarity_code） */
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Map<String, Object> getSiteStatistics();

    /** 站点统计配置保存 */
    /**
     * TODO 补充方法说明
     * @param params TODO
     */
    void setSiteStatistics(Map<String, Object> params);
}
