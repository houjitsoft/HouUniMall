package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 自研分账接收方预配置（hou_pay_division_receiver）
 * 关联表DDL：docs/sql/patches/division_receiver.sql
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_division_receiver")
public class PayDivisionReceiverEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属商户号 */
    private String mchNo;

    /** 接收方业务编号（平台生成） */
    private String receiverNo;

    /** 接收方类型 PERSON/MERCHANT/PLATFORM */
    private String receiverType;

    /** 接收方名称 */
    private String receiverName;

    /** 账户类型 BANK/INNER */
    private String accountType;

    /** 账户号 */
    private String accountNo;

    /** 开户行 */
    private String accountBank;

    /** 证件号（脱敏存储） */
    private String idCard;

    /** 联系手机 */
    private String phone;

    /** 默认分账比例（万分比 0-10000） */
    private Integer shareRatio;

    /** 优先级 */
    private Integer priority;

    /** 最低分账金额（分，计算结果低于此值则以此值为准） */
    private Long minShareAmount;

    /** 最大分账金额（分，计算结果高于此值则封顶） */
    private Long maxShareAmount;

    /** 是否兆底接收方（1=是，所有其他接收方分完后余额归其） */
    private Integer isFallback;

    /** 状态 0已停用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建人 */
    private Long createUserId;

    private Date createTime;
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
