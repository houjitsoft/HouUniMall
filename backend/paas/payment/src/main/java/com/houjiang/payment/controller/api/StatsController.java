package com.houjiang.payment.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PayConsumeTradeEntity;
import com.houjiang.payment.entity.PayMerchantEntity;
import com.houjiang.payment.entity.PayRefundOrderEntity;
import com.houjiang.payment.mapper.PayConsumeTradeMapper;
import com.houjiang.payment.mapper.PayMerchantMapper;
import com.houjiang.payment.mapper.PayRefundOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Date;

/**
 * 支付统计 Controller（实时聚合查询）
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>/overview - 概览数据（今日收入、本月收入、今日订单、今日退款、成功率）</li>
 *   <li>/trend - 每日趋势（近 N 天的收入和订单量）</li>
 *   <li>/channelDist - 渠道分布（饼图数据）</li>
 *   <li>/merchantRank - 商户 TOP N 交易排行（柱状图）</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/stats")
@RequiredArgsConstructor
public class StatsController {

    private final PayConsumeTradeMapper tradeMapper;
    private final PayMerchantMapper merchantMapper;
    private final PayRefundOrderMapper refundOrderMapper;

    /** 订单状态：支付成功 */
    private static final int STATE_PAID = 2;
    /** 订单状态：已关闭/已取消 */
    private static final int STATE_CLOSED = 4;
    /** 退款状态：处理中 */
    private static final String REFUND_PROCESSING = "PROCESSING";
    /** 退款状态：成功 */
    private static final String REFUND_SUCCESS = "SUCCESS";

    /**
     * 概览数据
     * 返回：todayIncome, monthIncome, todayOrders, successRate, todayRefund, todayRefundCount
     */
    @PostMapping("/overview")
    public ResponseResult<Map<String, Object>> overview(@RequestBody(required = false) Map<String, Object> req) {
        try {
            LocalDate today = LocalDate.now();
            int year = today.getYear();
            int month = today.getMonthValue();
            int day = today.getDayOfMonth();

            // 今日全部订单
            LambdaQueryWrapper<PayConsumeTradeEntity> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.eq(PayConsumeTradeEntity::getTradeYear, year)
                    .eq(PayConsumeTradeEntity::getTradeMonth, month)
                    .eq(PayConsumeTradeEntity::getTradeDay, day);
            List<PayConsumeTradeEntity> todayOrders = tradeMapper.selectList(todayWrapper);

            // 今日成功订单
            long todaySuccessCount = todayOrders.stream()
                    .filter(o -> o.getOrderStateId() != null && o.getOrderStateId() == STATE_PAID)
                    .count();
            BigDecimal todayIncome = todayOrders.stream()
                    .filter(o -> o.getOrderStateId() != null && o.getOrderStateId() == STATE_PAID)
                    .map(o -> o.getTradePaymentAmount() != null ? o.getTradePaymentAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 成功率
            double successRate = todayOrders.isEmpty() ? 100.0
                    : Math.round(todaySuccessCount * 10000.0 / todayOrders.size()) / 100.0;

            // 本月收入
            LambdaQueryWrapper<PayConsumeTradeEntity> monthWrapper = new LambdaQueryWrapper<>();
            monthWrapper.eq(PayConsumeTradeEntity::getTradeYear, year)
                    .eq(PayConsumeTradeEntity::getTradeMonth, month)
                    .eq(PayConsumeTradeEntity::getOrderStateId, STATE_PAID);
            List<PayConsumeTradeEntity> monthOrders = tradeMapper.selectList(monthWrapper);
            BigDecimal monthIncome = monthOrders.stream()
                    .map(o -> o.getTradePaymentAmount() != null ? o.getTradePaymentAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 今日退款（成功+处理中，金额取退款单实退额）
            Date todayStartDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date tomorrowStartDate = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            LambdaQueryWrapper<PayRefundOrderEntity> refundWrapper = new LambdaQueryWrapper<>();
            refundWrapper.ge(PayRefundOrderEntity::getCreateTime, todayStartDate)
                    .lt(PayRefundOrderEntity::getCreateTime, tomorrowStartDate);
            List<PayRefundOrderEntity> todayRefunds = refundOrderMapper.selectList(refundWrapper);
            BigDecimal todayRefund = todayRefunds.stream()
                    .filter(r -> REFUND_SUCCESS.equals(r.getStatus()) || REFUND_PROCESSING.equals(r.getStatus()))
                    .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long todayRefundCount = todayRefunds.stream()
                    .filter(r -> REFUND_SUCCESS.equals(r.getStatus()) || REFUND_PROCESSING.equals(r.getStatus()))
                    .count();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("todayIncome", todayIncome);
            result.put("monthIncome", monthIncome);
            result.put("todayOrders", todayOrders.size());
            result.put("todaySuccessOrders", todaySuccessCount);
            result.put("successRate", successRate);
            result.put("todayRefund", todayRefund);
            result.put("todayRefundCount", todayRefundCount);
            return ResponseResult.success(result);
        } catch (Exception ex) {
            log.error("[Stats] overview 失败", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /**
     * 每日趋势数据
     * 参数：days（默认7天）
     * 返回：List[{date, income, orders, successOrders}]
     */
    @PostMapping("/trend")
    public ResponseResult<List<Map<String, Object>>> trend(@RequestBody(required = false) Map<String, Object> req) {
        try {
            int days = 7;
            if (req != null && req.get("days") != null) {
                days = Integer.parseInt(req.get("days").toString());
            }
            if (days > 90) days = 90;

            LocalDate today = LocalDate.now();
            List<Map<String, Object>> trendList = new ArrayList<>();

            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                LambdaQueryWrapper<PayConsumeTradeEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PayConsumeTradeEntity::getTradeYear, date.getYear())
                        .eq(PayConsumeTradeEntity::getTradeMonth, date.getMonthValue())
                        .eq(PayConsumeTradeEntity::getTradeDay, date.getDayOfMonth());
                List<PayConsumeTradeEntity> dayOrders = tradeMapper.selectList(wrapper);

                BigDecimal income = dayOrders.stream()
                        .filter(o -> o.getOrderStateId() != null && o.getOrderStateId() == STATE_PAID)
                        .map(o -> o.getTradePaymentAmount() != null ? o.getTradePaymentAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                long successCount = dayOrders.stream()
                        .filter(o -> o.getOrderStateId() != null && o.getOrderStateId() == STATE_PAID)
                        .count();

                Map<String, Object> dayData = new LinkedHashMap<>();
                dayData.put("date", date.toString());
                dayData.put("income", income);
                dayData.put("orders", dayOrders.size());
                dayData.put("successOrders", successCount);
                trendList.add(dayData);
            }
            return ResponseResult.success(trendList);
        } catch (Exception ex) {
            log.error("[Stats] trend 失败", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /**
     * 渠道分布（饼图数据）
     * 参数：period（today/month/all，默认month）
     * 返回：List[{channel, channelName, amount, count}]
     */
    @PostMapping("/channelDist")
    public ResponseResult<List<Map<String, Object>>> channelDist(@RequestBody(required = false) Map<String, Object> req) {
        try {
            String period = "month";
            if (req != null && req.get("period") != null) {
                period = req.get("period").toString();
            }

            LambdaQueryWrapper<PayConsumeTradeEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PayConsumeTradeEntity::getOrderStateId, STATE_PAID);
            LocalDate today = LocalDate.now();
            if ("today".equals(period)) {
                wrapper.eq(PayConsumeTradeEntity::getTradeYear, today.getYear())
                        .eq(PayConsumeTradeEntity::getTradeMonth, today.getMonthValue())
                        .eq(PayConsumeTradeEntity::getTradeDay, today.getDayOfMonth());
            } else if ("month".equals(period)) {
                wrapper.eq(PayConsumeTradeEntity::getTradeYear, today.getYear())
                        .eq(PayConsumeTradeEntity::getTradeMonth, today.getMonthValue());
            }
            // "all" 不加日期限制

            List<PayConsumeTradeEntity> orders = tradeMapper.selectList(wrapper);

            // 按 paymentChannelId 分组
            Map<Integer, List<PayConsumeTradeEntity>> grouped = orders.stream()
                    .filter(o -> o.getPaymentChannelId() != null)
                    .collect(Collectors.groupingBy(PayConsumeTradeEntity::getPaymentChannelId));

            List<Map<String, Object>> distList = new ArrayList<>();
            for (Map.Entry<Integer, List<PayConsumeTradeEntity>> entry : grouped.entrySet()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("channel", entry.getKey());
                item.put("channelName", getChannelName(entry.getKey()));
                item.put("count", entry.getValue().size());
                BigDecimal amount = entry.getValue().stream()
                        .map(o -> o.getTradePaymentAmount() != null ? o.getTradePaymentAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                item.put("amount", amount);
                distList.add(item);
            }
            // 按金额降序
            distList.sort((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")));
            return ResponseResult.success(distList);
        } catch (Exception ex) {
            log.error("[Stats] channelDist 失败", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /**
     * 商户 TOP N 交易排行（柱状图数据）
     * 参数：limit（默认 10，上限 50）、period（today/month/all，默认 month）
     * 返回：List[{merchantId, merchantName, amount, count}]，按金额降序
     */
    @PostMapping("/merchantRank")
    public ResponseResult<List<Map<String, Object>>> merchantRank(@RequestBody(required = false) Map<String, Object> req) {
        try {
            int limit = 10;
            String period = "month";
            if (req != null) {
                if (req.get("limit") != null) {
                    limit = Integer.parseInt(req.get("limit").toString());
                }
                if (req.get("period") != null) {
                    period = req.get("period").toString();
                }
            }
            if (limit > 50) limit = 50;
            if (limit <= 0) limit = 10;

            LambdaQueryWrapper<PayConsumeTradeEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PayConsumeTradeEntity::getOrderStateId, STATE_PAID);
            LocalDate today = LocalDate.now();
            if ("today".equals(period)) {
                wrapper.eq(PayConsumeTradeEntity::getTradeYear, today.getYear())
                        .eq(PayConsumeTradeEntity::getTradeMonth, today.getMonthValue())
                        .eq(PayConsumeTradeEntity::getTradeDay, today.getDayOfMonth());
            } else if ("month".equals(period)) {
                wrapper.eq(PayConsumeTradeEntity::getTradeYear, today.getYear())
                        .eq(PayConsumeTradeEntity::getTradeMonth, today.getMonthValue());
            }
            // "all" 不限定日期

            List<PayConsumeTradeEntity> orders = tradeMapper.selectList(wrapper);

            // 按 sellerId（商户标识）分组聚合
            Map<String, List<PayConsumeTradeEntity>> grouped = orders.stream()
                    .filter(o -> o.getSellerId() != null && !o.getSellerId().isEmpty())
                    .collect(Collectors.groupingBy(PayConsumeTradeEntity::getSellerId));

            List<Map<String, Object>> rankList = new ArrayList<>();
            for (Map.Entry<String, List<PayConsumeTradeEntity>> entry : grouped.entrySet()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("merchantId", entry.getKey());
                item.put("merchantName", resolveMerchantName(entry.getKey()));
                item.put("count", entry.getValue().size());
                BigDecimal amount = entry.getValue().stream()
                        .map(o -> o.getTradePaymentAmount() != null ? o.getTradePaymentAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                item.put("amount", amount);
                rankList.add(item);
            }
            // 金额降序 + 取 TOP N
            rankList.sort((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")));
            if (rankList.size() > limit) {
                rankList = rankList.subList(0, limit);
            }
            return ResponseResult.success(rankList);
        } catch (Exception ex) {
            log.error("[Stats] merchantRank 失败", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /**
     * 通过 sellerId 反查商户简称/全称，找不到则返回 sellerId 本身
     * @param sellerId 卖家ID（兼容数字主键 / mchNo）
     * @return 商户展示名
     */
    private String resolveMerchantName(String sellerId) {
        if (sellerId == null || sellerId.isEmpty()) return "未知商户";
        try {
            // 1) 数字主键
            if (sellerId.matches("^\\d+$")) {
                PayMerchantEntity m = merchantMapper.selectById(Long.parseLong(sellerId));
                if (m != null) {
                    return pickMerchantDisplayName(m, sellerId);
                }
            }
            // 2) mchNo 查询
            LambdaQueryWrapper<PayMerchantEntity> w = new LambdaQueryWrapper<>();
            w.eq(PayMerchantEntity::getMchNo, sellerId).last("limit 1");
            PayMerchantEntity m = merchantMapper.selectOne(w);
            if (m != null) {
                return pickMerchantDisplayName(m, sellerId);
            }
        } catch (Exception ignored) {
        }
        return sellerId;
    }

    /**
     * 选择商户展示名：优先简称，其次全称，最后返回 sellerId
     */
    private String pickMerchantDisplayName(PayMerchantEntity m, String sellerId) {
        if (m.getMchShortName() != null && !m.getMchShortName().isEmpty()) return m.getMchShortName();
        if (m.getCompanyName() != null && !m.getCompanyName().isEmpty()) return m.getCompanyName();
        return sellerId;
    }

    /**
     * 获取渠道名称
     * @param channelId 渠道ID
     * @return 渠道名称
     */
    private String getChannelName(Integer channelId) {
        if (channelId == null) return "未知";
        switch (channelId) {
            case 1: return "微信支付";
            case 2: return "支付宝";
            case 3: return "银联支付";
            case 4: return "云闪付";
            case 5: return "余额支付";
            case 6: return "银行卡快捷";
            default: return "渠道" + channelId;
        }
    }
}
