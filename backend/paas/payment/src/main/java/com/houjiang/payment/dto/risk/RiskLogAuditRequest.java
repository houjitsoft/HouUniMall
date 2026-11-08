package com.houjiang.payment.dto.risk;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 风控日志审核请求
 *
 * @author Amos
 */
@Data
public class RiskLogAuditRequest {

    /** 日志单号 */
    @NotBlank(message = "日志单号不能为空")
    private String logNo;

    /** 审核状态：1-审核通过 2-审核驳回 */
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    /** 审核意见 */
    private String auditResult;
}
