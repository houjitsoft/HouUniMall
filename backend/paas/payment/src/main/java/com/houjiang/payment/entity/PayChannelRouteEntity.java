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
 * 渠道路由规则表实体（hou_pay_channel_route）
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_channel_route")
public class PayChannelRouteEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则编号（唯一） */
    private String routeNo;

    /** 规则名称 */
    private String routeName;

    /** 业务类型：0-全部 1-下单 2-退款 3-提现 */
    private Integer bizType;

    /** 支付方式 */
    private String payMethod;

    /** 终端 */
    private String terminal;

    /** 商户号 */
    private String merchantNo;

    /** 最小金额 */
    private BigDecimal amountMin;

    /** 最大金额 */
    private BigDecimal amountMax;

    /** 候选渠道编码（逗号分隔） */
    private String channelCodes;

    /** 路由策略：PRIORITY/WEIGHT/RANDOM */
    private String strategy;

    /** 规则优先级（越小越优先） */
    private Integer priority;

    /** 状态：0-停用 1-启用 */
    private Integer status;

    private Date effectiveTime;
    private Date expireTime;
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
