package com.houjiang.merchant.validate.mallorder;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallOrderSendDeliveryValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "orderId参数必传且需大于0")
    @Schema(description = "订单ID", required = true)
    private Integer id;

    @IntegerContains(values = {0,1})
    @Schema(description = "配送方式: [1=快递配送, 0=无需快递]", required = true)
    private Integer hasExpress;

    @Schema(description = "物流公司")
    private Integer expressCompanyId;

    @Schema(description = "物流单号")
    private String expressNo;

    @Schema(description = "发货备注")
    private String remark;

}
