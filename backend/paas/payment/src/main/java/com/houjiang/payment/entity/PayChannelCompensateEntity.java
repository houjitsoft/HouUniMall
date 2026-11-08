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
 * 统一补偿查询任务表实体（hou_pay_channel_compensate）
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_channel_compensate")
public class PayChannelCompensateEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务编号（唯一） */
    private String taskNo;

    /** 业务订单号 */
    private String bizNo;

    /** 业务类型：1-支付 2-退款 3-提现 */
    private Integer bizType;

    /** 渠道编码 */
    private String channelCode;

    /** 查询次数 */
    private Integer queryCount;

    /** 最大查询次数 */
    private Integer maxQuery;

    /** 下次执行时间 */
    private Date nextExecuteTime;

    /** 最后一次查询时间 */
    private Date lastQueryTime;

    /** 最后一次查询结果：PENDING/SUCCESS/FAILED/CLOSED */
    private String lastResult;

    /** 任务状态：1-待执行 2-执行中 3-成功 4-失败 5-放弃 */
    private Integer taskStatus;

    /** 最终订单状态 */
    private String finalStatus;

    private String errorMsg;
    private Date createTime;
    private Date updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
