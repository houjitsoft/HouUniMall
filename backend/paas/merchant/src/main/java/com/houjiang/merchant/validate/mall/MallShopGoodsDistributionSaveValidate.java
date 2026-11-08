package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import com.houjiang.merchant.vo.distribution.DistributionLevelSimpleVo;
import com.houjiang.merchant.vo.user.DiscountUserListedVo;
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
@Schema(description = "设置分销参数")
public class MallShopGoodsDistributionSaveValidate implements Serializable {
    private static final long serialVersionUID = 1L;
    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "分销规则 0默认 1单独佣金比例 2单独设置佣金")
    @IntegerContains(values = {0, 1, 2}, message = "分销规则参数错误")
    @NotNull(message = "提交分销规则")
    private Integer distributionType;

    @Schema(description = "参与分销 1是 0否")
    @IntegerContains(values = {0, 1}, message = "是否参与分销参数错误")
    @NotNull(message = "提交参与分销")
    private Integer isDistribution;

    @Schema(description = "用户等级列表")
    @NotNull(message = "用户分销等级列表")
    private List<DistributionLevelSimpleVo> distributionLevel;
}
