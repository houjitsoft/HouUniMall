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
 * 分销订单实体
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_distribution_order")
public class DistributionOrder implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 店铺ID
     */
    private Long shopId;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 分销员ID
     */
    private Long distributorId;
    
    /**
     * 购买用户ID
     */
    private Long buyerId;
    
    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 下单商品快照 JSON：[{goodsId,categoryId,goodsName,amount}]
     */
    private String goodsSnapshot;
    
    /**
     * 佣金金额
     */
    private BigDecimal commissionAmount;

    /**
     * 累计已退款金额（等比冲减待结算佣金）
     */
    private BigDecimal refundedAmount;
    
    /**
     * 订单状态
     */
    private Integer orderStatus;
    
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
