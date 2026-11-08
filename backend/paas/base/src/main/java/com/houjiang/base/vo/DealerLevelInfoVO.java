package com.houjiang.base.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealerLevelInfoVO {

    private Integer dealerLevelId;
    /**
     * 供应商店铺id
     */
    private Integer supplierShopId;

    private String dealerLevelName;

    private Integer dealerDiscount;

    private Integer dealerLevelSort;

    private Integer dealerLevelStatus;

    private Integer operateType;

}
