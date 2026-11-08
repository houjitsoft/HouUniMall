package com.houjiang.payment.dto.channel;

import lombok.Data;

/**
 * 渠道列表查询请求
 * @author Amos
 */
@Data
public class ChannelListRequest {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String channelCode;
    private String provider;
    private String payMethod;
    private Integer status;
    private String merchantNo;
}
