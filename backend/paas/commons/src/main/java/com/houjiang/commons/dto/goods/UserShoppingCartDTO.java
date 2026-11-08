package com.houjiang.commons.dto.goods;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车
 *
 * @author Amos
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserShoppingCartDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 店铺Id
     */
    private Integer shopId;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品id集合
     */
    private List<?> goodsIds;

    /**
     * 商品名
     */
    private String goodsName;

    /**
     * 商品图
     */
    private String goodsImage;

    /**
     * 数量
     */
    private Integer goodsNum;

    /**
     * 商品库存
     */
    private Integer goodsStock;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品总价
     */
    private BigDecimal goodsTotalPrice;

    /**
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 规格中文值
     */
    private String goodsSpecChineseValue;

    /**
     * 是否上架1:上架
     */
    private Integer goodsIsShelves;

    /**
     * 规格集合
     */
    private List<?> specification;

    /**
     * 最大购买数量
     */
    private Integer goodsMaxSale;

    /**
     * 是否经销商品  0普通商品   1经销商品(商家一键上架供应商商品)
     * 2购买经销商品（商家通过立即购买供应商上架的商品）
     */
    private Integer dealerGoodsFlag;


}
