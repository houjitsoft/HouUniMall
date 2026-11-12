package com.houjiang.mall.service.distribution;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 分销佣金自动结算任务
 *
 * <p>订单完成事件（{@code /internal/distribution/order/finish}）只负责把佣金生成为
 * 「待结算」；真正的可提现要等 T+N 售后期过后。本任务定期把到期的待结算佣金
 * 转为可提现，补齐「订单完成 → 结算 → 提现」闭环中不需要人工点按钮的那一环。
 *
 * <p>幂等：{@link DistributionSettleService#settleDueRecords} 只处理 status=0 且
 * 创建时间超过结算周期的记录，重复执行不会重复入账。
 *
 * @author Amos
 */
@Slf4j
@Component
public class DistributionSettleTask {

    private final DistributionSettleService distributionSettleService;
    private final DistributionOrderHookService distributionOrderHookService;
    private final DistributorLevelUpgradeService distributorLevelUpgradeService;

    public DistributionSettleTask(DistributionSettleService distributionSettleService,
                                  DistributionOrderHookService distributionOrderHookService,
                                  DistributorLevelUpgradeService distributorLevelUpgradeService) {
        this.distributionSettleService = distributionSettleService;
        this.distributionOrderHookService = distributionOrderHookService;
        this.distributorLevelUpgradeService = distributorLevelUpgradeService;
    }

    /** 启动后 3 分钟首次执行，之后每 30 分钟一次 */
    @Scheduled(initialDelayString = "${houcloud.distribution.settle-initial-delay:180000}",
            fixedDelayString = "${houcloud.distribution.settle-fixed-delay:1800000}")
    public void settleDue() {
        try {
            Map<String, Object> stat = distributionSettleService.settleDueRecords(null);
            Object settled = stat.get("settled");
            if (settled instanceof Integer count && count > 0) {
                log.info("分销佣金自动结算完成: settled={}, amount={}", count, stat.get("settledAmount"));
            }
        } catch (Exception e) {
            // 结算失败不能影响商城主流程，只记录日志，下个周期重试
            log.error("分销佣金自动结算失败: {}", e.getMessage(), e);
        }
        try {
            Map<String, Object> upgrade = distributorLevelUpgradeService.upgradeAll(null);
            Object upgraded = upgrade.get("upgraded");
            if (upgraded instanceof Integer count && count > 0) {
                log.info("分销员等级自动升级完成: upgraded={}, checked={}", count, upgrade.get("checked"));
            }
        } catch (Exception e) {
            log.error("分销员等级自动升级失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 清理下单后长期未完成的孤儿分销订单。
     *
     * <p>下单即登记（用于预估收益展示），未付款 / 下单事务回滚的会留下永远等不到的记录，
     * 每天清一次，避免污染客户统计。
     */
    @Scheduled(initialDelayString = "${houcloud.distribution.cleanup-initial-delay:600000}",
            fixedDelayString = "${houcloud.distribution.cleanup-fixed-delay:86400000}")
    public void cleanupStaleOrders() {
        try {
            distributionOrderHookService.cleanupStaleOrders(7);
        } catch (Exception e) {
            log.error("分销孤儿订单清理失败: {}", e.getMessage(), e);
        }
    }
}
