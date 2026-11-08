package com.houjiang.mall.entity.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支出记录实体类
 * 表名: hou_mall_finance_expense
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_finance_expense")
public class FinanceExpense implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支出编号 */
    private String expenseNo;

    /** 支出类型: refund/commission/other */
    private String type;

    /** 金额 */
    private BigDecimal amount;

    /** 关联订单号 */
    private String relatedOrderNo;

    /** 备注 */
    private String remark;

    /** 状态: 0-待确认 1-已确认 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除 */
    @TableLogic
    private Integer deleted;
}
