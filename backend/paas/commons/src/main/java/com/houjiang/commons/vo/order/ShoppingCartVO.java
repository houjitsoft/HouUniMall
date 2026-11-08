package com.houjiang.commons.vo.order;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShoppingCartVO extends PageHelper {

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 手机号码
     */
    private String userMobile;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 商品名
     */
    private String goodsName;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 价格
     */
    private BigDecimal goodsPrice;

    /**
     * 库存
     */
    private Integer goodsStock;

    /**
     * 规格
     */
    private String goodsSpecChineseValue;

    /**
     * 插入购物车时间
     */
    private String insertCartTime;

    /**
     * 用户id
     */
    private String userId;
}
