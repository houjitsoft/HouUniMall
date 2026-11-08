package com.houjiang.goods.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName SupplierDealerPutawayRecordEntity
 * @Description 商品供应记录表
 * @Author Amos
 * @Date 2024/6/7 15:44
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierDealerPutawayRecordVO {


    /**
     * 供应商商品spuid
     */
    private Integer supplyCommonId;

    /**
     * 供应商商品skuid
     */
    private Integer supplyGoodsId;

    /**
     * 供应商店铺id
     */
    private Integer supplierShopId;

    /**
     * 供应商店铺
     */
    private String supplierShopName;

    /**
     * 经销商品spuid
     */
    private Integer dealerCommonId;

    /**
     * 经销商品skuid
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
     * 1一键上架  2立即购买
     */
    private Integer dealerType;

    /**
     * 经销发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dealerTime;

    /**
     * 同步商品商品价格是否修改 1 是 2 否 未修改价格的商品不展示
     */
    private Integer isPriceUpdate;
}
