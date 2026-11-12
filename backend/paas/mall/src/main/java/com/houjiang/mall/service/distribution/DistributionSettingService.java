package com.houjiang.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.distribution.DistributionSetting;
import com.houjiang.mall.mapper.distribution.DistributionSettingMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 分销设置服务类
 *
 * 提供分销总开关、层级数等配置的读取与保存。
 * 无记录时返回默认配置（默认关闭、两级分佣），保证 C 端读取永远有值。
 *
 * @author Amos
 */
@Service
public class DistributionSettingService extends ServiceImpl<DistributionSettingMapper, DistributionSetting> {

    /** 合规红线：分佣层级硬上限，超出按上限收敛 */
    public static final int MAX_LEVEL_LIMIT = 2;

    /**
     * 读取分销配置（不存在则返回默认值，不落库）
     *
     * @param shopId 店铺ID，null 或 0 表示平台配置
     */
    public DistributionSetting getSetting(Long shopId) {
        Long effectiveShopId = shopId == null ? 0L : shopId;
        LambdaQueryWrapper<DistributionSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DistributionSetting::getShopId, effectiveShopId);
        wrapper.last("LIMIT 1");
        DistributionSetting setting = getOne(wrapper);
        if (setting != null) {
            return setting;
        }
        return buildDefault(effectiveShopId);
    }

    /**
     * 保存分销配置（无则新建，有则更新），层级数强制收敛到 1..2
     */
    public DistributionSetting saveSetting(DistributionSetting input) {
        Long shopId = input.getShopId() == null ? 0L : input.getShopId();
        DistributionSetting current = getSetting(shopId);
        current.setShopId(shopId);
        current.setEnabled(input.getEnabled() != null && input.getEnabled() == 1 ? 1 : 0);
        current.setMaxLevel(clampLevel(input.getMaxLevel()));
        current.setApplyMode(input.getApplyMode() != null ? input.getApplyMode() : current.getApplyMode());
        current.setBindMode(input.getBindMode() != null ? input.getBindMode() : current.getBindMode());
        current.setBindProtectDays(input.getBindProtectDays() != null ? input.getBindProtectDays() : current.getBindProtectDays());
        current.setSettleDays(input.getSettleDays() != null ? input.getSettleDays() : current.getSettleDays());
        current.setMinWithdrawAmount(input.getMinWithdrawAmount() != null ? input.getMinWithdrawAmount() : current.getMinWithdrawAmount());
        current.setAutoUpgrade(input.getAutoUpgrade() != null && input.getAutoUpgrade() == 1 ? 1 : 0);
        current.setAutoPayoutBalance(input.getAutoPayoutBalance() != null && input.getAutoPayoutBalance() == 1 ? 1 : 0);
        current.setUpdateTime(LocalDateTime.now());
        saveOrUpdate(current);
        return current;
    }

    /**
     * 分销功能是否开启
     */
    public boolean isEnabled(Long shopId) {
        return getSetting(shopId).getEnabled() == 1;
    }

    private int clampLevel(Integer level) {
        if (level == null || level < 1) {
            return 1;
        }
        return Math.min(level, MAX_LEVEL_LIMIT);
    }

    private DistributionSetting buildDefault(Long shopId) {
        DistributionSetting setting = new DistributionSetting();
        setting.setShopId(shopId);
        // 默认关闭：需后台显式开启，避免历史店铺被静默开通分佣
        setting.setEnabled(0);
        setting.setMaxLevel(MAX_LEVEL_LIMIT);
        // 默认需审核：分佣涉及资金，宁紧勿松
        setting.setApplyMode(2);
        setting.setBindMode(1);
        setting.setBindProtectDays(15);
        setting.setSettleDays(7);
        setting.setMinWithdrawAmount(new java.math.BigDecimal("10.00"));
        setting.setAutoUpgrade(1);
        setting.setAutoPayoutBalance(1);
        return setting;
    }
}
