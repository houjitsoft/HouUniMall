package com.houjiang.base.vo;

import lombok.Data;

/**
 * @Date 2023/6/21 16:05
 * @Version 1.0
 * @author Amos
 */
@Data
public class AppIndexSlideBeanVO {

    private String slide;
    private String slideUrl;

    /**
     * 2商品编号{"goodsId":5026,"commonId":5026}，3链接{"url":http://},4店铺{"shopId":11},10:红包
     */
    private String opType;
}
