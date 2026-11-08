package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.channel.ChannelOpValidate;
import com.houjiang.merchant.vo.channel.ChannelOpVo;

/**
 * 微信开放渠道设置接口服务类
 */
public interface IChannelOpService {

    /**
     * 开放平台设置详情
     *
     * @author Amos
     * @return ChannelOpVo
     */
    ChannelOpVo getConfig();

    /**
     * 开放平台设置保存
     *
     * @author Amos
     * @param opValidate 参数
     */
    void setConfig(ChannelOpValidate opValidate);

}
