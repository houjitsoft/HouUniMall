package com.houjiang.payment.dto.risk;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 黑白名单新增请求
 *
 * @author Amos
 */
@Data
public class BlacklistAddRequest {

    /** 名单类型：1-黑名单 2-白名单 */
    @NotNull(message = "名单类型不能为空")
    private Integer listType;

    /** 目标类型：1-用户ID 2-商户号 3-IP 4-银行卡 5-手机号 6-身份证号 */
    @NotNull(message = "目标类型不能为空")
    private Integer targetType;

    /** 目标取值 */
    @NotBlank(message = "目标取值不能为空")
    private String targetValue;

    /** 业务作用域：0-全部 1-下单 2-退款 3-提现 4-转账 */
    private Integer bizScope = 0;

    /** 加入原因 */
    private String reason;

    /** 风险等级：1-低 2-中 3-高 */
    private Integer riskLevel = 1;

    /** 生效时间 */
    private Date effectiveTime;

    /** 失效时间 */
    private Date expireTime;
}
