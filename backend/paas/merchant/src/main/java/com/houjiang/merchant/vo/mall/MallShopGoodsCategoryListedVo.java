package com.houjiang.merchant.vo.mall;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopGoodsCategoryListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "图标")
    private String image;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "排序")
    @JsonProperty("goods_num")
    private Long goodsNum;
}
