package com.houjiang.payment.dto.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 渠道保存请求（新增/更新）
 * @author Amos
 */
@Data
public class ChannelSaveRequest {

    /** 新增时为空，更新时必填 */
    private Long id;

    @NotBlank(message = "渠道编码必填")
    private String channelCode;

    @NotBlank(message = "渠道名称必填")
    private String channelName;

    @NotBlank(message = "服务商必填")
    private String provider;

    @NotBlank(message = "支付方式必填")
    private String payMethod;

    @NotNull(message = "优先级必填")
    private Integer priority;

    @NotNull(message = "权重必填")
    private Integer weight;

    private BigDecimal dailyLimit;
    private BigDecimal singleLimit;
    private BigDecimal singleMin;
    private BigDecimal feeRate;
    private String fallbackCodes;
    private Integer status;
    private String merchantNo;
    private String configJson;
    private String remark;
}
