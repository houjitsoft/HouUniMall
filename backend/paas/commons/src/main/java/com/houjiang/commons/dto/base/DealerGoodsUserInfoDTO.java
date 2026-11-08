package com.houjiang.commons.dto.base;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Amos
 * @date 2021-05-1022:50
 * @Description: 商品详情页用户和商品按钮显示关系查询
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class DealerGoodsUserInfoDTO extends UserTokenBeanDTO {

    /**
     * 商品id  spu
     */
    private Integer commonId;

    /**
     * 商品id  sku
     */
    private Integer goodsId;


    /**
     * 供应商店铺id
     */
    private Integer supplierShopId;

    /**
     * 经销商id
     */
    private  String dealerShopId;

    /**
     * 商品id
     */
    private List<Integer> commonIdList;

    private Integer sellDeliver;

    /**
     * 分销状态 1待审核  2审核通过 3审核不通过  4已删除
     */
    private Integer dealerStatus;

    public DealerGoodsUserInfoDTO(Integer supplierShopId, String dealerShopId) {
        this.supplierShopId = supplierShopId;
        this.dealerShopId = dealerShopId;
    }
}
