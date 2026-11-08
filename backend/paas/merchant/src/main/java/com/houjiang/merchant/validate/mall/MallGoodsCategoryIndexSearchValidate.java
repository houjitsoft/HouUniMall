package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallGoodsCategoryIndexSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "分类ID")
    private Integer categoryId;

}
