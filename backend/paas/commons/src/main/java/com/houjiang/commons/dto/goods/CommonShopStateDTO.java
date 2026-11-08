package com.houjiang.commons.dto.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonShopStateDTO {

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺状态-3：开店成功 2:待审核付款 1:待审核资料 0:关闭
     */
    private Integer shopStatus;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）,2-店铺下架商品下架
     */
    private Integer commonState;

    /**
     * 1-上架 2-下架 0-关闭店铺手动下架,3-店铺下架商品下架
     */
    private Integer goodsIsShelves;

}
