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
 * 商户费率配置（hou_pay_merchant_fee）
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_merchant_fee")
public class PayMerchantFeeEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商户号 */
    private String mchNo;

    /** 产品编码 */
    private String productCode;

    /** 支付方式编码（可为空，代表产品默认） */
    private String payMethod;

    /** 费率类型：1-按比例 2-按笔固定 3-混合 */
    private Integer feeType;

    /** 费率 */
    private BigDecimal feeRate;

    /** 固定手续费（元） */
    private BigDecimal feeFixed;

    /** 手续费下限 */
    private BigDecimal feeMin;

    /** 手续费上限 */
    private BigDecimal feeMax;

    /** 生效时间 */
    private Date effectiveTime;

    /** 失效时间 */
    private Date expireTime;

    /** 状态：0-已停用 1-生效中 */
    private Integer status;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
