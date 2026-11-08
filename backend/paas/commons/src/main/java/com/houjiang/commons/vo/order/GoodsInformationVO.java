package com.houjiang.commons.vo.order;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品编号查询具体信息
 *
 * @author Amos
 */
@Data
public class GoodsInformationVO {

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 是否是同一个团的，对应hou_pingtuan_mark表
     */
    private String markId;

    /**
     * 1普通商品  2供应商商品
     */
    private Integer goodsType;

    /**
     * 0拼团，1单独购买
     */
    private Integer type;

    private Integer commonId;
    /**
     * 商品名
     */
    private String goodsName;
    /**
     * 商品价格
     */
    private Double goodsPrice;


}
