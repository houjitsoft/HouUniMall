package com.houjiang.order.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName
 * @Description 佣金订单
 * @Author Amos
 * @Date 2023-06-01 11:38:05
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_order_commission")
@Data
public class OrderCommissionEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer orderCommissionId;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品名
     */
    private String goodsName;
    /**
     * 商品价格
     */
    private Double goodsPrice;
    /**
     * 购买数量
     */
    private Integer goodsNum;
    /**
     * 商品图片
     */
    private String goodsImage;
    /**
     * 分类id
     */
    private Integer catId;
    /**
     * 商品所属店铺id
     */
    private Integer shopId;
    /**
     * 分享用户id
     */
    private Integer shareUserId;
    /**
     * 分享用户名
     */
    private String shareUserName;
    /**
     * 分享用户手机号
     */
    private String shareUserMobile;
    /**
     * 申请分享人id
     */
    private String shareTwoUserId;
    /**
     * 申请分享人名称
     */
    private String shareTwoUserName;
    /**
     * 申请分享人电话
     */
    private String shareTwoUserMobile;
    /**
     * 购买用户id
     */
    private Integer buyUserId;
    /**
     * 购买用户名
     */
    private String buyUserName;
    /**
     * 购买用户手机号
     */
    private String buyUserMobile;
    /**
     * 购买用户头像
     */
    private String buyUserAvatar;
    /**
     * 购买用户最后登录时间
     */
    private Date buyUserTime;
    /**
     * 状态 1待支付  2未入账  3已入账  4已退款 5已取消
     */
    private Integer orderStatus;
    /**
     * 订单金额
     */
    private Double orderPrice;
    /**
     * 退款金额
     */
    private Double returnPrice;
    /**
     * 一级推广佣金比例（数字格式 运算需要自己/100）
     */
    private String oneTgRate;
    /**
     * 二级推广佣金比例（数字格式 运算需要自己/100）
     */
    private String twoTgRate;
    /**
     * 分享佣金比例
     */
    private String fxRate;
    /**
     * 一级推广佣金
     */
    private BigDecimal oneTgCommission;
    /**
     * 二级推广佣金
     */
    private BigDecimal twoTgCommission;
    /**
     * 分享佣金
     */
    private Double fxCommission;
    /**
     * 一级退还推广佣金
     */
    private BigDecimal returnOneTgCommission;
    /**
     * 二级退还推广佣金
     */
    private BigDecimal returnTwoTgCommission;
    /**
     * 退还分享佣金
     */
    private Double returnFxCommission;
    /**
     * 下单时间
     */
    private Date createTime;
    /**
     * 支付时间
     */
    private Date payTime;
    /**
     * 退款时间
     */
    private Date returnTime;
    /**
     * 1会员微客佣金   2员工微客佣金
     */
    private Integer commissionType;
    /**
     * 购买用户是否分享用户下级  1是  2否
     */
    private Integer bindType;
    /**
     * 结算交易id
     */
    private String settleRecordId;
    /**
     * 0未结算  1已结算
     */
    private Integer settleState;
    /**
     * 结算时间
     */
    private Date settleTime;
    /**
     * 购物者是否已成为订单所属店铺的微客：0，否；1，是
     */
    private Integer isShopVk;
    /**
     * 分享 1 推广 2
     */
    private Integer fxTg;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
