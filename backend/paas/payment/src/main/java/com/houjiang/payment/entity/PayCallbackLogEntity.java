package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 支付渠道回调流水（hou_pay_callback_log）
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_callback_log")
public class PayCallbackLogEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 收银台订单号 */
    private String cashierNo;

    /** 支付平台订单号 */
    private String payOrderId;

    /** 商户订单号 */
    private String mchOrderNo;

    /** 渠道 */
    private String channel;

    /** 事件类型：PAY/REFUND/CLOSE */
    private String eventType;

    /** 请求头原文 */
    private String reqHeaders;

    /** 请求报文原文 */
    private String reqBody;

    /** 验签结果：0-未验；1-通过；2-不通过 */
    private Integer signVerify;

    /** 处理状态：0-待处理；1-成功；2-失败；3-重复 */
    private Integer processStatus;

    /** 响应内容 */
    private String responseBody;

    /** 错误信息 */
    private String errMsg;

    /** 接收时间 */
    private Date receiveTime;

    /** 处理完成时间 */
    private Date processTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
