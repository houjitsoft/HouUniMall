package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @ClassName ManageGdsCommonVO
 * @Author Amos
 * @Date 2023/6/27 14:33
 * @Version 1.0
 * @author Amos
 */
@Data
public class QueryDealerGoodsVO {

    /**
     * 商品名称
     */
    private String commonName;
    /**
     * 商品图片
     */
    private String commonImage;
    /**
     * 代发商品数量
     */
    private Integer dealerGoodsNum;

    /**
     * 分类名称，前台传入
     */
    private String catName;
    /**
     * 供应商商品id
     */
    private String supplyCommonId;

    /**
     * 前台传入
     */
    private Integer shopId;

    /**
     * 前台传入
     */
    private String shopName;


}
