package com.houjiang.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.distribution.DistributionOrder;
import com.houjiang.mall.entity.distribution.DistributionSetting;
import com.houjiang.mall.entity.distribution.Distributor;
import com.houjiang.mall.entity.distribution.DistributorLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分销员等级自动升级。
 *
 * <p>消费设置 {@code autoUpgrade} 与等级表升级条件（累计销售额 / 订单数 / 下级数）。
 * 条件值为 null 或 ≤0 表示该维度不限制；已配置维度需全部满足。从高 sort 到低匹配，取最高达标等级。
 */
@Slf4j
@Service
public class DistributorLevelUpgradeService {

    private static final int ORDER_STATUS_FINISHED = 3;

    private final DistributionSettingService distributionSettingService;
    private final DistributorService distributorService;
    private final DistributorLevelService distributorLevelService;
    private final DistributionOrderService distributionOrderService;

    public DistributorLevelUpgradeService(DistributionSettingService distributionSettingService,
                                          DistributorService distributorService,
                                          DistributorLevelService distributorLevelService,
                                          DistributionOrderService distributionOrderService) {
        this.distributionSettingService = distributionSettingService;
        this.distributorService = distributorService;
        this.distributorLevelService = distributorLevelService;
        this.distributionOrderService = distributionOrderService;
    }

    /** 单人升级（结算/完成订单后调用） */
    @Transactional(rollbackFor = Exception.class)
    public boolean upgradeOne(Long distributorId) {
        if (distributorId == null) {
            return false;
        }
        Distributor distributor = distributorService.getById(distributorId);
        if (distributor == null || distributor.getStatus() == null || distributor.getStatus() != 1) {
            return false;
        }
        DistributionSetting setting = distributionSettingService.getSetting(distributor.getShopId());
        if (setting.getAutoUpgrade() == null || setting.getAutoUpgrade() != 1) {
            return false;
        }
        return applyBestLevel(distributor);
    }

    /** 批量扫描（定时任务） */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> upgradeAll(Long shopId) {
        Map<String, Object> stat = new HashMap<>();
        int checked = 0;
        int upgraded = 0;
        LambdaQueryWrapper<Distributor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Distributor::getStatus, 1);
        wrapper.eq(Distributor::getDeleted, 0);
        if (shopId != null) {
            wrapper.eq(Distributor::getShopId, shopId);
        }
        for (Distributor distributor : distributorService.list(wrapper)) {
            DistributionSetting setting = distributionSettingService.getSetting(distributor.getShopId());
            if (setting.getAutoUpgrade() == null || setting.getAutoUpgrade() != 1) {
                continue;
            }
            checked++;
            if (applyBestLevel(distributor)) {
                upgraded++;
            }
        }
        stat.put("checked", checked);
        stat.put("upgraded", upgraded);
        return stat;
    }

    private boolean applyBestLevel(Distributor distributor) {
        Long shopId = distributor.getShopId() == null ? 0L : distributor.getShopId();
        List<DistributorLevel> levels = distributorLevelService.listByShop(shopId);
        if (levels == null || levels.isEmpty()) {
            // 店铺无等级时回退平台 shopId=0
            if (shopId != 0L) {
                levels = distributorLevelService.listByShop(0L);
            }
        }
        if (levels == null || levels.isEmpty()) {
            return false;
        }
        BigDecimal sales = sumFinishedOrderAmount(distributor.getId());
        int orderCount = distributor.getOrderCount() == null ? 0 : distributor.getOrderCount();
        int subCount = distributor.getSubCount() == null ? 0 : distributor.getSubCount();

        DistributorLevel best = levels.stream()
                .filter(this::hasUpgradeCondition)
                .filter(level -> qualifies(level, sales, orderCount, subCount))
                .max(Comparator.comparing((DistributorLevel l) -> l.getSort() == null ? 0 : l.getSort()))
                .orElse(null);
        if (best == null) {
            return false;
        }
        Long current = distributor.getLevelId();
        if (current != null && current.equals(best.getId())) {
            return false;
        }
        // 只升不降：当前等级 sort 已不低于目标则跳过
        if (current != null) {
            DistributorLevel currentLevel = distributorLevelService.getById(current);
            int currentSort = currentLevel == null || currentLevel.getSort() == null ? 0 : currentLevel.getSort();
            int bestSort = best.getSort() == null ? 0 : best.getSort();
            if (currentSort >= bestSort) {
                return false;
            }
        }
        distributorService.lambdaUpdate()
                .eq(Distributor::getId, distributor.getId())
                .set(Distributor::getLevelId, best.getId())
                .update();
        log.info("分销员自动升级: distributorId={}, fromLevel={}, toLevel={}, sales={}, orders={}, subs={}",
                distributor.getId(), current, best.getId(), sales, orderCount, subCount);
        return true;
    }

    private boolean hasUpgradeCondition(DistributorLevel level) {
        if (level.getUpgradeAmount() != null && level.getUpgradeAmount().compareTo(BigDecimal.ZERO) > 0) {
            return true;
        }
        if (level.getUpgradeOrderCount() != null && level.getUpgradeOrderCount() > 0) {
            return true;
        }
        return level.getUpgradeSubCount() != null && level.getUpgradeSubCount() > 0;
    }

    private boolean qualifies(DistributorLevel level, BigDecimal sales, int orderCount, int subCount) {
        if (level.getUpgradeAmount() != null && level.getUpgradeAmount().compareTo(BigDecimal.ZERO) > 0
                && sales.compareTo(level.getUpgradeAmount()) < 0) {
            return false;
        }
        if (level.getUpgradeOrderCount() != null && level.getUpgradeOrderCount() > 0
                && orderCount < level.getUpgradeOrderCount()) {
            return false;
        }
        if (level.getUpgradeSubCount() != null && level.getUpgradeSubCount() > 0
                && subCount < level.getUpgradeSubCount()) {
            return false;
        }
        return true;
    }

    private BigDecimal sumFinishedOrderAmount(Long distributorId) {
        List<DistributionOrder> orders = distributionOrderService.lambdaQuery()
                .eq(DistributionOrder::getDistributorId, distributorId)
                .eq(DistributionOrder::getOrderStatus, ORDER_STATUS_FINISHED)
                .eq(DistributionOrder::getDeleted, 0)
                .list();
        BigDecimal sum = BigDecimal.ZERO;
        for (DistributionOrder order : orders) {
            if (order.getOrderAmount() != null) {
                sum = sum.add(order.getOrderAmount());
            }
        }
        return sum;
    }
}
