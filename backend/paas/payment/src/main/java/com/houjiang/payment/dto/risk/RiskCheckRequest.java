package com.houjiang.payment.dto.risk;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 风控检查请求（核心入参）
 * 由业务层调用 RiskCheckService.check(request) 进行风控评估
 *
 * @author Amos
 */
@Data
public class RiskCheckRequest {

    /** 业务类型：1-下单 2-退款 3-提现 4-转账 5-充值 */
    @NotNull(message = "业务类型不能为空")
    private Integer bizType;

    /** 业务单号 */
    private String bizNo;

    /** 商户号 */
    private String mchNo;

    /** 用户ID */
    private String userId;

    /** 用户IP */
    private String userIp;

    /** 金额（元） */
    private java.math.BigDecimal amount;

    /** 支付方式编码 */
    private String payMethod;

    /** 银行卡号（校验黑名单） */
    private String bankCardNo;

    /** 手机号 */
    private String phone;

    /** 身份证号 */
    private String idCardNo;

    /** 扩展上下文 JSON */
    private String extraContext;
}
