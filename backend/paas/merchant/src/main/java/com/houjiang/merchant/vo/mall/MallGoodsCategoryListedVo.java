package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MallGoodsCategoryListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "父级ID")
    private Integer pid;

    @Schema(description = "等级")
    private Integer level;

    @Schema(description = "图标")
    private String image;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "是否存在父级")
    private Integer hasParent;

    @Schema(description = "商品数")
    private Integer goodsNum;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;


    @Schema(description = "子级")
    private List<MallGoodsCategoryListedVo> sons;
}
