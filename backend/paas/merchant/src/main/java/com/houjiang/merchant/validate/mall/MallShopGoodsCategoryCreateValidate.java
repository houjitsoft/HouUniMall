package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import jakarta.validation.constraints.*;

/**
 * @author Amos
 */
@Data
public class MallShopGoodsCategoryCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @NotNull(message = "必须填写分类名称")
    @Schema(description = "名称")
    private String name;

    @NotNull(message = "必须上传分类图片")
    @Schema(description = "图标")
    private String image;

    @Schema(description = "排序")
    private Integer sort;

    @NotNull(message = "isShow参数缺失")
    @Schema(description = "是否显示")
    private Integer isShow;

}
