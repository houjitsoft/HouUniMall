package com.houjiang.merchant.vo.distribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class DistributionLevelSimpleVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "分销等级")
    private Integer level;

    @Schema(description = "默认等级 1是 0否")
    private Integer isDefault;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "自购佣金比例")
    private String selfRatio;

    @Schema(description = "一级分销比例")
    private String oneRatio;

    @Schema(description = "二级分销比例")
    private String twoRatio;

    private List<MallGoodsDistributionSimpleVo> distributionList;


}
