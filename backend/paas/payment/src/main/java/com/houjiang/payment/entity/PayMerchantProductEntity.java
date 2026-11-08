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
 * 商户产品开通记录（hou_pay_merchant_product）
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_merchant_product")
public class PayMerchantProductEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商户号 */
    private String mchNo;

    /** 产品编码 */
    private String productCode;

    /** 产品名称 */
    private String productName;

    /** 产品状态：0-申请中 1-已开通 2-已停用 3-审核失败 */
    private Integer status;

    /** 单笔限额（元） */
    private BigDecimal singleLimit;

    /** 单日累计限额（元） */
    private BigDecimal dailyLimit;

    /** 单月累计限额（元） */
    private BigDecimal monthlyLimit;

    /** 开通时间 */
    private Date openTime;

    /** 关闭时间 */
    private Date closeTime;

    /** 备注 */
    private String remark;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
