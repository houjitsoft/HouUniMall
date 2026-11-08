package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据大屏统计-订单商品表
 * @TableName ${database.prefix.datav}hou_datav_order_goods
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_order_goods")
@Data
public class DcenterOrderGoodsEntity implements Serializable {
    /**
     * 订单id
     */
    @TableId
    private String orderId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品common_id
     */
    private Integer commonId;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 商品对应的类目ID
     */
    private Integer goodsClassId;

    /**
     * 商品数量
     */
    private Integer orderGoodsNum;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 退货状态:0是无退货,1是退货中,2是退货完成 3商家拒绝退货
     */
    private Integer goodsReturnStatus;

    /**
     * 退款状态：0：无退款 1：退款中 2：退款完成  3：商家拒绝退款
     */
    private Integer goodsRefundStatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}