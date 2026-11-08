package com.houjiang.merchant.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallShopConfig;
import com.houjiang.merchant.common.enums.OrderSettingEnum;
import com.houjiang.merchant.common.mapper.mall.MallShopConfigMapper;
import com.houjiang.merchant.service.IConfigService;
import com.houjiang.merchant.common.config.ProjectConfig;
import com.houjiang.merchant.common.util.*;
import com.houjiang.merchant.service.IDecoratePageService;
import com.houjiang.merchant.validate.config.ConfigSetServiceValidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import javax.management.Query;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配置实现类
 */
@Slf4j
@Service
public class ConfigServiceImpl implements IConfigService {

    @Resource
    IDecoratePageService iDecoratePageService;
    @Resource
    MallShopConfigMapper mallShopConfigMapper;
    /**
     * 公共配置
     *
     * @author Amos
     * @return Map<String, Object>
     */
    @Override
    public JSONObject getConfigData() {
        JSONObject ret = new JSONObject();
        Map<String, String> website  = ConfigUtils.get("website");
        JSONObject pConfig = ProjectConfig.website;
        // 文件域名
        String copyright = ConfigUtils.get("copyright", "config", "");
        // 文件域名
        ret.put("oss_domain", UrlUtils.domain());
        // 网站名称
        ret.put("web_name", website.getOrDefault("name", ""));
        // 网站图标
        ret.put("web_favicon", UrlUtils.toShopAbsoluteUrl(website.getOrDefault("web_favicon", "")));
        // 商家登录端标题
        ret.put("shop_login_title", StringUtils.isNull(website.get("shop_login_title")) ? "" : website.get("shop_login_title"));

        // 商家后台logo
        ret.put("shop_admin_logo", UrlUtils.toShopAbsoluteUrl(StringUtils.isNull(website.get("shop_admin_logo")) ? pConfig.getString("shop_admin_logo") : website.get("shop_admin_logo")));

        // 浏览器图标
        ret.put("shop_browser_icon", UrlUtils.toShopAbsoluteUrl(StringUtils.isNull(website.get("shop_browser_icon")) ? pConfig.getString("shop_browser_icon") : website.get("shop_browser_icon")));

        // 商家登录封面
        ret.put("shop_login_logo", UrlUtils.toShopAbsoluteUrl(StringUtils.isNull(website.get("shop_login_logo")) ? pConfig.getString("shop_login_logo") : website.get("shop_login_logo")));

        // 商家默认logo
        ret.put("shop_logo", UrlUtils.toShopAbsoluteUrl(StringUtils.isNull(website.get("shop_logo")) ? pConfig.getString("shop_logo") : website.get("shop_logo")));

        // 商家默认背景
        ret.put("shop_background", UrlUtils.toShopAbsoluteUrl(StringUtils.isNull(website.get("shop_logo")) ? pConfig.getString("shop_background") : website.get("shop_background")));
        // 版权信息
        ret.put("copyright_config", ListUtils.stringToListAsMapStr(copyright));

        //客服
        try {
            ret.put("admin", new JSONObject() {{
                put("kefu", iDecoratePageService.detail(3));
            }});
        } catch (Exception e) {
            // 装修页面(id=3)不存在时返回空对象，不中断整个getConfig
            log.warn("客服装修页面(id=3)不存在，返回空对象: {}", e.getMessage());
            ret.put("admin", new JSONObject() {{
                put("kefu", null);
            }});
        }

        return ret;
    }

    @Override
    public JSONObject settlementGet(Integer shopId) {
        JSONObject ret = new JSONObject();
        ret.put("settlement", this.getMallOrderSettlementG1et());
        ret.put("enum_week", OrderSettingEnum.SETTLEMENT_CYCLE_WEEK_TEXT_DAYS);
        ret.put("enum_month", OrderSettingEnum.getMonthTextDays());
        ret.put("enum_cycle", OrderSettingEnum.SETTLEMENT_CYCLE_TEXT_ARR);
        return ret;
    }

    @Override
    public JSONObject getMallOrderSettlementG1et() {
        return new JSONObject() {{
            put("settle_cycle", Integer.parseInt(ConfigUtils.get("mall-order-settlement", "settle_cycle", String.valueOf(OrderSettingEnum.SETTLEMENT_CYCLE_DAY).replace("null", String.valueOf(OrderSettingEnum.SETTLEMENT_CYCLE_DAY)))));
            put("settle_cycle_week_day", Integer.parseInt(ConfigUtils.get("mall-order-settlement", "settle_cycle_week_day", "1").replace("null", "1")));
            put("settle_cycle_month_day", Integer.parseInt(ConfigUtils.get("mall-order-settlement", "settle_cycle_month_day", "1").replace("null", "1")));
        }};
    }

    @Override
    public void setService(ConfigSetServiceValidate serviceValidate) {
        JSONObject mnp = serviceValidate.getMnp();
        JSONObject h5 = serviceValidate.getH5();
        JSONObject oa = serviceValidate.getOa();
        mnp.put("qr_code", UrlUtils.toShopRelativeUrl(mnp.getString("qr_code")));
        oa.put("qr_code", UrlUtils.toShopRelativeUrl(oa.getString("qr_code")));
        h5.put("qr_code", UrlUtils.toShopRelativeUrl(h5.getString("qr_code")));

        this.setMallShopConfg(serviceValidate.getShopId(), "service_config", "mnp", mnp);
        this.setMallShopConfg(serviceValidate.getShopId(), "service_config", "h5", h5);
        this.setMallShopConfg(serviceValidate.getShopId(), "service_config", "oa", oa);
    }

    private void setMallShopConfg(Integer shopId, String type, String name, JSONObject value) {
        MallShopConfig mnpConfig = mallShopConfigMapper.selectOne(new QueryWrapper<MallShopConfig>().eq("shop_id", shopId).eq("type", type).eq("name", name).eq("delete_time", 0));
        if (StringUtils.isNull(mnpConfig)) {
            mnpConfig = new MallShopConfig();
            mnpConfig.setShopId(shopId);
            mnpConfig.setType(type);
            mnpConfig.setName(name);
            mnpConfig.setValue(JSONObject.toJSONString(value));
            mnpConfig.setCreateTime(System.currentTimeMillis() / 1000);
            mnpConfig.setUpdateTime(System.currentTimeMillis() / 1000);
            mallShopConfigMapper.insert(mnpConfig);
        } else {
            mnpConfig.setValue(JSONObject.toJSONString(value));
            mnpConfig.setUpdateTime(System.currentTimeMillis() / 1000);
            mallShopConfigMapper.updateById(mnpConfig);
        }
    }

    @Override
    public JSONObject getService(Integer shopId) {
        JSONObject defaultJSON = new JSONObject();
        defaultJSON.put("way", "1");
        defaultJSON.put("name", "");
        defaultJSON.put("remarks", "");
        defaultJSON.put("phone", "");
        defaultJSON.put("business_time", "");
        defaultJSON.put("qr_code", "");
        defaultJSON.put("enterprise_id", "");
        defaultJSON.put("kefu_link", "");

        JSONObject mnp = this.getShopConfigName(shopId, "service_config", "mnp");
        if (StringUtils.isNull(mnp)) {
            mnp = defaultJSON;
        } else {
            mnp.put("qr_code", UrlUtils.toShopAbsoluteUrl(UrlUtils.toRelativeUrl(mnp.getString("qr_code"))));
        }

        JSONObject oa = this.getShopConfigName(shopId, "service_config", "oa");
        if (StringUtils.isNull(oa)) {
            oa = defaultJSON;
        } else {
            oa.put("qr_code", UrlUtils.toShopAbsoluteUrl(UrlUtils.toRelativeUrl(oa.getString("qr_code"))));
        }

        JSONObject h5 = this.getShopConfigName(shopId, "service_config", "h5");
        if (StringUtils.isNull(h5)) {
            h5 = defaultJSON;
        } else {
            h5.put("qr_code", UrlUtils.toShopAbsoluteUrl(UrlUtils.toRelativeUrl(h5.getString("qr_code"))));
        }
        JSONObject ret = new JSONObject();
        ret.put("mnp", mnp);
        ret.put("oa", oa);
        ret.put("h5", h5);
        return ret;
    }

    @Override
    public JSONObject getShopConfigName(Integer shopId, String type, String name) {
        QueryWrapper queryWrapper = new QueryWrapper<MallShopConfig>();
        queryWrapper.eq("shop_id", shopId);
        queryWrapper.eq("type", type);
        queryWrapper.eq("name", name);
        queryWrapper.eq("delete_time", 0);
        MallShopConfig shopConfig = mallShopConfigMapper.selectOne(queryWrapper);
        return StringUtils.isNull(shopConfig) ? null : JSONObject.parseObject(shopConfig.getValue());
    }
}
