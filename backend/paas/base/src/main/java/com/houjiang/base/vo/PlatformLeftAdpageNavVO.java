package com.houjiang.base.vo;

import lombok.Data;

/**
 * @ClassName PlatformLeftAdpageNav
 * @Description 首页左侧分类
 * @Author Amos
 * @Date 2023/4/12 14:53
 * @Version 1.0
 * @author Amos
 */
@Data
public class PlatformLeftAdpageNavVO {

    /**
     * 左侧分类名称
     */
    private String leftAdpageNavName;

    /**
     * 左侧分类地址
     */
    private String leftAdpageNavUrl;

    /**
     * 1关键字，2商品编号{"goodsId":5026,"commonId":5026}，3链接{"url":http://},5店铺{"shopId":11}6分类id {\"catId\":10000104}
     */
    private Integer opType;
    /**
     * 商品名
     */
    private  String commonName;
    /**
     * 店铺名
     */
    private  String shopName;
    /**
     * 类别名
     */
    private  String catName;
}
