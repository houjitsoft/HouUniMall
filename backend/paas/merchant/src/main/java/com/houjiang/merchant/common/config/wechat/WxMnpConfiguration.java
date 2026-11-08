package com.houjiang.merchant.common.config.wechat;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.binarywang.wxpay.service.WxPayService;
import com.houjiang.merchant.common.entity.Config;
import com.houjiang.merchant.common.mapper.ConfigMapper;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Configuration
@ConditionalOnClass(WxPayService.class)
@AllArgsConstructor
public class WxMnpConfiguration {

    @Resource
    private final ConfigMapper systemConfigMapper;

    /**
     * 微信小程序配置
     *
     * @author Amos
     * @return WxMaService
     */
    @Bean
    @ConditionalOnMissingBean
    public WxMaService wxMnpService() {
        Map<String, String> config = this.getChannelConfig("mnp_setting");

        WxMaDefaultConfigImpl wxConfig = new WxMaDefaultConfigImpl();
        wxConfig.setAppid(config.getOrDefault("app_id", ""));
        wxConfig.setSecret(config.getOrDefault("app_secret", ""));

        WxMaService wxService = new WxMaServiceImpl();
        wxService.setWxMaConfig(wxConfig);
        return wxService;
    }

    /**
     * 微信公众号配置
     *
     * @author Amos
     * @return WxMpService
     */
    @Bean
    @ConditionalOnMissingBean
    public WxMpService wxOaService() {
        Map<String, String> config = this.getChannelConfig("oa_setting");

        WxMpDefaultConfigImpl wxMpDefaultConfig = new WxMpDefaultConfigImpl();
        wxMpDefaultConfig.setAppId(config.getOrDefault("app_id", "").trim());
        wxMpDefaultConfig.setSecret(config.getOrDefault("app_secret", "").trim());
        wxMpDefaultConfig.setToken(config.getOrDefault("token", "").trim());
        wxMpDefaultConfig.setAesKey(config.getOrDefault("encoding_aes_key", "").trim());

        WxMpService wxService = new WxMpServiceImpl();
        wxService.setWxMpConfigStorage(wxMpDefaultConfig);
        return wxService;
    }

    /**
     * 配置读取
     *
     * @author Amos
     * @param type 类型
     * @return Map<String, String>
     */
    private Map<String, String> getChannelConfig(String type) {
        List<Config> configs = systemConfigMapper.selectList(
                    new QueryWrapper<Config>()
                        .select("id", "type", "name", "value")
                        .eq("type", type));

        Map<String, String> map = new LinkedHashMap<>();
        for (Config config : configs) {
            map.put(config.getName(), config.getValue() != null ? config.getValue() : "");
        }

        return map;
    }

}
