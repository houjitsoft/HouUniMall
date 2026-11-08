package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 支付安全（支付密码/实名认证）（hou_pay_security）
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_security")
public class PaySecurityEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private String userId;

    /** 支付密码(BCrypt) */
    private String payPassword;

    /** 实名姓名密文 */
    private String realNameEncrypted;

    /** 身份证号密文 */
    private String idCardEncrypted;

    /** 实名认证状态：0-未认证；1-待审核；2-已认证；3-已拒绝 */
    private Integer realNameStatus;

    /** 实名认证通过时间 */
    private Date realNameTime;

    /** 密码错误次数 */
    private Integer pwdErrorCount;

    /** 密码锁定时间 */
    private Date pwdLockTime;

    /** 密码最后修改时间 */
    private Date lastModifyTime;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
