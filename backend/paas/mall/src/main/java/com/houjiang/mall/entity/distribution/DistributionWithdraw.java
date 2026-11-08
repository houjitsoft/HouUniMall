package com.houjiang.mall.entity.distribution;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分销佣金提现单实体类
 * 表名: hou_mall_distribution_withdraw
 *
 * 状态流转：0 待审核 → 1 审核通过待打款 → 3 已打款；或 0 → 2 已驳回（余额退回）。
 * 金额口径：amount 申请金额，fee 手续费，actualAmount = amount - fee。
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_distribution_withdraw")
public class DistributionWithdraw implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提现单号 */
    private String withdrawNo;

    /** 店铺ID（0=平台） */
    private Long shopId;

    /** 分销员ID */
    private Long distributorId;

    /** 关联用户ID */
    private Long userId;

    /** 提现金额 */
    private BigDecimal amount;

    /** 手续费 */
    private BigDecimal fee;

    /** 实际到账金额 */
    private BigDecimal actualAmount;

    /** 到账方式：1=银行卡 2=微信 3=支付宝 4=余额 */
    private Integer payType;

    /** 收款人姓名 */
    private String accountName;

    /** 收款账号 */
    private String accountNo;

    /** 开户行 */
    private String bankName;

    /** 状态：0=待审核 1=审核通过待打款 2=已驳回 3=已打款 */
    private Integer status;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 审核人 */
    private String auditor;

    /** 审核备注 */
    private String auditRemark;

    /** 申请人备注 */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
