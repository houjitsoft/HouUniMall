package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 自研对账月度报表（hou_pay_recon_report）
 * 关联表DDL：docs/sql/patches/recon_module.sql
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_recon_report")
public class PayReconReportEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商户号（NULL表示全平台） */
    private String mchNo;

    /** 渠道编码（NULL表示全渠道） */
    private String channelCode;

    /** 账单月份 yyyy-MM */
    private String billMonth;

    /** 总笔数 */
    private Integer totalCount;

    /** 总金额（分） */
    private Long totalAmount;

    /** 成功笔数 */
    private Integer successCount;

    /** 成功金额（分） */
    private Long successAmount;

    /** 差异笔数 */
    private Integer diffCount;

    /** 差异金额（分） */
    private Long diffAmount;

    /** 报表导出URL */
    private String reportUrl;

    /** 生成时间 */
    private Date genTime;

    private Date createTime;
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
