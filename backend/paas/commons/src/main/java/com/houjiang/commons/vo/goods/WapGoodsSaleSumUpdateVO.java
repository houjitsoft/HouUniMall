package com.houjiang.commons.vo.goods;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class WapGoodsSaleSumUpdateVO {

    /**
     * token
     */
    private String token;

    /**
     * 门店id
     */
    private Integer shopGateId;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品数据
     */
    private Integer goodsNum;

    /**
     * 1非聚宝商品
     * 2聚宝商品
     */
    private Integer goodsType;

    /**
     * 赠品订单上级店铺id
     */
    private Integer treasureOrderShopId;

    /**
     * 退货数量
     */
    private Integer returnNum;

    /**
     * 代金券编码
     */
    private String voucherCode;

    /**
     * 红包编码
     */
    private String redpacketCode;

    /**
     * 0 普通点单  2 秒杀 3 限时折扣 4拼团 5 满减赠 6 满减折
     */
    private Integer promotionId;
    /**
     * 活动ID
     */
    private Integer goodsPromotionId;

}
