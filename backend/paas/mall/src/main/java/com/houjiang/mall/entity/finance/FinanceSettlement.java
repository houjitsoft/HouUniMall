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
 * 结算记录实体类
 * 表名: hou_mall_finance_settlement
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_finance_settlement")
public class FinanceSettlement implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 结算编号 */
    private String settlementNo;

    /** 店铺ID */
    private Long shopId;

    /** 店铺名称 */
    private String shopName;

    /** 结算周期开始 */
    private java.time.LocalDate periodStart;

    /** 结算周期结束 */
    private java.time.LocalDate periodEnd;

    /** 结算金额 */
    private BigDecimal totalAmount;

    /** 平台佣金 */
    private BigDecimal commissionAmount;

    /** 实际打款 */
    private BigDecimal actualAmount;

    /** 状态: 0-待结算 1-已结算 2-已打款 */
    private Integer status;

    /** 结算时间 */
    private LocalDateTime settleTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除 */
    @TableLogic
    private Integer deleted;
}
