package com.houjiang.goods.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName GoodsDealerPutawayRecordEntity
 * @Description 商品供应记录
 * @Author Amos
 * @Date 2023/6/26 11:25
 * @Version 1.0
 * @author Amos
 */
@Data
public class GoodsDealerPutawayRecordEntity {

    private Integer id;
    /**
     * 供应商商品commonId
     */
    private Integer  supplyCommonId;
    /**
     * 立即购买供应商品id
     */
    private Integer supplyGoodsId;
    /**
     * 经销商品commonId
     */
    private Integer  dealerCommonId;
    /**
     * 立即购买经销商品id（一键上架全部上架值为0）
     */
    private Integer dealerGoodsId;
    /**
     * 经销店铺id
     */
    private Integer dealerShopId;
    /**
     * 经销店铺名
     */
    private String dealerShopName;
    /**
     * 供应商店铺id
     */
     private Integer supplierShopId;
    /**
     * 供应商店铺
     */
    private String supplierShopName;
    /**
     * 经销供货价
     */
    private BigDecimal dealerPrice;
    /**
     * 上架数量
     */
    private Integer dealerNum;
    /**
     * 一键上架记录状态  1正常  2取消
     */
    private Integer recordState;
    /**
     * "1一键上架  2立即购买
     */
    private Integer dealerType;
    /**
     * 经销发布时间
     */
    private Date dealerTime;
    /**
     * 同步商品商品价格是否修改 1 是 2 否 未修改价格的商品不展示
     */
    private Integer isPriceUpdate;

}
