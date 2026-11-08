package com.houjiang.mall.entity.rebate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 返利发放实体类
 * 表名: hou_mall_rebate_payment
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_rebate_payment")
public class RebatePayment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发放单号
     */
    private String paymentNo;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 发放金额
     */
    private BigDecimal paymentAmount;

    /**
     * 发放方式 1-余额 2-银行卡 3-微信 4-支付宝
     */
    private Integer paymentMethod;

    /**
     * 收款账号
     */
    private String paymentAccount;

    /**
     * 收款人姓名
     */
    private String payeeName;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 关联返利记录ID（JSON数组）
     */
    private String rebateRecordIds;

    /**
     * 状态 0-待发放 1-发放中 2-已发放 3-发放失败
     */
    private Integer status;

    /**
     * 发放时间
     */
    private LocalDateTime paymentTime;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

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
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
