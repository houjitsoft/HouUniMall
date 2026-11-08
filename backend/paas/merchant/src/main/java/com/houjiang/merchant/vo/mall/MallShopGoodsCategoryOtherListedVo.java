package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopGoodsCategoryOtherListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "名称")
    private String name;


    @Schema(description = "图标")
    private String image;

    @Schema(description = "排序")
    private Integer sort;
}
