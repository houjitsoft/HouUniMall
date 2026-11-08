package com.houjiang.goods.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 管理名称
 * @author Amos
 */
@Data
public class ManageGdsNameQueryParamDTO extends PageHelper {

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 商品spu名称
     */
    private String commonName;

    /**
     * 上架时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
     * 1普通商品 2供应商商品
     */
    private Integer goodsType;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架,3商品删除,4定时发布，5放入仓库
     */
    private Integer commonState;

    /**
     *是否经销商品  0普通商品  1 一键代发(商家一键代发供应商商品)  2商家购买供应商商品 3 一键铺货
     */
    private String dealerGoodsFlag;

    /**
     * 商品分类
     */
    private String catId;
    /**
     * 商品分类
     */
    private List<Integer> catIds;

    /**
     * 怡亚通商品分类
     */
    private String categoryId;

    /**
     * 怡亚通商品分类
     */
    private String supplierSource;
}
