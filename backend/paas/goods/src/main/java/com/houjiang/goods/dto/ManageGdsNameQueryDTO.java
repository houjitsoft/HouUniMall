package com.houjiang.goods.dto;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName ManageGdsNameQueryDTO
 * @Description 查商品数据时封装条件
 * @Author Amos
 * @Date 2023/6/25 15:09
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGdsNameQueryDTO extends UserTokenIntegralDTO{

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 商品名
     */
    private String commonName;
    /**
     * 上架时间,发布时间
     */
    private Date commonSellTime;

    /**
     * 1:商城价2:小宝价格3:商城和小宝价组合
     */
    private Integer commonType;
    /**
     * 供应商店铺id
     */
    private Integer supplyShopId;

    /**
     * 经销商店铺id
     */
    private Integer dealerShopId;

    /**
     * 经销店铺名
     */
    private String dealerShopName;

    /**
     * 是否是虚拟商品 0是
     */
    private Integer virtualGoods;
    /**
     * 1普通商品   2供应商商品',
     */
    private Integer goodsType;

    /**
     * 0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品
     */
    private Integer dealerGoodsFlag;
    /**
     * 空查询全部 1 设置过国际运费模板 2 未设置过国际运费模板
     */
    private Integer internationalTransportFlag;

    /**
     * 1普通商品   2供应商商品',
     */
    private String plu;

    /**
     * 怡亚通商品分类
     */
    private String supplierSource;
}
