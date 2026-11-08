package com.houjiang.commons.dto.order;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class WapGoodsSaleSumDTO {

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
    private String goodsNum;

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
     * 销售数量
     */
    private Long goodsSaleNum;

    /**
     *商品销量
     */
    private Long commonSaleNum;


}
