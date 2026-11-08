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
 * 返利调整实体类
 * 表名: hou_mall_rebate_adjustment
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_rebate_adjustment")
public class RebateAdjustment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 调整单号
     */
    private String adjustNo;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联返利记录ID
     */
    private Long rebateRecordId;

    /**
     * 调整类型 1-增加 2-减少
     */
    private Integer adjustType;

    /**
     * 调整金额
     */
    private BigDecimal adjustAmount;

    /**
     * 调整前金额
     */
    private BigDecimal beforeAmount;

    /**
     * 调整后金额
     */
    private BigDecimal afterAmount;

    /**
     * 调整原因
     */
    private String reason;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 审核状态 0-待审核 1-已通过 2-已拒绝
     */
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核人姓名
     */
    private String auditorName;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    private String auditRemark;

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
