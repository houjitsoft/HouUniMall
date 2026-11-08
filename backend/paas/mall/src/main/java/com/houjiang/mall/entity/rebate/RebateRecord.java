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
 * 返利记录实体类
 * 表名: hou_mall_rebate_record
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_rebate_record")
public class RebateRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 返利单号
     */
    private String rebateNo;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 返利规则ID
     */
    private Long ruleId;

    /**
     * 返利类型 1-会员返利 2-消费返利 3-推广返利 4-积分返利
     */
    private Integer rebateType;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 返利金额
     */
    private BigDecimal rebateAmount;

    /**
     * 返利积分
     */
    private Integer rebatePoints;

    /**
     * 状态 0-待结算 1-已结算 2-已取消 3-已失效
     */
    private Integer status;

    /**
     * 结算时间
     */
    private LocalDateTime settleTime;

    /**
     * 失效原因
     */
    private String invalidReason;

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
