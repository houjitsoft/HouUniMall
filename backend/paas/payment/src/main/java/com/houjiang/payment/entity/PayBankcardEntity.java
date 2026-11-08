package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户银行卡（hou_pay_bankcard）
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_bankcard")
public class PayBankcardEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private String userId;

    /** 银行编码 */
    private String bankCode;

    /** 银行名称 */
    private String bankName;

    /** 卡号密文（AES） */
    private String cardNoEncrypted;

    /** 卡号后四位 */
    private String cardNoTail;

    /** 卡类型：1-储蓄卡；2-信用卡 */
    private Integer cardType;

    /** 持卡人姓名 */
    private String holderName;

    /** 持卡人身份证密文 */
    private String holderIdEncrypted;

    /** 银行预留手机号 */
    private String holderMobile;

    /** 是否默认：0-否；1-是 */
    private Integer isDefault;

    /** 状态：0-已删除；1-正常；2-已冻结 */
    private Integer status;

    /** 验证状态：0-未验证；1-已验证 */
    private Integer verifyStatus;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
