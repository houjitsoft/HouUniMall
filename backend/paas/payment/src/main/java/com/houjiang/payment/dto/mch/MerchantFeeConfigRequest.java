package com.houjiang.payment.dto.mch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 平台商户费率配置请求 DTO
 * @author Amos
 */
@Data
public class MerchantFeeConfigRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "商户号不能为空")
    private String mchNo;

    @NotBlank(message = "产品编码不能为空")
    private String productCode;

    /** 支付方式编码，空表示产品默认费率 */
    private String payMethod;

    /** 费率类型：1-按比例 2-按笔固定 3-混合 */
    @NotNull(message = "费率类型不能为空")
    private Integer feeType;

    /** 费率（比例），如 0.0038 表示 0.38% */
    private BigDecimal feeRate;

    /** 固定手续费（元） */
    private BigDecimal feeFixed;

    /** 手续费下限（元） */
    private BigDecimal feeMin;

    /** 手续费上限（元） */
    private BigDecimal feeMax;

    /** 生效时间 */
    private Date effectiveTime;

    /** 失效时间 */
    private Date expireTime;
}
