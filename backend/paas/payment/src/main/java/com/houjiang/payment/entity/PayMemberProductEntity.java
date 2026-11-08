package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 自研会员产品开通记录（hou_pay_member_product）
 * 关联表DDL：docs/sql/patches/member_business.sql
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_member_product")
public class PayMemberProductEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会员ID */
    private Long memberId;

    /** 所属商户号（可空） */
    private String mchNo;

    /** 产品编码 PAY_BASIC/WITHDRAW/DIVISION/QUICK_PAY */
    private String productCode;

    /** 产品名称 */
    private String productName;

    /** 状态 0已关闭 1已开通 2待审核 3已驳回 */
    private Integer status;

    /** 费率（万分比） */
    private Integer feeRate;

    /** 日限额（分） */
    private Long dailyLimit;

    /** 单笔限额（分） */
    private Long singleLimit;

    /** 开通时间 */
    private Date openTime;

    /** 关闭时间 */
    private Date closeTime;

    /** 备注 */
    private String remark;

    private Date createTime;
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
