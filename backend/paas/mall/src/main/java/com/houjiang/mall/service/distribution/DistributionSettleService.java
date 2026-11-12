package com.houjiang.mall.service.distribution;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.distribution.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分销佣金结算服务
 *
 * 完整链路：已完成的分销订单 → 生成佣金（待结算，pending） → T+N 到期自动结算（available，可提现）。
 * 可后台手动触发，也可由定时任务/消息队列调用 {@link #runSettlement(Long)}。
 *
 * 资金口径（与 DistributionWithdrawService 严格配对）：
 *   生成佣金：pending += amount，total += amount
 *   到期结算：pending -= amount，available += amount
 *
 * @author Amos
 */
@Service
public class DistributionSettleService {

    /** 订单「已完成」状态快照值（订单侧写入 DistributionOrder.orderStatus，口径与商城订单一致） */
    public static final int ORDER_STATUS_FINISHED = 3;

    /** 佣金记录状态：0 待结算 1 已结算 2 已失效 */
    public static final int RECORD_STATUS_PENDING = 0;
    public static final int RECORD_STATUS_SETTLED = 1;
    public static final int RECORD_STATUS_INVALID = 2;

    private final DistributionSettingService distributionSettingService;
    private final DistributionOrderService distributionOrderService;
    private final CommissionRecordService commissionRecordService;
    private final CommissionRuleService commissionRuleService;
    private final DistributorLevelService distributorLevelService;
    private final DistributorService distributorService;
    private final DistributionGoodsService distributionGoodsService;
    private final DistributorLevelUpgradeService distributorLevelUpgradeService;

    public DistributionSettleService(DistributionSettingService distributionSettingService,
                                     DistributionOrderService distributionOrderService,
                                     CommissionRecordService commissionRecordService,
                                     CommissionRuleService commissionRuleService,
                                     DistributorLevelService distributorLevelService,
                                     DistributorService distributorService,
                                     DistributionGoodsService distributionGoodsService,
                                     DistributorLevelUpgradeService distributorLevelUpgradeService) {
        this.distributionSettingService = distributionSettingService;
        this.distributionOrderService = distributionOrderService;
        this.commissionRecordService = commissionRecordService;
        this.commissionRuleService = commissionRuleService;
        this.distributorLevelService = distributorLevelService;
        this.distributorService = distributorService;
        this.distributionGoodsService = distributionGoodsService;
        this.distributorLevelUpgradeService = distributorLevelUpgradeService;
    }

    /**
     * 一键跑完整结算：先按已完成的分销订单生成佣金，再把到期（T+N）的佣金结算为可提现
     *
     * @param shopId 店铺ID（null=平台/全部）
     * @return 统计结果
     */
    public Map<String, Object> runSettlement(Long shopId) {
        Map<String, Object> result = new HashMap<>();
        result.putAll(generateCommissions(shopId));
        result.putAll(settleDueRecords(shopId));
        return result;
    }

    /**
     * 生成佣金：扫描已完成且尚未生成佣金的分销订单，按规则计算一级（及二级）佣金
     *
     * @param shopId 店铺ID（null=全部）
     * @return 统计结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generateCommissions(Long shopId) {
        Map<String, Object> stat = new HashMap<>();
        int generated = 0;
        int skipped = 0;
        BigDecimal amount = BigDecimal.ZERO;

        LambdaQueryWrapper<DistributionOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(DistributionOrder::getOrderStatus, ORDER_STATUS_FINISHED);
        orderWrapper.eq(DistributionOrder::getDeleted, 0);
        if (shopId != null) {
            orderWrapper.eq(DistributionOrder::getShopId, shopId);
        }
        List<DistributionOrder> orders = distributionOrderService.list(orderWrapper);

        for (DistributionOrder distributionOrder : orders) {
            GenerateStat one = generateOne(distributionOrder);
            generated += one.generated;
            skipped += one.skipped;
            amount = amount.add(one.amount);
            if (one.generated > 0 && distributionOrder.getDistributorId() != null) {
                tryUpgrade(distributionOrder.getDistributorId());
            }
        }

        stat.put("generated", generated);
        stat.put("generatedAmount", amount.setScale(2, RoundingMode.HALF_UP));
        stat.put("skipped", skipped);
        return stat;
    }

    /**
     * T+N 到期结算：把创建时间超过结算周期的待结算佣金转为已结算（可提现）
     *
     * @param shopId 店铺ID（null=全部）
     * @return 统计结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> settleDueRecords(Long shopId) {
        Map<String, Object> stat = new HashMap<>();
        int settled = 0;
        BigDecimal amount = BigDecimal.ZERO;

        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionRecord::getStatus, RECORD_STATUS_PENDING);
        wrapper.eq(CommissionRecord::getDeleted, 0);
        if (shopId != null) {
            wrapper.eq(CommissionRecord::getShopId, shopId);
        }
        List<CommissionRecord> pending = commissionRecordService.list(wrapper);
        if (pending.isEmpty()) {
            stat.put("settled", 0);
            stat.put("settledAmount", BigDecimal.ZERO.setScale(2));
            return stat;
        }

        LocalDateTime now = LocalDateTime.now();
        for (CommissionRecord record : pending) {
            DistributionSetting setting = distributionSettingService.getSetting(record.getShopId());
            int days = setting.getSettleDays() == null ? 0 : setting.getSettleDays();
            LocalDateTime createTime = record.getCreateTime() == null ? now : record.getCreateTime();
            if (createTime.plusDays(days).isAfter(now)) {
                continue;
            }
            BigDecimal commission = nz(record.getCommissionAmount());
            // 与后台手工结算共用 CommissionRecordService.settle，确保「状态 + 资金台账」只有一个口径
            if (!commissionRecordService.settle(record.getId())) {
                continue;
            }
            settled++;
            amount = amount.add(commission);
        }

        stat.put("settled", settled);
        stat.put("settledAmount", amount.setScale(2, RoundingMode.HALF_UP));
        return stat;
    }

    /**
     * 结算概览：待结算佣金 / 已结算佣金 / 待结算笔数 / 分销员可提现余额
     */
    public Map<String, Object> stats(Long shopId) {
        Map<String, Object> stat = new HashMap<>();
        LambdaQueryWrapper<CommissionRecord> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(CommissionRecord::getStatus, RECORD_STATUS_PENDING);
        pendingWrapper.eq(CommissionRecord::getDeleted, 0);
        if (shopId != null) {
            pendingWrapper.eq(CommissionRecord::getShopId, shopId);
        }
        BigDecimal pendingAmount = sum(commissionRecordService.list(pendingWrapper));

        LambdaQueryWrapper<CommissionRecord> settledWrapper = new LambdaQueryWrapper<>();
        settledWrapper.eq(CommissionRecord::getStatus, RECORD_STATUS_SETTLED);
        settledWrapper.eq(CommissionRecord::getDeleted, 0);
        if (shopId != null) {
            settledWrapper.eq(CommissionRecord::getShopId, shopId);
        }
        BigDecimal settledAmount = sum(commissionRecordService.list(settledWrapper));

        // 可提现余额取自分销员台账：已结算佣金会随提现/驳回变化，不能用已结算金额代替
        LambdaQueryWrapper<Distributor> distributorWrapper = new LambdaQueryWrapper<>();
        distributorWrapper.eq(shopId != null, Distributor::getShopId, shopId);
        BigDecimal availableAmount = BigDecimal.ZERO;
        for (Distributor distributor : distributorService.list(distributorWrapper)) {
            availableAmount = availableAmount.add(nz(distributor.getAvailableCommission()));
        }

        stat.put("pendingAmount", pendingAmount);
        stat.put("settledAmount", settledAmount);
        stat.put("pendingCount", commissionRecordService.count(pendingWrapper));
        stat.put("availableAmount", availableAmount.setScale(2, RoundingMode.HALF_UP));
        return stat;
    }

    /** 供订单完成事件调用：为单个已完成订单生成佣金（幂等，只处理这一单） */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generateForOrder(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        LambdaQueryWrapper<DistributionOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DistributionOrder::getOrderId, orderId);
        wrapper.eq(DistributionOrder::getDeleted, 0);
        wrapper.last("limit 1");
        DistributionOrder distributionOrder = distributionOrderService.getOne(wrapper);
        if (distributionOrder == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("generated", 0);
            empty.put("generatedAmount", BigDecimal.ZERO.setScale(2));
            empty.put("skipped", 0);
            return empty;
        }
        GenerateStat one = generateOne(distributionOrder);
        if (one.generated > 0 && distributionOrder.getDistributorId() != null) {
            tryUpgrade(distributionOrder.getDistributorId());
            if (distributionOrder.getDistributorId() != null) {
                Distributor d = distributorService.getById(distributionOrder.getDistributorId());
                if (d != null && d.getParentId() != null && d.getParentId() > 0) {
                    tryUpgrade(d.getParentId());
                }
            }
        }
        Map<String, Object> stat = new HashMap<>();
        stat.put("generated", one.generated);
        stat.put("generatedAmount", one.amount.setScale(2, RoundingMode.HALF_UP));
        stat.put("skipped", one.skipped);
        return stat;
    }

    private void tryUpgrade(Long distributorId) {
        try {
            distributorLevelUpgradeService.upgradeOne(distributorId);
        } catch (Exception ignored) {
            // 升级失败不影响佣金主链路
        }
    }

    /**
     * 订单失效（退款/关闭）：把该订单已生成但尚未结算的佣金全部作废。
     * 已结算（可提现）佣金不再追回。
     *
     * @return 作废条数
     */
    @Transactional(rollbackFor = Exception.class)
    public int invalidateByOrder(Long orderId) {
        Object invalidated = clawbackByOrder(orderId, null, true).get("invalidated");
        return invalidated instanceof Number ? ((Number) invalidated).intValue() : 0;
    }

    /**
     * 按退款金额等比冲减待结算佣金。
     *
     * <p>口径：keepRatio = max(0, 1 - cumulativeRefunded / orderAmount)；
     * 每条 pending 记录目标金额 = original_commission_amount × keepRatio；
     * 差额从分销员 pending/total 回滚。已结算不追回。
     * {@code fullInvalidate=true} 或累计退款 ≥ 订单金额时整单作废待结算。
     *
     * @param orderId         订单ID
     * @param refundAmount    本次退款金额；null 且 fullInvalidate=true 表示整单作废
     * @param fullInvalidate  强制整单作废（取消订单等）
     * @return invalidated / adjusted / refundedAmount / keepRatio
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> clawbackByOrder(Long orderId, BigDecimal refundAmount, boolean fullInvalidate) {
        Map<String, Object> result = new HashMap<>();
        result.put("invalidated", 0);
        result.put("adjusted", 0);
        if (orderId == null) {
            return result;
        }
        LambdaQueryWrapper<DistributionOrder> orderQ = new LambdaQueryWrapper<>();
        orderQ.eq(DistributionOrder::getOrderId, orderId);
        orderQ.eq(DistributionOrder::getDeleted, 0);
        orderQ.last("limit 1");
        DistributionOrder distributionOrder = distributionOrderService.getOne(orderQ);

        BigDecimal orderAmount = distributionOrder == null ? BigDecimal.ZERO : nz(distributionOrder.getOrderAmount());
        BigDecimal prevRefunded = distributionOrder == null ? BigDecimal.ZERO : nz(distributionOrder.getRefundedAmount());
        BigDecimal addRefund = refundAmount == null ? BigDecimal.ZERO : nz(refundAmount);
        if (addRefund.compareTo(BigDecimal.ZERO) < 0) {
            addRefund = BigDecimal.ZERO;
        }

        BigDecimal newRefunded = prevRefunded;
        if (fullInvalidate || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            newRefunded = orderAmount.compareTo(BigDecimal.ZERO) > 0 ? orderAmount : prevRefunded.max(addRefund);
            fullInvalidate = true;
        } else if (addRefund.compareTo(BigDecimal.ZERO) > 0) {
            newRefunded = prevRefunded.add(addRefund);
            if (newRefunded.compareTo(orderAmount) > 0) {
                newRefunded = orderAmount;
            }
        }

        if (distributionOrder != null && newRefunded.compareTo(prevRefunded) != 0) {
            distributionOrder.setRefundedAmount(newRefunded);
            distributionOrder.setUpdateTime(LocalDateTime.now());
            distributionOrderService.updateById(distributionOrder);
        }

        boolean wipe = fullInvalidate || (orderAmount.compareTo(BigDecimal.ZERO) > 0
                && newRefunded.compareTo(orderAmount) >= 0);
        BigDecimal keepRatio = BigDecimal.ZERO;
        if (!wipe && orderAmount.compareTo(BigDecimal.ZERO) > 0) {
            keepRatio = orderAmount.subtract(newRefunded)
                    .divide(orderAmount, 8, RoundingMode.HALF_UP)
                    .max(BigDecimal.ZERO);
        }

        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionRecord::getOrderId, orderId);
        wrapper.eq(CommissionRecord::getStatus, RECORD_STATUS_PENDING);
        wrapper.eq(CommissionRecord::getDeleted, 0);
        List<CommissionRecord> pending = commissionRecordService.list(wrapper);
        int invalidated = 0;
        int adjusted = 0;
        LocalDateTime now = LocalDateTime.now();

        for (CommissionRecord record : pending) {
            BigDecimal original = record.getOriginalCommissionAmount() != null
                    ? nz(record.getOriginalCommissionAmount()) : nz(record.getCommissionAmount());
            if (record.getOriginalCommissionAmount() == null) {
                record.setOriginalCommissionAmount(original);
            }
            BigDecimal current = nz(record.getCommissionAmount());
            BigDecimal target = wipe ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : original.multiply(keepRatio).setScale(2, RoundingMode.HALF_UP);
            BigDecimal delta = current.subtract(target);
            if (delta.compareTo(BigDecimal.ZERO) <= 0 && !wipe) {
                if (record.getOriginalCommissionAmount() != null
                        && record.getId() != null
                        && current.compareTo(original) == 0) {
                    // 仅补 original，无金额变化
                    commissionRecordService.updateById(record);
                }
                continue;
            }
            if (wipe || target.compareTo(BigDecimal.ZERO) <= 0) {
                record.setCommissionAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                record.setStatus(RECORD_STATUS_INVALID);
                record.setRemark(appendRemark(record.getRemark(), "退款/关单作废"));
                record.setUpdateTime(now);
                if (commissionRecordService.updateById(record) && delta.compareTo(BigDecimal.ZERO) > 0) {
                    subtractPending(record.getDistributorId(), delta);
                    invalidated++;
                }
            } else {
                record.setCommissionAmount(target);
                record.setRemark(appendRemark(record.getRemark(),
                        "退款冲减 keepRatio=" + keepRatio.setScale(4, RoundingMode.HALF_UP)));
                record.setUpdateTime(now);
                if (commissionRecordService.updateById(record) && delta.compareTo(BigDecimal.ZERO) > 0) {
                    subtractPending(record.getDistributorId(), delta);
                    adjusted++;
                }
            }
        }

        if (distributionOrder != null && wipe) {
            // 状态由 Hook 置失效；这里同步回填剩余佣金
            distributionOrder.setCommissionAmount(sumRemainingCommission(orderId));
            distributionOrder.setUpdateTime(now);
            distributionOrderService.updateById(distributionOrder);
        } else if (distributionOrder != null && adjusted > 0) {
            distributionOrder.setCommissionAmount(sumRemainingCommission(orderId));
            distributionOrder.setUpdateTime(now);
            distributionOrderService.updateById(distributionOrder);
        }

        result.put("invalidated", invalidated);
        result.put("adjusted", adjusted);
        result.put("refundedAmount", newRefunded.setScale(2, RoundingMode.HALF_UP));
        result.put("keepRatio", keepRatio.setScale(4, RoundingMode.HALF_UP));
        result.put("fullInvalidate", wipe);
        return result;
    }

    private BigDecimal sumRemainingCommission(Long orderId) {
        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionRecord::getOrderId, orderId);
        wrapper.in(CommissionRecord::getStatus, RECORD_STATUS_PENDING, RECORD_STATUS_SETTLED);
        wrapper.eq(CommissionRecord::getDeleted, 0);
        return sum(commissionRecordService.list(wrapper));
    }

    private void subtractPending(Long distributorId, BigDecimal amount) {
        if (distributorId == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        distributorService.lambdaUpdate()
                .eq(Distributor::getId, distributorId)
                .setSql("pending_commission = pending_commission - " + amount.toPlainString()
                        + ", total_commission = total_commission - " + amount.toPlainString())
                .update();
    }

    private static String appendRemark(String remark, String extra) {
        if (!StringUtils.hasText(remark)) {
            return extra;
        }
        if (remark.contains(extra)) {
            return remark;
        }
        return remark + ";" + extra;
    }

    /**
     * 统计某笔订单已产生的佣金记录条数（含已失效），用于判断分销订单是否已被结算过。
     */
    public long countRecordsByOrder(Long orderId) {
        if (orderId == null) {
            return 0L;
        }
        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionRecord::getOrderId, orderId);
        wrapper.eq(CommissionRecord::getDeleted, 0);
        return commissionRecordService.count(wrapper);
    }

    /** 单笔分销订单的佣金生成统计 */
    private static class GenerateStat {
        private int generated;
        private int skipped;
        private BigDecimal amount = BigDecimal.ZERO;
    }

    /**
     * 为单个分销订单生成一/二级佣金（按层级分别幂等）。
     *
     * 旧实现只要一级已存在就整单跳过：若一级生成成功、二级因异常或上级临时禁用而失败，
     * 重跑时二级佣金将永久丢失。现在两级分别判重，缺哪一级补哪一级；订单佣金回填改为
     * 汇总该订单全部有效记录，避免「只补二级」时把一级金额覆盖掉。
     */
    private GenerateStat generateOne(DistributionOrder distributionOrder) {
        GenerateStat stat = new GenerateStat();
        if (distributionOrder.getDistributorId() == null || distributionOrder.getOrderId() == null) {
            return stat;
        }
        Long orderId = distributionOrder.getOrderId();
        boolean hasLevel1 = hasRecord(orderId, 1);

        DistributionSetting setting = distributionSettingService.getSetting(distributionOrder.getShopId());
        if (setting.getEnabled() == null || setting.getEnabled() != 1) {
            stat.skipped++;
            return stat;
        }
        Distributor distributor = distributorService.getById(distributionOrder.getDistributorId());
        if (distributor == null || distributor.getStatus() == null || distributor.getStatus() != 1) {
            stat.skipped++;
            return stat;
        }
        BigDecimal orderAmount = nz(distributionOrder.getOrderAmount());
        if (orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            stat.skipped++;
            return stat;
        }

        int maxLevel = setting.getMaxLevel() == null ? 1 : setting.getMaxLevel();
        boolean eligibleLevel2 = maxLevel >= 2
                && distributor.getParentId() != null && distributor.getParentId() > 0;
        boolean needLevel2 = eligibleLevel2 && !hasRecord(orderId, 2);
        if (hasLevel1 && !needLevel2) {
            stat.skipped++;
            return stat;
        }

        List<GoodsLine> lines = parseGoodsLines(distributionOrder.getGoodsSnapshot(), orderAmount);
        String goodsNameSummary = summarizeGoodsNames(lines);

        // 一级：按行汇总（分销商品覆盖 > 规则 > 等级）
        if (!hasLevel1) {
            LineAgg level1 = aggregateLevel(distributionOrder.getShopId(), distributor.getLevelId(), 1, lines);
            commissionRecordService.createRecord(buildRecord(distributionOrder, distributor.getId(),
                    level1.amount, level1.rate, 1, null, goodsNameSummary));
            addPending(distributor.getId(), level1.amount, true);
            stat.generated++;
            stat.amount = stat.amount.add(level1.amount);
        }

        // 二级佣金（合规上限两级），独立于一级判重
        if (needLevel2) {
            Distributor parent = distributorService.getById(distributor.getParentId());
            if (parent != null && parent.getStatus() != null && parent.getStatus() == 1) {
                LineAgg level2 = aggregateLevel(distributionOrder.getShopId(), parent.getLevelId(), 2, lines);
                commissionRecordService.createRecord(buildRecord(distributionOrder, parent.getId(),
                        level2.amount, level2.rate, 2, distributor.getId(), goodsNameSummary));
                addPending(parent.getId(), level2.amount, false);
                stat.generated++;
                stat.amount = stat.amount.add(level2.amount);
            }
        }

        if (stat.generated == 0) {
            stat.skipped++;
            return stat;
        }
        // 回填订单佣金：汇总全部有效记录，兼容「仅补生成某一级」的重跑场景
        distributionOrder.setCommissionAmount(sumRemainingCommission(orderId));
        distributionOrder.setUpdateTime(LocalDateTime.now());
        distributionOrderService.updateById(distributionOrder);
        return stat;
    }

    private boolean hasRecord(Long orderId, int level) {
        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionRecord::getOrderId, orderId);
        wrapper.eq(CommissionRecord::getLevel, level);
        wrapper.eq(CommissionRecord::getDeleted, 0);
        return commissionRecordService.count(wrapper) > 0;
    }

    private CommissionRecord buildRecord(DistributionOrder order, Long distributorId, BigDecimal commission,
                                         BigDecimal rate, int level, Long sourceDistributorId, String goodsName) {
        CommissionRecord record = new CommissionRecord();
        record.setDistributorId(distributorId);
        record.setShopId(order.getShopId());
        record.setOrderId(order.getOrderId());
        record.setOrderNo(order.getOrderNo());
        record.setGoodsName(goodsName);
        record.setOrderAmount(nz(order.getOrderAmount()));
        BigDecimal amount = commission == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : commission.setScale(2, RoundingMode.HALF_UP);
        record.setCommissionAmount(amount);
        record.setOriginalCommissionAmount(amount);
        record.setCommissionRate(rate);
        record.setLevel(level);
        record.setSourceDistributorId(sourceDistributorId);
        record.setRemark("订单完成自动分佣");
        return record;
    }

    private void addPending(Long distributorId, BigDecimal amount, boolean countOrder) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        distributorService.lambdaUpdate()
                .eq(Distributor::getId, distributorId)
                .setSql("pending_commission = pending_commission + " + amount.toPlainString()
                        + ", total_commission = total_commission + " + amount.toPlainString()
                        + (countOrder ? ", order_count = order_count + 1" : ""))
                .update();
    }

    /**
     * 按订单行汇总某一层级佣金。
     * 优先级：分销商品覆盖（仅一级）> 佣金规则（商品→分类→全店）> 分销等级。
     */
    private LineAgg aggregateLevel(Long shopId, Long levelId, int level, List<GoodsLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal amountBase = BigDecimal.ZERO;
        boolean shopFixedApplied = false;
        for (GoodsLine line : lines) {
            ResolvedCommission resolved = resolveLineCommission(
                    shopId, levelId, level, line.goodsId, line.categoryId, line.amount, shopFixedApplied);
            if (resolved.shopFixedApplied) {
                shopFixedApplied = true;
            }
            total = total.add(resolved.amount);
            amountBase = amountBase.add(nz(line.amount));
        }
        LineAgg agg = new LineAgg();
        agg.amount = total.setScale(2, RoundingMode.HALF_UP);
        if (amountBase.compareTo(BigDecimal.ZERO) > 0 && agg.amount.compareTo(BigDecimal.ZERO) > 0) {
            agg.rate = agg.amount.multiply(BigDecimal.valueOf(100))
                    .divide(amountBase, 2, RoundingMode.HALF_UP);
        } else {
            agg.rate = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return agg;
    }

    private ResolvedCommission resolveLineCommission(Long shopId, Long levelId, int level,
                                                     Long goodsId, Long categoryId, BigDecimal lineAmount,
                                                     boolean shopFixedAlreadyApplied) {
        ResolvedCommission out = new ResolvedCommission();
        BigDecimal amount = nz(lineAmount);

        // 1) 分销商品覆盖：仅一级
        if (level == 1 && goodsId != null) {
            DistributionGoods goods = distributionGoodsService.getEnabledByShopAndGoods(shopId, goodsId);
            if (goods != null && goods.getCommissionRate() != null
                    && goods.getCommissionRate().compareTo(BigDecimal.ZERO) > 0) {
                out.amount = calcByRate(amount, goods.getCommissionRate());
                out.rate = goods.getCommissionRate();
                return out;
            }
        }

        // 2) 佣金规则
        CommissionRule rule = commissionRuleService.getApplicableRule(shopId, categoryId, goodsId);
        if (rule != null) {
            Integer calcType = rule.getCalcType() == null ? 1 : rule.getCalcType();
            if (calcType == 2) {
                BigDecimal fixed = level == 1 ? rule.getLevel1Amount() : rule.getLevel2Amount();
                if (fixed != null && fixed.compareTo(BigDecimal.ZERO) > 0) {
                    Integer type = rule.getType() == null ? 1 : rule.getType();
                    if (type == 1) {
                        // 全店固定金额整单只计一次
                        if (shopFixedAlreadyApplied) {
                            out.amount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                            return out;
                        }
                        out.amount = fixed.setScale(2, RoundingMode.HALF_UP);
                        out.shopFixedApplied = true;
                        return out;
                    }
                    out.amount = fixed.setScale(2, RoundingMode.HALF_UP);
                    return out;
                }
            } else {
                BigDecimal rate = level == 1 ? rule.getLevel1Rate() : rule.getLevel2Rate();
                if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                    out.amount = calcByRate(amount, rate);
                    out.rate = rate;
                    return out;
                }
            }
        }

        // 3) 分销等级
        DistributorLevel distributorLevel = null;
        if (levelId != null) {
            distributorLevel = distributorLevelService.getById(levelId);
        }
        if (distributorLevel == null) {
            distributorLevel = distributorLevelService.getDefaultLevel(shopId);
        }
        if (distributorLevel != null) {
            BigDecimal rate = level == 1 ? distributorLevel.getLevel1Rate() : distributorLevel.getLevel2Rate();
            if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                out.amount = calcByRate(amount, rate);
                out.rate = rate;
                return out;
            }
        }
        out.amount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        out.rate = BigDecimal.ZERO;
        return out;
    }

    private BigDecimal calcByRate(BigDecimal baseAmount, BigDecimal ratePercent) {
        if (ratePercent == null || ratePercent.compareTo(BigDecimal.ZERO) <= 0
                || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return baseAmount.multiply(ratePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private List<GoodsLine> parseGoodsLines(String snapshot, BigDecimal fallbackAmount) {
        List<GoodsLine> lines = new ArrayList<>();
        if (StringUtils.hasText(snapshot)) {
            try {
                JSONArray arr = JSON.parseArray(snapshot);
                if (arr != null) {
                    for (int i = 0; i < arr.size(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        if (o == null) {
                            continue;
                        }
                        GoodsLine line = new GoodsLine();
                        line.goodsId = o.getLong("goodsId");
                        line.categoryId = o.getLong("categoryId");
                        line.goodsName = o.getString("goodsName");
                        line.amount = o.getBigDecimal("amount");
                        if (line.amount == null) {
                            line.amount = BigDecimal.ZERO;
                        }
                        if (line.amount.compareTo(BigDecimal.ZERO) > 0) {
                            lines.add(line);
                        }
                    }
                }
            } catch (Exception ignored) {
                // 快照损坏时回退整单
            }
        }
        if (lines.isEmpty()) {
            GoodsLine line = new GoodsLine();
            line.amount = nz(fallbackAmount);
            lines.add(line);
        }
        return lines;
    }

    private static String summarizeGoodsNames(List<GoodsLine> lines) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (GoodsLine line : lines) {
            if (!StringUtils.hasText(line.goodsName)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(line.goodsName);
            if (sb.length() > 120) {
                sb.append("…");
                break;
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private static class GoodsLine {
        private Long goodsId;
        private Long categoryId;
        private String goodsName;
        private BigDecimal amount;
    }

    private static class LineAgg {
        private BigDecimal amount = BigDecimal.ZERO;
        private BigDecimal rate = BigDecimal.ZERO;
    }

    private static class ResolvedCommission {
        private BigDecimal amount = BigDecimal.ZERO;
        private BigDecimal rate = BigDecimal.ZERO;
        private boolean shopFixedApplied;
    }

    private BigDecimal sum(List<CommissionRecord> records) {
        BigDecimal total = BigDecimal.ZERO;
        if (records == null || records.isEmpty()) {
            return total.setScale(2, RoundingMode.HALF_UP);
        }
        for (CommissionRecord record : records) {
            total = total.add(nz(record.getCommissionAmount()));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /** 供前端展示用：结算任务可处理的订单状态列表 */
    public List<Integer> finishedOrderStatuses() {
        List<Integer> statuses = new ArrayList<>();
        statuses.add(ORDER_STATUS_FINISHED);
        return statuses;
    }
}
