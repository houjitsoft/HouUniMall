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
 * 渠道调用日志表实体（hou_pay_channel_log）
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_channel_log")
public class PayChannelLogEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 日志编号（唯一） */
    private String logNo;

    /** 业务订单号 */
    private String bizNo;

    /** 命中的路由规则编号 */
    private String routeNo;

    /** 最终选中的渠道编码 */
    private String channelCode;

    /** 支付方式 */
    private String payMethod;

    /** 终端 */
    private String terminal;

    /** 商户号 */
    private String merchantNo;

    /** 金额 */
    private BigDecimal amount;

    /** 选中策略：PRIORITY/WEIGHT/RANDOM/FALLBACK */
    private String selectStrategy;

    /** 降级链路（c1-&gt;c2-&gt;c3） */
    private String fallbackChain;

    /** 调用结果：0-未知 1-成功 2-失败 */
    private Integer result;

    /** 调用耗时（ms） */
    private Integer costMs;

    private String errorCode;
    private String errorMsg;
    private String extraContext;
    private Date createTime;
    private Date updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
