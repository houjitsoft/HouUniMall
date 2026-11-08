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
 * 支付渠道配置表实体（hou_pay_channel）
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_channel")
public class PayChannelEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 渠道编码（唯一） */
    private String channelCode;

    /** 渠道名称 */
    private String channelName;

    /** 服务商：WECHAT/ALIPAY/UNIONPAY/CMB/ICBC/STRIPE/PAYPAL */
    private String provider;

    /** 支付方式 */
    private String payMethod;

    /** 基础优先级（越小越优先） */
    private Integer priority;

    /** 权重 */
    private Integer weight;

    /** 日限额 */
    private BigDecimal dailyLimit;

    /** 单笔限额 */
    private BigDecimal singleLimit;

    /** 单笔最小金额 */
    private BigDecimal singleMin;

    /** 渠道费率 */
    private BigDecimal feeRate;

    /** 降级渠道编码（逗号分隔） */
    private String fallbackCodes;

    /** 健康度评分 0-100 */
    private Integer healthScore;

    /** 总调用次数 */
    private Long totalCount;

    /** 成功次数 */
    private Long successCount;

    /** 最后一次失败时间 */
    private Date lastFailedAt;

    /** 熔断状态：0-正常 1-半开 2-熔断 */
    private Integer circuitStatus;

    /** 状态：0-停用 1-启用 */
    private Integer status;

    /** 所属商户号（NULL 全局） */
    private String merchantNo;

    /** 渠道扩展配置 JSON */
    private String configJson;

    private String remark;
    private String creatorId;
    private String updaterId;
    private Date createTime;
    private Date updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
