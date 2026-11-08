package com.houjiang.merchant.vo.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MarketingRechargeVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否开启充值: 0=否,1=是")
    private Integer status;

    @Schema(description = "最低充值金额")
    private BigDecimal minAmount;

}
