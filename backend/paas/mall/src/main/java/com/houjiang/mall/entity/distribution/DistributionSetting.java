package com.houjiang.mall.entity.distribution;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分销设置实体类
 * 表名: hou_mall_distribution_setting
 *
 * 平台/店铺级分销功能配置：总开关、分销层级数、申请方式、绑定保护期、结算周期、提现门槛。
 * shopId = 0 表示平台默认配置，各店铺可覆盖。
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_distribution_setting")
public class DistributionSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 店铺ID（0=平台默认）
     */
    private Long shopId;

    /**
     * 分销总开关：1=开启 0=关闭
     */
    private Integer enabled;

    /**
     * 分佣层级数：1=一级分销 2=二级分销（合规上限两级，禁止配置更多）
     */
    private Integer maxLevel;

    /**
     * 成为分销员方式：1=无门槛申请 2=需平台审核 3=仅邀请
     */
    private Integer applyMode;

    /**
     * 上下级绑定方式：1=永久绑定 2=保护期后可换绑
     */
    private Integer bindMode;

    /**
     * 绑定保护期（天），0=不设保护
     */
    private Integer bindProtectDays;

    /**
     * 佣金结算周期：订单完成后 T+N 天转为可提现
     */
    private Integer settleDays;

    /**
     * 单笔最低提现金额（元）
     */
    private BigDecimal minWithdrawAmount;

    /**
     * 分销员等级自动升级：1=按规则自动升 0=仅手动调整
     */
    private Integer autoUpgrade;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
