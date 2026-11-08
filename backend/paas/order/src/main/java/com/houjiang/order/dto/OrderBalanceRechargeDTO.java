package com.houjiang.order.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName
 * @Description  余额充值订单表
 * @Author Amos
 * @Date 2024-09-19 09:35:39
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderBalanceRechargeDTO extends PageHelper {

    /**
     * 用户id
     */
    private String userId;
    /**
     * 联系方式
     */
    private String userMobile;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 支付状态 1 待支付 2 待审核 3 审核失败 4 已取消 5 已完成
     */
    private Integer rechargeOrderStart;
    /**
     * 充值单号
     */
    private String rechargeOrderId;
    /**
     * 充值方式 1支付宝 2 微信 3 线下支付
     */
    private Integer payCode;
    /**
     * 充值金额
     */
    private BigDecimal rechargeAmount;
    /**
     * 线下支付 审核凭证
     */
    private String rechargeImage;
    /**
     * 审核备注
     */
    private String rechargeMsg;
    /**
     *  1 平台审核通过  2  审核不通过
     */
    private Integer rechargeType;
}
