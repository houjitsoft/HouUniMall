package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户分销佣金交易记录表
 * @TableName ${database.prefix.payment}hou_pay_fx_record
 * @author Amos
 */
@TableName(value ="hou_pay_fx_record")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayFxRecordEntity implements Serializable {
    /**
     * 交易单号
     */
    @TableId
    private String recordId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 交易金额
     */
    private BigDecimal recordAmount;

    /**
     * 交易之前用户总余额
     */
    private BigDecimal beforeRecordAmount;

    /**
     * 交易之后用户总余额
     */
    private BigDecimal afterRecordAmount;

    /**
     * 交易状态 1:待处理 2交易完成 3交易取消4提现失败
     */
    private Integer recordStatus;

    /**
     * 交易类型1获得佣金 2提现
     */
    private Integer tradeTypeId;

    /**
     * 1微客佣金 2员工佣金
     */
    private Integer recordType;

    /**
     * 金额正负判断(1:+0:-)
     */
    private Integer plusOrMinus;

    /**
     * 支付方式1支付宝 2微信
     */
    private Integer payCode;

    /**
     * 支付来源手机端1PC 2wap微信浏览器 3安卓 5ios 6微信小程序 7wap浏览器
     */
    private Integer payFrom;

    /**
     * 提现到微信用户openid
     */
    private String wxOpenId;

    /**
     * 提现到微信用户昵称
     */
    private String wxUserName;

    /**
     * 描述
     */
    private String recordName;

    /**
     * 状态描述
     */
    private String recordStatusName;

    /**
     * 日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date recordDate;

    /**
     * 交易时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


    public PayFxRecordEntity(String recordId, BigDecimal beforeRecordAmount, BigDecimal afterRecordAmount) {
        this.recordId = recordId;
        this.beforeRecordAmount = beforeRecordAmount;
        this.afterRecordAmount = afterRecordAmount;
        this.updateTime = DateUtils.getNowDate();
    }

    public PayFxRecordEntity(String recordId, Integer recordStatus, String recordStatusName) {
        this.recordId = recordId;
        this.recordStatus = recordStatus;
        this.recordStatusName = recordStatusName;
        this.updateTime = DateUtils.getNowDate();
    }

    public PayFxRecordEntity(String recordId, BigDecimal beforeRecordAmount, BigDecimal afterRecordAmount, Integer recordStatus,String recordStatusName,Date updateTime) {
        this.recordId = recordId;
        this.beforeRecordAmount = beforeRecordAmount;
        this.afterRecordAmount = afterRecordAmount;
        this.recordStatus = recordStatus;
        this.recordStatusName = recordStatusName;
        this.updateTime = updateTime;
    }

}