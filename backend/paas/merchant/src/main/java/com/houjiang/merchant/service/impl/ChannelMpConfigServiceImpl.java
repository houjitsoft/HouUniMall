package com.houjiang.merchant.service.impl;

import com.houjiang.merchant.service.IChannelMpConfigService;
import com.houjiang.merchant.validate.channel.ChannelMpValidate;
import com.houjiang.merchant.vo.channel.ChannelMpVo;
import com.houjiang.merchant.common.util.*;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 微信小程序渠道服务实现类
 */
@Service
public class ChannelMpConfigServiceImpl implements IChannelMpConfigService {

    /**
     * 微信小程序渠道详情
     *
     * @author Amos
     * @return ChannelMpVo
     */
    @Override
    public ChannelMpVo getConfig() {
        Map<String, String> config = ConfigUtils.get("mnp_setting");

        String env = YmlUtils.get("merchant.production");
        boolean envStatus = StringUtils.isNotNull(env) && env.equals("true");

        ChannelMpVo vo = new ChannelMpVo();
        vo.setName(config.getOrDefault("name", ""));
        vo.setOriginalId(config.getOrDefault("original_id", ""));
        vo.setAppId(envStatus ? "******" : config.getOrDefault("app_id", ""));
        vo.setAppSecret(envStatus ? "******" : config.getOrDefault("app_secret", ""));
        vo.setQrCode(UrlUtils.toShopAbsoluteUrl(config.getOrDefault("qr_code", "")));

        String domain = RequestUtils.domain();
        vo.setRequestDomain(domain);
        vo.setSocketDomain(domain.replace("https://", "wss://").replace("http://", "wss://"));
        vo.setUploadFileDomain(domain);
        vo.setDownloadFileDomain(domain);
        vo.setUdpDomain(domain.replace("https://", "udp://").replace("http://", "udp://"));
        vo.setTcpDomain(domain);
        vo.setBusinessDomain(domain);

        return vo;
    }

    /**
     * 微信小程序渠道保存
     *
     * @author Amos
     * @param channelMpValidate 参数
     */
    @Override
    public void setConfig(ChannelMpValidate channelMpValidate) {
        ConfigUtils.set("mnp_setting", "name", channelMpValidate.getName());
        ConfigUtils.set("mnp_setting", "original_id", channelMpValidate.getOriginalId());
        ConfigUtils.set("mnp_setting", "app_id", channelMpValidate.getAppId());
        ConfigUtils.set("mnp_setting", "app_secret", channelMpValidate.getAppSecret());
        ConfigUtils.set("mnp_setting", "qr_code", UrlUtils.toShopRelativeUrl(channelMpValidate.getQrCode()));
    }

}
