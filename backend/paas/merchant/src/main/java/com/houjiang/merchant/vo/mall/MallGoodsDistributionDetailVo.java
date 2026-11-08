package com.houjiang.merchant.vo.mall;

import com.houjiang.merchant.vo.distribution.DistributionLevelSimpleVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MallGoodsDistributionDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;
    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品货号")
    private String code;

    @Schema(description = "主图")
    private String image;

    @Schema(description = "分销规则 0默认 1单独佣金比例 2单独设置佣金")
    private Integer distributionType;

    @Schema(description = "参与分销 1是 0否")
    private Integer isDistribution;

    @Schema(description = "用户等级列表")
    private List<DistributionLevelSimpleVo> distributionLevel;

    private List<MallGoodsDistributionSkuVo> sku;


}
