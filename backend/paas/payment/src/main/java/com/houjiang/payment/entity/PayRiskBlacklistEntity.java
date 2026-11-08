package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 支付风控黑白名单表实体（hou_pay_risk_blacklist）
 * 支持用户/商户/IP/银行卡/手机号/身份证号 6 种目标
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_risk_blacklist")
public class PayRiskBlacklistEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 名单类型：1-黑名单 2-白名单 */
    private Integer listType;

    /** 目标类型：1-用户ID 2-商户号 3-IP 4-银行卡号 5-手机号 6-身份证号 */
    private Integer targetType;

    /** 目标取值 */
    private String targetValue;

    /** 业务作用域：0-全部 1-收银台下单 2-退款 3-提现 4-转账 */
    private Integer bizScope;

    /** 加入原因 */
    private String reason;

    /** 风险等级：1-低 2-中 3-高 */
    private Integer riskLevel;

    /** 生效时间 */
    private Date effectiveTime;

    /** 失效时间（NULL 永久） */
    private Date expireTime;

    /** 状态：0-失效 1-生效 */
    private Integer status;

    /** 操作人ID */
    private String operatorId;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
