package com.houjiang.merchant.common.entity.pt;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class PtGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "拼团id")
    private Integer ptId;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "商品id")
    private Integer goodsId;

    @Schema(description = "商品sku id")
    private Integer goodsSkuId;

    @Schema(description = "拼团价格")
    private BigDecimal price;

    @Schema(description = "拼团价格 正整数")
    private Integer priceInt;

    @Schema(description = "销量")
    private Integer saleNum;

}