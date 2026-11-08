package com.houjiang.mall.entity.distribution;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 佣金记录实体类
 * 表名: hou_mall_commission_record
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_commission_record")
public class CommissionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 记录编号
     */
    private String recordNo;

    /**
     * 分销员ID
     */
    private Long distributorId;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 佣金金额（可被退款冲减后的当前值）
     */
    private BigDecimal commissionAmount;

    /**
     * 生成时原始佣金（退款等比冲减的基准；生成时写入）
     */
    private BigDecimal originalCommissionAmount;

    /**
     * 佣金比例(%)
     */
    private BigDecimal commissionRate;

    /**
     * 佣金层级 1-一级 2-二级
     */
    private Integer level;

    /**
     * 来源分销员ID(被推广的分销员)
     */
    private Long sourceDistributorId;

    /**
     * 状态 0-待结算 1-已结算 2-已取消
     */
    private Integer status;

    /**
     * 结算时间
     */
    private LocalDateTime settleTime;

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
