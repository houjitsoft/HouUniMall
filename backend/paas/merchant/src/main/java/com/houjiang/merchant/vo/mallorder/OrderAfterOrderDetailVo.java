package com.houjiang.merchant.vo.mallorder;

import com.houjiang.merchant.vo.user.UserVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class OrderAfterOrderDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "应付金额")
    private BigDecimal needPayMoney;

    @Schema(description = "实付金额")
    private BigDecimal payMoney;
}
