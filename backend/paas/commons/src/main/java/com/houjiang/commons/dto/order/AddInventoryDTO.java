package com.houjiang.commons.dto.order;

import lombok.Data;

/**
 * 增加库存所需参数
 * @author Amos
 */
@Data
public class AddInventoryDTO {

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 订单商品数量
     */
    private Integer orderGoodsNum;

    /**
     * 活动ID 0 普通点单 1 砍价（废弃） 2 秒杀 3 限时折扣 4拼团 5 满减增折
     */
    private Integer orderPromotionId;

    /**
     * 赠品订单上级店铺id
     */
    private Integer treasureOrderShopId;

    /**
     * 商品spu销量
     */
    private Integer commonSalenum;

}
