package com.houjiang.payment.dto.channel;

import lombok.Data;

/**
 * 渠道调用结果回写（用于成功率统计 + 熔断）
 * @author Amos
 */
@Data
public class ChannelResultReport {

    /** 渠道调用日志编号（由 routeSelect 返回）*/
    private String logNo;

    /** 渠道编码 */
    private String channelCode;

    /** 调用结果：1-成功 2-失败 */
    private Integer result;

    /** 耗时 ms */
    private Integer costMs;

    /** 错误码 */
    private String errorCode;

    /** 错误信息 */
    private String errorMsg;
}
