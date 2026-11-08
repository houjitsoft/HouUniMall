package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginOrder;
import com.houjiang.mall.entity.market.PluginRevenueShare;
import com.houjiang.mall.entity.market.PluginSettlementBatch;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.PluginRevenueShareMapper;
import com.houjiang.mall.mapper.market.PluginSettlementBatchMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 插件第三方结算 / 分账（审计 2026-10-27 缺失能力 ②）。
 *
 * <p><b>规则</b>：
 * <ol>
 *   <li>订单支付成功（非沙箱、非 MOCK 渠道）→ 按<b>下单结算时刻</b>的平台抽成比例，
 *       落一条 {@code hou_plugin_revenue_share} 流水，平台与开发者金额当场固化；</li>
 *   <li>运营按开发者 + 时间区间生成结算批次，批次金额 = 期间待结算流水之和；</li>
 *   <li>打款后回填批次状态与凭证。批次是财务凭据，事后<b>不随流水变动</b>。</li>
 * </ol>
 *
 * <p><b>为什么必须在这里固化抽成比例</b>：如果结算时再读配置，改一次费率会把历史订单
 * 一起改掉——同一个订单两次查看金额不同，对账永远做不平。
 *
 * <p><b>幂等</b>：流水表 {@code uk_share_order} 唯一键兜底，支付回调与前端轮询
 * 重复触发结算也只记一笔账。
 *
 * @author Amos
 * @since 2026-10-27
 */
@Slf4j
@Service
public class PluginSettlementService {

    private static final DateTimeFormatter BATCH_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PluginRevenueShareMapper shareMapper;
    private final PluginSettlementBatchMapper batchMapper;
    private final MarketPluginMapper marketPluginMapper;

    /**
     * 平台抽成比例（0.3 = 30%）。默认 30%，与官方商城对外口径一致。
     */
    @Value("${houcloud.plugin.market.commission-rate:0.3}")
    private BigDecimal commissionRate = new BigDecimal("0.3");

    @Autowired
    public PluginSettlementService(PluginRevenueShareMapper shareMapper,
                                   PluginSettlementBatchMapper batchMapper,
                                   MarketPluginMapper marketPluginMapper) {
        this.shareMapper = shareMapper;
        this.batchMapper = batchMapper;
        this.marketPluginMapper = marketPluginMapper;
    }

    /**
     * 订单结算后记账（幂等）。
     *
     * <p>跳过三类订单：金额 ≤ 0（试用/免费）、沙箱单、MOCK 渠道单——
     * 这些不是真实收款，记进来会虚增开发者应结金额。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void recordForOrder(MarketPluginOrder order) {
        if (order == null || !StringUtils.hasText(order.getOrderNo())) {
            return;
        }
        BigDecimal gross = order.getAmount();
        if (gross == null || gross.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (Integer.valueOf(1).equals(order.getSandbox()) || "MOCK".equalsIgnoreCase(order.getChannel())) {
            return;
        }
        MarketPlugin plugin = marketPluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, order.getPluginCode()).last("limit 1"));
        Long developerId = plugin == null ? null : plugin.getAuthorId();
        if (developerId == null) {
            // 平台自营插件：没有第三方开发者，不产生分账
            return;
        }
        PluginRevenueShare existing = shareMapper.selectOne(new LambdaQueryWrapper<PluginRevenueShare>()
                .eq(PluginRevenueShare::getOrderNo, order.getOrderNo()).last("limit 1"));
        if (existing != null) {
            return;
        }
        BigDecimal rate = commissionRate == null ? BigDecimal.ZERO : commissionRate;
        BigDecimal platform = gross.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal developer = gross.subtract(platform).setScale(2, RoundingMode.HALF_UP);

        PluginRevenueShare share = new PluginRevenueShare();
        share.setOrderNo(order.getOrderNo());
        share.setPluginCode(order.getPluginCode());
        share.setDeveloperId(developerId);
        share.setBuyerId(order.getBuyerId());
        share.setGrossAmount(gross.setScale(2, RoundingMode.HALF_UP));
        share.setCurrency(StringUtils.hasText(order.getCurrency()) ? order.getCurrency() : "CNY");
        share.setCommissionRate(rate);
        share.setPlatformAmount(platform);
        share.setDeveloperAmount(developer);
        share.setStatus(PluginRevenueShare.STATUS_PENDING);
        share.setBatchNo("");
        share.setRemark("");
        share.setCreateTime(LocalDateTime.now());
        shareMapper.insert(share);
        log.info("分账流水已记录: order={} plugin={} developer={} gross={} platform={} developerAmount={}",
                order.getOrderNo(), order.getPluginCode(), developerId, share.getGrossAmount(), platform, developer);
    }

    /** 分账流水列表（developerId 为空表示全平台，供运营对账） */
    public List<Map<String, Object>> listShares(Long developerId, Integer status, int limit) {
        LambdaQueryWrapper<PluginRevenueShare> wrapper = new LambdaQueryWrapper<PluginRevenueShare>()
                .orderByDesc(PluginRevenueShare::getId);
        if (developerId != null) {
            wrapper.eq(PluginRevenueShare::getDeveloperId, developerId);
        }
        if (status != null) {
            wrapper.eq(PluginRevenueShare::getStatus, status);
        }
        wrapper.last("limit " + Math.min(Math.max(limit, 1), 500));
        List<PluginRevenueShare> rows = shareMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (PluginRevenueShare row : rows) {
            result.add(toShareView(row));
        }
        return result;
    }

    /** 汇总：待结算 / 已结算 / 本月新增 */
    public Map<String, Object> summary(Long developerId) {
        List<PluginRevenueShare> all = shareMapper.selectList(new LambdaQueryWrapper<PluginRevenueShare>()
                .eq(developerId != null, PluginRevenueShare::getDeveloperId, developerId));
        BigDecimal pending = BigDecimal.ZERO;
        BigDecimal settled = BigDecimal.ZERO;
        BigDecimal month = BigDecimal.ZERO;
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        for (PluginRevenueShare row : all) {
            BigDecimal amount = row.getDeveloperAmount() == null ? BigDecimal.ZERO : row.getDeveloperAmount();
            if (Integer.valueOf(PluginRevenueShare.STATUS_SETTLED).equals(row.getStatus())) {
                settled = settled.add(amount);
            } else if (Integer.valueOf(PluginRevenueShare.STATUS_PENDING).equals(row.getStatus())) {
                pending = pending.add(amount);
                if (row.getCreateTime() != null && row.getCreateTime().isAfter(monthStart)) {
                    month = month.add(amount);
                }
            }
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pendingAmount", pending.setScale(2, RoundingMode.HALF_UP));
        view.put("settledAmount", settled.setScale(2, RoundingMode.HALF_UP));
        view.put("monthAmount", month.setScale(2, RoundingMode.HALF_UP));
        view.put("commissionRate", (commissionRate == null ? BigDecimal.ZERO : commissionRate).setScale(4, RoundingMode.HALF_UP));
        return view;
    }

    /**
     * 生成结算批次：把该开发者在一段时间内「待结算」的流水汇总成一张批次单。
     *
     * @param from 起始日期（含），可为空表示不限
     * @param to   截止日期（含），可为空表示不限
     */
    @Transactional(rollbackFor = Exception.class)
    public PluginSettlementBatch settle(Long developerId, LocalDate from, LocalDate to, String remark) {
        if (developerId == null) {
            throw new IllegalArgumentException("请指定开发者");
        }
        LambdaQueryWrapper<PluginRevenueShare> wrapper = new LambdaQueryWrapper<PluginRevenueShare>()
                .eq(PluginRevenueShare::getDeveloperId, developerId)
                .eq(PluginRevenueShare::getStatus, PluginRevenueShare.STATUS_PENDING);
        if (from != null) {
            wrapper.ge(PluginRevenueShare::getCreateTime, from.atStartOfDay());
        }
        if (to != null) {
            wrapper.le(PluginRevenueShare::getCreateTime, to.plusDays(1).atStartOfDay());
        }
        List<PluginRevenueShare> shares = shareMapper.selectList(wrapper.orderByAsc(PluginRevenueShare::getId));
        if (shares.isEmpty()) {
            throw new IllegalStateException("该开发者在本区间内没有待结算的流水");
        }
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal platform = BigDecimal.ZERO;
        BigDecimal developer = BigDecimal.ZERO;
        for (PluginRevenueShare share : shares) {
            gross = gross.add(share.getGrossAmount() == null ? BigDecimal.ZERO : share.getGrossAmount());
            platform = platform.add(share.getPlatformAmount() == null ? BigDecimal.ZERO : share.getPlatformAmount());
            developer = developer.add(share.getDeveloperAmount() == null ? BigDecimal.ZERO : share.getDeveloperAmount());
        }
        String batchNo = nextBatchNo();
        PluginSettlementBatch batch = new PluginSettlementBatch();
        batch.setBatchNo(batchNo);
        batch.setDeveloperId(developerId);
        batch.setPeriodStart(from);
        batch.setPeriodEnd(to);
        batch.setShareCount(shares.size());
        batch.setGrossAmount(gross.setScale(2, RoundingMode.HALF_UP));
        batch.setPlatformAmount(platform.setScale(2, RoundingMode.HALF_UP));
        batch.setDeveloperAmount(developer.setScale(2, RoundingMode.HALF_UP));
        batch.setCurrency(shares.get(0).getCurrency());
        batch.setStatus(PluginSettlementBatch.STATUS_PENDING);
        batch.setVoucherUrl("");
        batch.setRemark(remark == null ? "" : remark.trim());
        batch.setCreateTime(LocalDateTime.now());
        batchMapper.insert(batch);

        // 流水打批次号并置为已结算：条件更新（status=0）避免与并发批次重复结算同一笔
        int changed = shareMapper.update(null, new LambdaUpdateWrapper<PluginRevenueShare>()
                .set(PluginRevenueShare::getStatus, PluginRevenueShare.STATUS_SETTLED)
                .set(PluginRevenueShare::getBatchNo, batchNo)
                .set(PluginRevenueShare::getSettleTime, LocalDateTime.now())
                .eq(PluginRevenueShare::getDeveloperId, developerId)
                .eq(PluginRevenueShare::getStatus, PluginRevenueShare.STATUS_PENDING)
                .in(PluginRevenueShare::getId, shares.stream().map(PluginRevenueShare::getId).toList()));
        if (changed != shares.size()) {
            log.warn("结算批次流水数不一致（可能被并发结算）: batch={} expect={} actual={}",
                    batchNo, shares.size(), changed);
        }
        log.info("结算批次已生成: batch={} developer={} shares={} developerAmount={}",
                batchNo, developerId, shares.size(), batch.getDeveloperAmount());
        return batch;
    }

    /** 打款完成：回填凭证与时间 */
    @Transactional(rollbackFor = Exception.class)
    public PluginSettlementBatch markPaid(Long batchId, String voucherUrl) {
        PluginSettlementBatch batch = batchMapper.selectById(batchId);
        if (batch == null) {
            throw new IllegalArgumentException("结算批次不存在: " + batchId);
        }
        if (!Integer.valueOf(PluginSettlementBatch.STATUS_PENDING).equals(batch.getStatus())) {
            throw new IllegalStateException("只有待打款的批次可以标记打款，当前状态: " + batch.getStatus());
        }
        batch.setStatus(PluginSettlementBatch.STATUS_PAID);
        batch.setVoucherUrl(voucherUrl == null ? "" : voucherUrl.trim());
        batch.setPayTime(LocalDateTime.now());
        batchMapper.updateById(batch);
        log.info("结算批次已打款: batch={} developer={} amount={}",
                batch.getBatchNo(), batch.getDeveloperId(), batch.getDeveloperAmount());
        return batch;
    }

    /** 驳回单笔流水（退款 / 风控）：已结算的流水不改，避免与已出账批次对不上 */
    @Transactional(rollbackFor = Exception.class)
    public PluginRevenueShare rejectShare(Long shareId, String remark) {
        PluginRevenueShare share = shareMapper.selectById(shareId);
        if (share == null) {
            throw new IllegalArgumentException("分账流水不存在: " + shareId);
        }
        if (!Integer.valueOf(PluginRevenueShare.STATUS_PENDING).equals(share.getStatus())) {
            throw new IllegalStateException("只有待结算的流水可以驳回；已出账请走冲抵流程");
        }
        share.setStatus(PluginRevenueShare.STATUS_REJECTED);
        share.setRemark(remark == null ? "" : remark.trim());
        share.setSettleTime(LocalDateTime.now());
        shareMapper.updateById(share);
        log.warn("分账流水已驳回: id={} order={} reason={}", shareId, share.getOrderNo(), remark);
        return share;
    }

    /** 批次列表 */
    public List<PluginSettlementBatch> listBatches(Long developerId, Integer status) {
        LambdaQueryWrapper<PluginSettlementBatch> wrapper = new LambdaQueryWrapper<PluginSettlementBatch>()
                .orderByDesc(PluginSettlementBatch::getId);
        if (developerId != null) {
            wrapper.eq(PluginSettlementBatch::getDeveloperId, developerId);
        }
        if (status != null) {
            wrapper.eq(PluginSettlementBatch::getStatus, status);
        }
        return batchMapper.selectList(wrapper);
    }

    private String nextBatchNo() {
        return "ST" + LocalDateTime.now().format(BATCH_TIME)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private Map<String, Object> toShareView(PluginRevenueShare row) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", row.getId());
        view.put("orderNo", row.getOrderNo());
        view.put("pluginCode", row.getPluginCode());
        view.put("developerId", row.getDeveloperId());
        view.put("buyerId", row.getBuyerId());
        view.put("grossAmount", row.getGrossAmount());
        view.put("currency", row.getCurrency());
        view.put("commissionRate", row.getCommissionRate());
        view.put("platformAmount", row.getPlatformAmount());
        view.put("developerAmount", row.getDeveloperAmount());
        view.put("status", row.getStatus());
        view.put("batchNo", row.getBatchNo());
        view.put("settleTime", row.getSettleTime());
        view.put("remark", row.getRemark());
        view.put("createTime", row.getCreateTime());
        return view;
    }
}
