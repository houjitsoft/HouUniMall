package com.houjiang.payment.dto.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 路由规则保存请求
 * @author Amos
 */
@Data
public class RouteSaveRequest {

    private Long id;

    @NotBlank(message = "规则编号必填")
    private String routeNo;

    @NotBlank(message = "规则名称必填")
    private String routeName;

    @NotNull(message = "业务类型必填")
    private Integer bizType;

    private String payMethod;
    private String terminal;
    private String merchantNo;
    private BigDecimal amountMin;
    private BigDecimal amountMax;

    @NotBlank(message = "候选渠道必填")
    private String channelCodes;

    /** 路由策略：PRIORITY/WEIGHT/RANDOM */
    private String strategy = "WEIGHT";

    @NotNull(message = "优先级必填")
    private Integer priority;

    private Integer status;
    private Date effectiveTime;
    private Date expireTime;
    private String remark;
}
