package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 自研对账差异单（hou_pay_recon_diff）
 * 关联表DDL：docs/sql/patches/recon_module.sql
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_recon_diff")
public class PayReconDiffEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 对账文件 ID */
    private Long reconFileId;

    /** 渠道编码 */
    private String channelCode;

    /** 账单日期 */
    private Date billDate;

    /** 平台订单号 */
    private String localOrderNo;

    /** 渠道订单号 */
    private String channelOrderNo;

    /** 平台金额（分） */
    private Long localAmount;

    /** 渠道金额（分） */
    private Long channelAmount;

    /** 差异类型 LOCAL_ONLY/CHANNEL_ONLY/AMOUNT_DIFF/STATUS_DIFF */
    private String diffType;

    /** 差异金额（分） */
    private Long diffAmount;

    /** 处理状态 0待处理 1已处理 2已忽略 */
    private Integer handleStatus;

    /** 处理人 */
    private Long handleUserId;

    /** 处理时间 */
    private Date handleTime;

    /** 处理备注 */
    private String handleRemark;

    private Date createTime;
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
