package com.houjiang.payment.dto.channel;

import lombok.Data;

/**
 * 渠道调用日志查询请求
 * @author Amos
 */
@Data
public class ChannelLogListRequest {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String logNo;
    private String bizNo;
    private String channelCode;
    private Integer result;
    private String startTime;
    private String endTime;
}
