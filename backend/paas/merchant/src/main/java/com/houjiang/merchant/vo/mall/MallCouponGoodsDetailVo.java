package com.houjiang.merchant.vo.mall;

import com.houjiang.merchant.vo.ms.GoodsMsVo;
import com.houjiang.merchant.vo.pt.GoodsPtVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MallCouponGoodsDetailVo implements Serializable {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品货号")
    private String code;
    @Schema(description = "主图")
    private String image;

    @Schema(description = "规格类型 0单规格 1多规格")
    private Integer skuType;
    @Schema(description = "规格类型 0单规格 1多规格")
    private String skuTypeText;
    @Schema(description = "状态 0仓库 1上架")
    private Integer status;
    @Schema(description = "状态 0仓库 1上架")
    private String statusText;
    @Schema(description = "价格")
    private String priceText;

    @Schema(description = "最小价格")
    private BigDecimal minPrice;

    @Schema(description = "最大价格")
    private BigDecimal maxPrice;

    @Schema(description = "最小划线价")
    private BigDecimal minLinePrice;

    @Schema(description = "最大划线价")
    private BigDecimal maxLinePrice;
}
