package com.houjiang.merchant.validate.gather;

import com.alibaba.fastjson2.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallGatherCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @NotNull(message = "采集类型缺失")
    @Schema(description = "采集类型")
    private Integer type;

    @NotNull(message = "平台分类缺失")
    @Schema(description = "平台分类")
    private Integer categoryId;

    @NotNull(message = "商品分类缺失")
    @Schema(description = "商品分类")
    private Integer shopCategoryId;

    @NotNull(message = "商品链接")
    @Schema(description = "商品链接")
    private JSONArray links;
}
