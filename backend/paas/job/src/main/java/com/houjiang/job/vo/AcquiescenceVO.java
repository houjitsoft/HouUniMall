package com.houjiang.job.vo;

import lombok.Data;

/**
 * 静止状态
 * @author Amos
 */
@Data
public class AcquiescenceVO {

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 买家id
     */
    private String buyerUserId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 商品id
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
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 评分
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
     * 图片
     */
    private String image;

    /**
     * 是否匿名评价
     */
    private Integer isanonymous;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 状态 0禁止显示 1显示 2置顶',
     */
    private Integer status;

    /**
     * 解释内容
     */
    private String explainContent;

    /**
     * 来源  5 默认好评
     */
    private Integer evaluationFrom;

    /**
     * 收货时间
     */
    private String orderReceiverDate;


}
