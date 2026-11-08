package com.houjiang.base.vo.index;

import lombok.Data;

import java.math.BigDecimal;
/**
 * @ClassName PcConfigGoodsVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class PcConfigGoodsVO {

    private String commonName;

    private String commonImage;

    private String shopName;

    private Integer commonEvaluate;
    /**
     * 商品价格
     */
    private BigDecimal commonPrice;
    /**
     * 市场价
     */
    private BigDecimal commonMarketPrice;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 货品id
     */
    private Integer goodsId;
    /**
     * 收藏量
     */
    private Integer commonCollect;
    /**
     * 销售量
     */
    private Integer commonSalenum;
    /**
     * 是否自营店铺
     */
    private String shopSelfSupport;
    /**
     * 是否plus商品
     */
    private Integer plusGoods;


}
