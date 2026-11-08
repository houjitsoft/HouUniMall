package com.houjiang.base.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealerApplyInfoDTO extends PageHelper {
    private Integer id;
    /**
     * 供应商店铺id
     */
    private Integer supplierShopId;

    private String supplierShopName;

    private Integer supplierUserId;

    private String supplierUserName;
    /**
     * 经销商id
     */
    private String dealerShopId;

    private String dealerShopName;

    private Integer dealerUserId;

    private String dealerUserName;

    private String dealerUserPhone;

    private Integer dealerStatus;

    private Integer unDealerStatus;

    private String dealerRemark;

    private Integer dealerLevelId;

    private String dealerLevelName;

    private Integer dealerDiscount;

    private String createTime;

    private Integer catStatus;

    private String startTime;

    private String endTime;

    private Integer operateType;

    private Integer commonId;

    private String dealerUserNickname;


}
