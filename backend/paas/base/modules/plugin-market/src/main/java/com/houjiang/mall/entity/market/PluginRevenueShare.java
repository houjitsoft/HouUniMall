package com.houjiang.mall.entity.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 插件销售分账流水（hou_plugin_revenue_share）。
 *
 * <p><b>一笔订单一条</b>：订单支付成功时按当时的平台抽成比例固化金额，
 * 之后改配置不影响历史流水——否则「同一个订单，月初看和月末看结算金额不一样」，
 * 对账永远说不清。唯一键 {@code uk_share_order} 保证结算重试不会重复记账。
 *
 * @author Amos
 * @since 2026-10-27
 */
@Data
@TableName("hou_plugin_revenue_share")
public class PluginRevenueShare implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 0=待结算 1=已结算 2=已驳回（退款/风控） */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SETTLED = 1;
    public static final int STATUS_REJECTED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private String pluginCode;

    private Long developerId;

    private Long buyerId;

    /** 订单实收金额 */
    private BigDecimal grossAmount;

    private String currency;

    /** 平台抽成比例（0.3 = 30%），下单结算时快照 */
    private BigDecimal commissionRate;

    private BigDecimal platformAmount;

    private BigDecimal developerAmount;

    /** 0=待结算 1=已结算 2=已驳回 */
    private Integer status;

    private String batchNo;

    private LocalDateTime settleTime;

    private String remark;

    private LocalDateTime createTime;
}
