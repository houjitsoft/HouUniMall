package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 自研对账文件登记（hou_pay_recon_file）
 * 关联表DDL：docs/sql/patches/recon_module.sql
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_recon_file")
public class PayReconFileEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 渠道编码 WX/ALI/UNION 等 */
    private String channelCode;

    /** 账单日期 */
    private Date billDate;

    /** 账单类型 TRADE/REFUND/SETTLE */
    private String billType;

    /** 账单文件 URL */
    private String fileUrl;

    /** 文件 MD5 */
    private String fileMd5;

    /** 总记录数 */
    private Integer totalCount;

    /** 总金额（分） */
    private Long totalAmount;

    /** 状态 0待拉取 1拉取中 2已落地 3比对中 4已比对 9失败 */
    private Integer status;

    /** 失败原因 */
    private String errorMsg;

    /** 拉取时间 */
    private Date pullTime;

    /** 比对完成时间 */
    private Date matchTime;

    private Date createTime;
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
