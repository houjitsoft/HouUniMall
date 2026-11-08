package com.houjiang.payment.dto.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 补偿任务注册请求（支付/退款/提现 发起时失败或未知结果时调用，由定时任务异步反查）
 * @author Amos
 */
@Data
public class CompensateRegisterRequest {

    @NotBlank(message = "业务订单号必填")
    private String bizNo;

    @NotNull(message = "业务类型必填")
    private Integer bizType;

    @NotBlank(message = "渠道编码必填")
    private String channelCode;

    /** 最大查询次数（默认 10） */
    private Integer maxQuery;

    /** 首次延迟秒数（默认 30s） */
    private Integer delaySeconds;
}
