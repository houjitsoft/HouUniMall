package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallGoodsCategorySearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

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

}
