package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.channel.ChannelH5Validate;
import com.houjiang.merchant.vo.channel.ChannelH5Vo;

/**
 * H5渠道设置服务接口类
 */
public interface IChannelH5ConfigService {

    /**
     * H5设置详情
     *
     * @author Amos
     * @return ChannelH5Vo
     */
    ChannelH5Vo getConfig();

    /**
     * H5设置保存
     *
     * @author Amos
     * @param channelH5Validate 参数
     */
    void setConfig(ChannelH5Validate channelH5Validate);

}
