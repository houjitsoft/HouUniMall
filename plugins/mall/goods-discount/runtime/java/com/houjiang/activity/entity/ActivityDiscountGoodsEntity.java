package com.houjiang.activity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  限时折扣商品表
 * @Author Amos
 * @Date 2023-05-25 14:10:09
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_addon_activity_discount_goods")
@Data
public class ActivityDiscountGoodsEntity  implements Serializable {
    /**
     * 限时折扣商品表
     */
    @TableId(type = IdType.AUTO)
    private Integer discountGoodsId;
    /**
     * 限时活动编号
     */
    private Integer discountId;
    /**
     * 活动名称
     */
    private String discountName;
    /**
     * 商品编号
     */
    private Integer goodsId;
     /**
      * spuId
      */
    private Integer commonId;
    /**
     * 店铺编号
     */
    private Integer shopId;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品原价
     */
    private Double goodsPrice;
    /**
     * 限时折扣价格
     */
    private Double discountPrice;
    /**
     * 商品图片
     */
    private String goodsImage;
    /**
     * 开始时间
     */
    private Date goodsStartTime;
    /**
     * 结束时间
     */
    private Date goodsEndTime;
    /**
     * 购买下限，0为不限制
     */
    private Integer goodsLowerLimit;
    /**
     * 状态， 1-已开始；2-已结束；3-已失效；0-未开始；4-预热中
     */
    private Integer discountGoodsState;
    /**
     * 购买上限
     */
    private Integer goodsUpperLimit;
    /**
     * 每种商品限购 限购天数
     */
    private Integer discountUpperTime;
    /**
     * 折扣
     */
    private Double discountAmount;
    /**
     * 预热时间
     */
    private Integer preStartHour;
    /**
     * 预热开始时间
     */
    private Date preStartTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

