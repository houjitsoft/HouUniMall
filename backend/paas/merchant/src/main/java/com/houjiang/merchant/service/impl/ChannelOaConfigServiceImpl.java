package com.houjiang.merchant.service.impl;

import com.houjiang.merchant.service.IChannelOaConfigService;
import com.houjiang.merchant.validate.channel.ChannelOaValidate;
import com.houjiang.merchant.vo.channel.ChannelOaVo;
import com.houjiang.merchant.common.util.*;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 公众号渠道设置服务实现类
 */
@Service
public class ChannelOaConfigServiceImpl implements IChannelOaConfigService {

    /**
     * 公众号渠道设置详情
     *
     * @author Amos
     * @return ChannelOaVo
     */
    @Override
    public ChannelOaVo getConfig() {
        Map<String, String> config = ConfigUtils.get("oa_setting");
        ChannelOaVo vo = new ChannelOaVo();

        String env = YmlUtils.get("merchant.production");
        boolean envStatus = StringUtils.isNotNull(env) && env.equals("true");
        String domain = RequestUtils.domain();

        vo.setQrCode(UrlUtils.toShopAbsoluteUrl(config.getOrDefault("qr_code", "")));
        vo.setName(config.getOrDefault("name", ""));
        vo.setOriginalId(config.getOrDefault("original_id", ""));
        vo.setAppId(envStatus ? "******" : config.getOrDefault("app_id", ""));
        vo.setAppSecret(envStatus ? "******" : config.getOrDefault("app_secret", ""));
        vo.setUrl(domain + (StringUtils.isNotEmpty(config.getOrDefault("url", "")) ? config.getOrDefault("url", "") : "/adminapi/channel/oa/callback"));
        vo.setToken(config.getOrDefault("token", ""));
        vo.setEncodingAesKey(config.getOrDefault("encoding_aes_key", ""));
        vo.setEncryptionType(Integer.parseInt(config.getOrDefault("encryption_type", "1")));

        vo.setBusinessDomain(domain);
        vo.setJsSecureDomain(domain);
        vo.setWebAuthDomain(domain);

        return vo;
    }

    /**
     * 公众号渠道设置保存
     *
     * @author Amos
     * @param channelOaValidate 参数
     */
    @Override
    public void setConfig(ChannelOaValidate channelOaValidate) {
        ConfigUtils.set("oa_setting", "name", channelOaValidate.getName());
        ConfigUtils.set("oa_setting", "original_id", channelOaValidate.getOriginalId());
        ConfigUtils.set("oa_setting", "qr_code", UrlUtils.toShopRelativeUrl(channelOaValidate.getQrCode()));
        ConfigUtils.set("oa_setting", "app_id", channelOaValidate.getAppId());
        ConfigUtils.set("oa_setting", "app_secret", channelOaValidate.getAppSecret());
//        ConfigUtils.set("oa_setting", "url", channelOaValidate.getUrl());
        ConfigUtils.set("oa_setting", "token", channelOaValidate.getToken());
        ConfigUtils.set("oa_setting", "encoding_aes_key", channelOaValidate.getEncodingAesKey());
        ConfigUtils.set("oa_setting", "encryption_type", String.valueOf(channelOaValidate.getEncryptionType()));
    }

}
