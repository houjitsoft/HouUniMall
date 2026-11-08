package com.houjiang.commons.dto.base;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Amos
 * @date 2021-05-1911:23
 * @Description: 经销申请
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class DealerApplyDTO extends UserTokenBeanDTO {

    /**
     * 供应商店铺id
     */
    private Integer supplierShopId;

    /**
     * 供应商店铺名称
     */
    private String supplierShopName;

    /**
     * 经销商店铺id
     */
    private Integer dealerShopId;

    /**
     * 经销商店铺名
     */
    private String dealerShopName;

    /**
     * 分类id
     */
    private List<Integer> catIdList;

}
