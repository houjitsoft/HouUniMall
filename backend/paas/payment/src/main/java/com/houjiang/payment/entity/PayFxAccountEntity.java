package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 用户分销佣金账户表
 * @TableName ${database.prefix.payment}hou_pay_fx_account
 * @author Amos
 */
@TableName(value ="hou_pay_fx_account")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayFxAccountEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userMobile;

    /**
     * 总金额
     */
    private BigDecimal totalAccount;

    /**
     * 微客佣金可提现
     */
    private BigDecimal vkAccount;

    /**
     * 提现冻结金额
     */
    private BigDecimal withdrawFreezeAccount;
    /**
     * 小程序openId
     */
    private String miniWxOpenId;

    /**
     * 小程序微信名
     */
    private String miniWxUserName;

    /**
     * 小程序用户标识
     */
    private String miniWxUnionId;

    /**
     * 用户微信openid
     */
    private String wxOpenId;

    /**
     * 微信授权用户名
     */
    private String wxUserName;

    /**
     * 微信用户标识
     */
    private String wxUnionId;

    /**
     * 创建时间
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

    public PayFxAccountEntity(String userId, BigDecimal freezeAccount, BigDecimal vkAccount) {
        this.updateTime = DateUtils.getNowDate();
        this.userId = userId;
        this.withdrawFreezeAccount = freezeAccount;
        this.vkAccount = vkAccount;
    }
}