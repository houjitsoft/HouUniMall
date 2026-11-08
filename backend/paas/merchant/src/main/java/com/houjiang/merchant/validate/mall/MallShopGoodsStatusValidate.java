package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商家商品状态参数
 * @author Amos
 */
@Data
@Schema(description = "商家商品更新状态参数")
public class MallShopGoodsStatusValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;
    @NotNull(message = "上架状态缺失")
    @Schema(description = "商家状态")
    private Integer status;

}
