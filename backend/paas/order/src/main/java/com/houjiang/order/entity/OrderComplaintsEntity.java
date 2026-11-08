package com.houjiang.order.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName
 * @Description  用户投诉表
 * @Author Amos
 * @Date 2023-08-23 09:38:14
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_order_complaints")
@Data
public class OrderComplaintsEntity  implements Serializable {
    /**
     * 投诉单号
     */
    private String complaintsId;

    private Integer id;
    /**
     * 投诉类型 1 未按时发货 2 虚假发货 3 违规出售商品 4 骚扰辱骂  5 售后退款
     */
    private Integer complaintsType;
    /**
     * 原因描叙
     */
    private String complaintsReason;
    /**
     * 投诉凭证
     */
    private String complaintsVoucher;
    /**
     * 联系方式
     */
    private String complaintsContact;
    /**
     * 投诉商品id
     */
    private String complaintsGoodsid;
    /**
     * 投诉状态 0 待卖家处理 1 投诉成立 2 投诉不成立 3 投诉撤销  4 待平台处理
     */
    private Integer complaintsStatus;
    /**
     * 赔付金额
     */
    private Double compensationAmount;
    /**
     * 是否赔付 0 未赔付 1 赔付
     */
    private Integer compensationStatus;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 撤销次数
     */
    private Integer revokeNum;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 买家id
     */
    private String userId;
    /**
     * 处理时效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date failureTime;
    /**
     * 卖家凭证
     */
    private String sellerVoucher;
    /**
     * 卖家说明
     */
    private String sellerReason;
    /**
     * 店铺id
     */
    private Integer shopId;
    private Integer shopType;
    /**
     * 罚款去向 1  账户余额 2平台账户
     */
    private Integer fundDestination;
    /**
     * 是否为平台罚扣 0 否 1 是
     */
    private Integer isPlatform;
    /**
     * 消费者手机号
     */
    private String phoneNumber;
    /**
     * 是否申诉  0 未申诉 1 申诉
     */
    private Integer isAppeal;

    @TableField(exist = false)
    private List complaintsGoodsids;

    @TableField(exist = false)
    private List complaintsVouchers;

    @TableField(exist = false)
    List<GoodsDetailsListVO> goods;

    @TableField(exist = false)
    OrderComplaintsEntity correlationComplaint;

    @TableField(exist = false)
    private List sellerVouchers;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    private String sellUserId;

    private String shopName;
    private String userName;
    private BigDecimal retentionMoneyBalance;
    private BigDecimal retentionMoneyLimit;


}
