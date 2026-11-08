package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallRefundReRefundValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "店铺id")
    private Integer shopId;

}
