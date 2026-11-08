package com.houjiang.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Amos
 * @date 2021-05-2809:11
 * @Description: 商家经销商店铺等级折扣
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DealerShopInfoVO {
    /**
     * 经销商id
     */
    private String dealerShopId;

    /**
     * 供应商id
     */
    private List<Integer> supplyShopIdList;

}
