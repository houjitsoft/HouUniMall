package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.commons.constant.CommonType;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 分销佣金提现第三方交易记录表
 * @TableName ${database.prefix.payment}hou_pay_fx_trade
 * @author Amos
 */
@TableName(value ="hou_pay_fx_trade")
@Data
public class PayFxTradeEntity implements Serializable {
    /**
     * 第三方交易请求单号
     */
    @TableId
    private String thirdPayId;

    /**
     * 交易单号
     */
    private String recordId;

    /**
     * 第三方交易单号
     */
    private String thirdTradeNo;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 交易总金额
     */
    private BigDecimal totalMoney;

    /**
     * 交易费率
     */
    private BigDecimal tradeRate;

    /**
     * 交易描述
     */
    private String tradeDesc;

    /**
     * 交易类型
     */
    private Integer tradeTypeId;

    /**
     * 是否支付 0:未支付 1:已支付 2:提现失败 3已取消
     */
    private Integer tradeStatus;

    /**
     * 支付宝:ALI  微信:WX
     */
    private String payType;

    /**
     * 第三方失败错误码
     */
    private String errorCode;

    /**
     * 回调失败时返回的信息
     */
    private String errorMsg;

    /**
     * 提现失败补偿次数
     */
    private Integer queryCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 回调时间
     */
    private Date notifyTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public PayFxTradeEntity() {
    }
    public PayFxTradeEntity(Integer tradeStatus, Integer payCode) {
        this.tradeStatus = tradeStatus;
        if(payCode.equals(CommonType.payType.ALI.getCode())){
            this.payType = CommonType.payType.ALI.getDesc();
        }else {
            this.payType =  CommonType.payType.WX.getDesc();
        }
    }
}
