package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.channel.ChannelOaValidate;
import com.houjiang.merchant.vo.channel.ChannelOaVo;


/**
 * 公众号渠道设置服务接口类
 */
public interface IChannelOaConfigService {

    /**
     * 公众号设置详情
     *
     * @author Amos
     * @return ChannelOaVo
     */
    ChannelOaVo getConfig();

    /**
     * 公众号设置保存
     *
     * @author Amos
     * @param channelOaValidate 参数
     */
    void setConfig(ChannelOaValidate channelOaValidate);

}
