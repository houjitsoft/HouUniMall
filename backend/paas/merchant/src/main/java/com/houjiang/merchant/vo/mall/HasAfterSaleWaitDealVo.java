package com.houjiang.merchant.vo.mall;

import com.houjiang.merchant.common.entity.mall.MallOrderAfterSale;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class HasAfterSaleWaitDealVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "售后商品列表")
    private List<MallOrderAfterSaleGoodsListedVo> orderGoods;
}
