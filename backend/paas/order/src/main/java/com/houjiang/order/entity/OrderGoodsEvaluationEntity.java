package com.houjiang.order.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  商品评论表
 * @Author Amos
 * @Date 2023-05-26 13:28:35
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_order_goods_evaluation")
@Data
public class OrderGoodsEvaluationEntity  implements Serializable {
    /**
     * 商品评论id
     */
    @TableId
    private Integer evaluationGoodsId;
    /**
     * 会员ID
     */
    private String userId;
    /**
     * 会员名
     */
    private String userName;
    /**
     * 会员头像
     */
    private String userAvatar;
    /**
     * 订单ID
     */
    private String orderId;
    /**
     * 商家ID
     */
    private Integer shopId;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 商品ID
     */
    private Integer commonId;
    /**
     * 规格Id
     */
    private Integer goodsId;
    /**
     * 商品名
     */
    private String goodsName;
    /**
     * 商品规格
     */
    private String goodsSpec;
    /**
     * 1-5分
     */
    private Integer scores;
    /**
     * 结果
     */
    private String result;
    /**
     * 内容
     */
    private String content;

    /**
     * 追评回复时间
     */
    private Date additionalContentTime;
    /**
     * 晒单图片
     */
    private String image;
    /**
     * 是否匿名评价 0 不匿名 1  匿名
     */
    private Integer isanonymous;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 状态 0禁止显示 1显示 2置顶
     */
    private Integer status;
    /**
     * 解释内容
     */
    private String explainContent;
    /**
     * 评价时间
     */
    private Date updateTime;
    /**
     * 商家回复追评时间
     */
    private Date additionalReplyTime;
    /**
     *  1 ios  2 pc 3 wap 4 android 5默认好评
     */
    private Integer evaluationFrom;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
