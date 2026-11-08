package com.houjiang.payment.dto.mch;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 平台商户产品开通/关闭请求 DTO
 * @author Amos
 */
@Data
public class MerchantProductOpenRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "商户号不能为空")
    private String mchNo;

    /** 产品编码：WX_PAY / ALI_PAY / QUICK_PAY / UNION_PAY / BALANCE 等 */
    @NotBlank(message = "产品编码不能为空")
    private String productCode;

    /** 产品名称 */
    private String productName;

    /** 目标状态：0-申请中 1-已开通 2-已停用 3-审核失败 */
    private Integer status;

    /** 单笔限额（元） */
    private BigDecimal singleLimit;

    /** 单日累计限额（元） */
    private BigDecimal dailyLimit;

    /** 单月累计限额（元） */
    private BigDecimal monthlyLimit;

    /** 备注 */
    private String remark;
}
