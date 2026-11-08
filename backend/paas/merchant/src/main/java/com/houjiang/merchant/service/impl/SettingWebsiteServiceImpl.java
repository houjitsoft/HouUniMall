package com.houjiang.merchant.service.impl;

import com.houjiang.merchant.service.ISettingWebsiteService;
import com.houjiang.merchant.validate.setting.SettingWebsiteValidate;
import com.houjiang.merchant.vo.setting.SettingWebsiteVo;
import com.houjiang.merchant.common.util.ConfigUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 网站信息配置服务实现类
 */
@Service
public class SettingWebsiteServiceImpl implements ISettingWebsiteService {

    /**
     * 获取网站信息
     *
     * @author Amos
     * @return Map<String, String>
     */
    @Override
    public SettingWebsiteVo getWebsite() {
        Map<String, String> config = ConfigUtils.get("website");
        SettingWebsiteVo vo = new SettingWebsiteVo();
        vo.setName(config.getOrDefault("name", ""));
        vo.setWebFavicon(UrlUtils.toShopAbsoluteUrl(config.getOrDefault("web_favicon", "")));
        vo.setWebLogo(UrlUtils.toShopAbsoluteUrl(config.getOrDefault("web_logo", "")));
        vo.setLoginImage(UrlUtils.toShopAbsoluteUrl(config.getOrDefault("login_image", "")));
        vo.setShopName(config.getOrDefault("shop_name", ""));
        vo.setShopLogo(UrlUtils.toShopAbsoluteUrl(config.getOrDefault("shop_logo", "")));
        vo.setPcLogo(UrlUtils.toShopAbsoluteUrl(config.getOrDefault("pc_logo", "")));
        vo.setPcTitle(config.getOrDefault("pc_title", ""));
        vo.setPcIco(UrlUtils.toShopAbsoluteUrl(config.getOrDefault("pc_ico", "")));
        vo.setPcDesc(config.getOrDefault("pc_desc", ""));
        vo.setPcKeywords(config.getOrDefault("pc_keywords", ""));
        vo.setH5Favicon(UrlUtils.toShopAbsoluteUrl(config.getOrDefault("h5_favicon", "")));
        return vo;
    }

    /**
     * 保存网站信息
     *
     * @author Amos
     * @param websiteValidate 参数
     */
    @Override
    public void setWebsite(SettingWebsiteValidate websiteValidate) {
        ConfigUtils.set("website", "name", websiteValidate.getName());
        ConfigUtils.set("website", "web_logo", UrlUtils.toShopRelativeUrl(websiteValidate.getWebLogo()));
        ConfigUtils.set("website", "web_favicon", UrlUtils.toShopRelativeUrl(websiteValidate.getWebFavicon()));
        ConfigUtils.set("website", "login_image", UrlUtils.toShopRelativeUrl(websiteValidate.getLoginImage()));
        ConfigUtils.set("website", "h5_favicon", UrlUtils.toShopRelativeUrl(websiteValidate.getH5Favicon()));

        ConfigUtils.set("website", "shop_name", websiteValidate.getShopName());
        ConfigUtils.set("website", "shop_logo", UrlUtils.toShopRelativeUrl(websiteValidate.getShopLogo()));

        ConfigUtils.set("website", "pc_logo", UrlUtils.toShopRelativeUrl(websiteValidate.getPcLogo()));
        ConfigUtils.set("website", "pc_ico", UrlUtils.toShopRelativeUrl(websiteValidate.getPcIco()));
        ConfigUtils.set("website", "pc_title", websiteValidate.getPcTitle());
        ConfigUtils.set("website", "pc_desc", websiteValidate.getPcDesc());
        ConfigUtils.set("website", "pc_keywords", websiteValidate.getPcKeywords());
    }

}
