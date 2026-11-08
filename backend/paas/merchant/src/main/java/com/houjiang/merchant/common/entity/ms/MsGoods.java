package com.houjiang.merchant.common.entity.ms;

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
public class MsGoods implements Serializable {

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

    @Schema(description = "秒杀id")
    private Integer msId;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "商品id")
    private Integer goodsId;

    @Schema(description = "商品sku id")
    private Integer goodsSkuId;

    @Schema(description = "秒杀价格")
    private BigDecimal price;

    @Schema(description = "秒杀价格 证整数")
    private Integer priceInt;

    @Schema(description = "总库存")
    private Integer totalStock;

    @Schema(description = "剩余库存")
    private Integer stock;

    @Schema(description = "销量")
    private Integer saleNum;

}