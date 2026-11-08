package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import jakarta.validation.constraints.*;
import com.houjiang.merchant.common.validator.annotation.IDMust;

/**
 * 商家商品分类参数
 * @author Amos
 */
@Data
@Schema(description = "商家商品分类更新参数")
public class MallShopGoodsCategoryUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @NotNull(message = "请填写分类名称")
    @Schema(description = "名称")
    private String name;

    @NotNull(message = "请填写分类图片")
    @Schema(description = "图标")
    private String image;

    @Schema(description = "排序")
    private Integer sort;

    @NotNull(message = "是否显示缺失")
    @Schema(description = "是否显示")
    private Integer isShow;

}
