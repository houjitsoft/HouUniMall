package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.channel.ChannelMpValidate;
import com.houjiang.merchant.vo.channel.ChannelMpVo;

/**
 * 微信小程序渠道服务接口类
 */
public interface IChannelMpConfigService {

    /**
     * 微信小程序设置详情
     *
     * @author Amos
     * @return ChannelMpVo
     */
    ChannelMpVo getConfig();

    /**
     * 微信小程序设置保存
     *
     * @author Amos
     * @param channelMpValidate 参数
     */
    void setConfig(ChannelMpValidate channelMpValidate);

}
