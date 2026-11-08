package com.houjiang.base.vo;

import lombok.Data;

/**
 * @author Amos
 * @version 1.0.0
 * @ClassName WxSubmitGoodsResult.java
 * @Description TODO
 * @createTime 2021年08月19日 13:49:00
 */
@Data
public class WxSubmitGoodsVO {

    /**
     * 商城id
     */
    private Integer goodsId;

    /**
     * 审核ID
     */
    private Integer auditId;

    /**
     * 错误代码
     */
    private Integer errcode;

}
