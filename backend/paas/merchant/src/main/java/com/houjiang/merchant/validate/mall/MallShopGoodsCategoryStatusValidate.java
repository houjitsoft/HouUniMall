package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商家商品分类参数
 * @author Amos
 */
@Data
@Schema(description = "商家商品分类更新状态参数")
public class MallShopGoodsCategoryStatusValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;
    @NotNull(message = "是否显示缺失")
    @Schema(description = "是否显示")
    private Integer isShow;

}
