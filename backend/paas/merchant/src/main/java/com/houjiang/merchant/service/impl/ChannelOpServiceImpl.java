package com.houjiang.merchant.service.impl;

import com.houjiang.merchant.service.IChannelOpService;
import com.houjiang.merchant.validate.channel.ChannelOpValidate;
import com.houjiang.merchant.vo.channel.ChannelOpVo;
import com.houjiang.merchant.common.util.ConfigUtils;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.YmlUtils;
import org.springframework.stereotype.Service;

/**
 * 开放平台设置服务类
 */
@Service
public class ChannelOpServiceImpl implements IChannelOpService {

    /**
     * 开放平台设置详情
     *
     * @author Amos
     * @return ChannelOpVo
     */
    @Override
    public ChannelOpVo getConfig() {
        String appId = ConfigUtils.get("open_platform", "app_id", "");
        String appSecret = ConfigUtils.get("open_platform", "app_secret", "");

        String env = YmlUtils.get("merchant.production");
        boolean envStatus = StringUtils.isNotNull(env) && env.equals("true");

        ChannelOpVo vo = new ChannelOpVo();
        vo.setAppId(envStatus ? "******" : appId);
        vo.setAppSecret(envStatus ? "******" : appSecret);
        return vo;
    }

    /**
     * 开放平台设置保存
     *
     * @author Amos
     * @param opValidate 参数
     */
    @Override
    public void setConfig(ChannelOpValidate opValidate) {
        ConfigUtils.set("open_platform", "app_id", opValidate.getAppId());
        ConfigUtils.set("open_platform", "app_secret", opValidate.getAppSecret());
    }

}
