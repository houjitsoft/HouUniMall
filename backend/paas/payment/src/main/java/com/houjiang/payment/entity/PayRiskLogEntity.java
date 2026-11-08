package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付风控拦截日志表实体（hou_pay_risk_log）
 * 每次风控引擎调用都会落一条流水（无论放行/拦截/告警/审核）
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_risk_log")
public class PayRiskLogEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 日志单号（唯一） */
    private String logNo;

    /** 业务类型：1-收银台下单 2-退款 3-提现 4-转账 5-充值 */
    private Integer bizType;

    /** 业务单号 */
    private String bizNo;

    /** 商户号 */
    private String mchNo;

    /** 用户ID */
    private String userId;

    /** 用户IP */
    private String userIp;

    /** 金额（元） */
    private BigDecimal amount;

    /** 支付方式 */
    private String payMethod;

    /** 命中规则编号（逗号分隔） */
    private String hitRuleNos;

    /** 命中规则明细 JSON */
    private String hitRulesJson;

    /** 风险等级：0-正常 1-低 2-中 3-高 */
    private Integer riskLevel;

    /** 最终动作：1-放行 2-拦截 3-告警 4-待审核 */
    private Integer action;

    /** 拦截/告警/审核原因 */
    private String rejectReason;

    /** 审核状态：0-未审核 1-审核通过 2-审核驳回 */
    private Integer auditStatus;

    /** 审核结果说明 */
    private String auditResult;

    /** 审核人ID */
    private String auditOperator;

    /** 审核时间 */
    private Date auditTime;

    /** 人工审核状态：0-待审 1-通过 2-驳回（W4 新增 review 流） */
    private Integer reviewStatus;

    /** 人工审核人ID（W4 新增） */
    private Long reviewUserId;

    /** 人工审核时间（W4 新增） */
    private Date reviewTime;

    /** 人工审核备注（W4 新增） */
    private String reviewRemark;

    /** 扩展上下文 JSON */
    private String extraContext;

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
