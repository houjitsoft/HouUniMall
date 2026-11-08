package com.houjiang.mall.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 质保金实体类
 * 表名: hou_mall_quality_deposit
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_quality_deposit")
public class QualityDeposit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 质保金单号
     */
    private String depositNo;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 商家名称
     */
    private String merchantName;

    /**
     * 应缴金额
     */
    private BigDecimal requiredAmount;

    /**
     * 已缴金额
     */
    private BigDecimal paidAmount;

    /**
     * 待缴金额
     */
    private BigDecimal unpaidAmount;

    /**
     * 缴纳方式: 1-银行转账 2-支付宝 3-微信 4-其他
     */
    private Integer paymentMethod;

    /**
     * 缴纳凭证
     */
    private String paymentVoucher;

    /**
     * 缴纳时间
     */
    private LocalDateTime paymentTime;

    /**
     * 状态: 0-未缴纳 1-已缴纳 2-部分缴纳 3-已退还
     */
    private Integer status;

    /**
     * 退还金额
     */
    private BigDecimal refundAmount;

    /**
     * 退还原因
     */
    private String refundReason;

    /**
     * 退还时间
     */
    private LocalDateTime refundTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer deleted;
}
