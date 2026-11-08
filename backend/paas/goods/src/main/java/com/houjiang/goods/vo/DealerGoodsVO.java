package com.houjiang.goods.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Amos
 * @date 2021-05-2916:05
 * @Description: 经销商品
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DealerGoodsVO {
    /**
     * 供应商店铺id
     */
    private Integer supplierShopId;
    /**
     * 经销商id
     */
    private String dealerShopId;

    /**
     * 一键上架记录状态  1正常  2取消
     */
    private Integer recordState;
    /**
     * 经销商类型
     */
    private Integer dealerType;

    /**
     * 供应商商品id
     */
    private List<Integer> supplyGoodsIdList;
    /**
     * 供应商商品id
     */
    private List<Integer> supplyCommonIdList;

    public DealerGoodsVO(String dealerShopId, Integer recordState, Integer dealerType, List<Integer> supplyGoodsIdList) {
        this.dealerShopId = dealerShopId;
        this.recordState = recordState;
        this.dealerType = dealerType;
        this.supplyGoodsIdList = supplyGoodsIdList;
    }

    public DealerGoodsVO(String dealerShopId, Integer recordState, Integer dealerType) {
        this.dealerShopId = dealerShopId;
        this.recordState = recordState;
        this.dealerType = dealerType;
    }
}
