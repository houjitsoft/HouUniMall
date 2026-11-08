package com.houjiang.merchant.vo.coupon;

import com.houjiang.merchant.vo.mall.MallCouponGoodsDetailVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class CouponGooodsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "优惠券商品详情")
    private MallCouponGoodsDetailVo goodsDetail;
}
