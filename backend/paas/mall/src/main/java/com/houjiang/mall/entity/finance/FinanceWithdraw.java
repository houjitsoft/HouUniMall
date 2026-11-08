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
 * 提现申请实体类
 * 表名: hou_mall_finance_withdraw
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_finance_withdraw")
public class FinanceWithdraw implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提现单号 */
    private String withdrawNo;

    /** 店铺ID */
    private Long shopId;

    /** 店铺名称 */
    private String shopName;

    /** 会员ID */
    private Long memberId;

    /** 会员名称 */
    private String memberName;

    /** 提现金额 */
    private BigDecimal withdrawAmount;

    /** 手续费 */
    private BigDecimal fee;

    /** 实付金额 */
    private BigDecimal actualAmount;

    /** 银行账户 */
    private String bankAccount;

    /** 开户行 */
    private String bankName;

    /** 账户名 */
    private String accountName;

    /** 关联结算单号 */
    private String settlementNo;

    /** 状态: 0-待审核 1-已通过 2-已拒绝 3-已打款 */
    private Integer status;

    /** 审核人 */
    private String auditor;

    /** 审核备注 */
    private String auditRemark;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除 */
    @TableLogic
    private Integer deleted;
}
