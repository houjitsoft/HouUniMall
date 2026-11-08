package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 商品参数
 * @author Amos
 */
@Data
@Schema(description = "商品参与/不参与分销参数")
public class MallShopGoodsDistributionSetValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "ids参数缺失")
    @Schema(description = "ID")
    private List<Integer> ids;

    @Schema(description = "是否参与分销")
    @IntegerContains(values = {0, 1}, message = "是否参与分销参数错误")
    private Integer isDistribution;
}
