package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * mall_fkg系统充值卡表
 * @TableName ${database.prefix.payment}hou_pay_card
 * @author Amos
 */
@TableName(value ="hou_pay_card")
@Data
public class PayCardEntity implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 卡号
     */
    private String cardNum;

    /**
     * 面额
     */
    private Integer totalPrice;

    /**
     * 赠送(废弃)
     */
    private Double givePrice;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态0未使用，1已使用
     */
    private Integer statut;

    /**
     * 使用者
     */
    private String useName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date creatTime;

    /**
     * 有效开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date stime;

    /**
     * 到期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date etime;

    /**
     * 卡片描述
     */
    private String cardDesc;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}