package com.houjiang.order.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName
 * @Description  购物车表
 * @Author Amos
 * @Date 2023-05-25 13:39:40
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_order_user_cart")
@Data
public class OrderUserCartEntity  implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer cartId;
    /**
     * 买家id
     */
    private String userId;
    /**
     * 店铺id
     */
    private Integer shopId;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 商品Id
     */
    private Integer commonId;
    /**
     * 规格id
     */
    private Integer goodsId;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品图片
     */
    private String goodsImage;
    /**
     * 数量
     */
    private Integer goodsNum;
    /**
     * 商品价格
     */
    private Double goodsPrice;
    /**
     * 库存
     */
    private Integer goodsStock;
    /**
     * 1普通商品  2供应商商品
     */
    private Integer goodsType;
    /**
     * 规格(废弃)
     */
    private String goodsSpec;
    /**
     * 商品起订量
     */
    private String goodsMoq;
    /**
     * 插入购物车时间
     */
    private Date insertCartTime;
    /**
     * 0 正常 1 删除 2 店铺关闭商品下架
     */
    private Integer cartStatus;
    /**
     * 门店id
     */
    private Integer shopGateId;
     /**
      * 是否plus会员 0过期 1是 2否
      */
     @TableField(exist = false)
    private Integer plusMember;
     /**
      * 会员折扣-plus折扣
      */
     @TableField(exist = false)
     private BigDecimal plusDiscount;

     /**
      * 是否经销商品  0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品（商家通过立即购买供应商上架的商品）
      */
     @TableField(exist = false)
     private Integer dealerGoodsFlag;

     /**
      * 是否上架1:上架
      */
     @TableField(exist = false)
     private Integer goodsIsShelves;

     /**
      * 最大购买数量
      */
     @TableField(exist = false)
     private Integer goodsMaxSale;

     /**
      *
      */
     @TableField(exist = false)
     private Integer commonIsXian;

    /**
     * 最小限购数据
     */
     @TableField(exist = false)
     private Integer goodsMinSale;

     /**
      * 活动状态
      */
     @TableField(exist = false)
     private Integer activityGoodsState;

     /**
      * 已购买数量
      */
     @TableField(exist = false)
     private Integer purchased;

     /**
      * 是否plus商品
      */
     @TableField(exist = false)
     private Integer plusGoods;

     /**
      * 商品总价
      */
     @TableField(exist = false)
     private BigDecimal goodsTotalPrice;

    /**
     *规格集合
     */
    @TableField(exist = false)
    private List<?> specification;

    /**
     * 规格中文值
     */
    private String goodsSpecChineseValue;

    /**
     *
     */
    @TableField(exist = false)
    private Integer toBuyMaxNum;
    /**
     * 活动开始时间
     */
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date goodsStartTime;

    /**
     * 活动详情
     */
    @TableField(exist = false)
    private ActivityGoodsDetailDTO activityGoodsDetail;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;



    /**
     * 积分支付比例
     */
    @TableField(exist = false)
    private Double pointsRate;
    /**
     * 现金价格
     */
    @TableField(exist = false)
    private BigDecimal cashPrice;
}

