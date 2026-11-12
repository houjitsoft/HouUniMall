package com.houjiang.userms.service.impl;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.BehaviorAnalyticsQueryDTO;
import com.houjiang.userms.dto.BehaviorTrackDTO;
import com.houjiang.userms.mapper.BehaviorAnalyticsMapper;
import com.houjiang.userms.service.BehaviorAnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户行为与留存分析实现
 *
 * <p>口径（全部可复核，全部来自真实库表，不引入模拟数据）：</p>
 * <ul>
 *   <li>行为：login / view / search / cart / order / share 六类频次，按日趋势 + 汇总表；
 *       未接入采集的类型标记 not_collected，不把「没采」当成「0 次」。</li>
 *   <li>活跃：DAU 当日去重；WAU / MAU 为截至当日往前 7 / 30 天滚动窗口去重。</li>
 *   <li>留存：第 N 日留存 = 基准日 cohort 中在第 N 天当日仍有活跃事件的会员占比；
 *       同时给「新增注册用户」与「活跃用户」两套 cohort，观察期未满记 pending，不按 0 计算。</li>
 *   <li>转化：注册到首购漏斗以全量有效会员为基准，各级行为按所选时间范围统计；
 *       未采集维度单独标记，各级独立取数、不强行逐级收窄。</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
@Service
public class BehaviorAnalyticsServiceImpl implements BehaviorAnalyticsService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String> EVENT_TYPES = Arrays.asList("login", "view", "search", "cart", "order", "share");
    private static final Map<String, String> EVENT_LABELS = new LinkedHashMap<>();
    private static final int[] RETENTION_OFFSETS = {1, 7, 30};

    static {
        EVENT_LABELS.put("login", "登录");
        EVENT_LABELS.put("view", "浏览");
        EVENT_LABELS.put("search", "搜索");
        EVENT_LABELS.put("cart", "加购");
        EVENT_LABELS.put("order", "下单");
        EVENT_LABELS.put("share", "分享");
    }

    @Autowired
    private BehaviorAnalyticsMapper behaviorAnalyticsMapper;

    @Override
    public ResponseResult queryAnalytics(BehaviorAnalyticsQueryDTO dto) {
        BehaviorAnalyticsQueryDTO query = dto == null ? new BehaviorAnalyticsQueryDTO() : dto;
        LocalDate today = LocalDate.now();
        LocalDate end = parseDate(query.getEndDate(), today);
        LocalDate start = parseDate(query.getStartDate(), end.minusDays(29));
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        if (ChronoUnit.DAYS.between(start, end) > 365) {
            start = end.minusDays(365);
        }

        // 事件回溯 30 天用于 WAU / MAU 滚动窗口；向前最多看到今天，用于留存观察期判定
        LocalDate eventEnd = end.plusDays(31).isAfter(today) ? today : end.plusDays(31);
        String eventFrom = start.minusDays(30).atStartOfDay().format(TS);
        String eventTo = eventEnd.plusDays(1).atStartOfDay().format(TS);
        String rangeFrom = start.atStartOfDay().format(TS);
        String rangeTo = end.plusDays(1).atStartOfDay().format(TS);

        List<Map<String, Object>> rawEvents = safeList(behaviorAnalyticsMapper.selectEvents(eventFrom, eventTo));
        List<Map<String, Object>> registrations = safeList(behaviorAnalyticsMapper.selectRegistrations(rangeFrom, rangeTo));
        // 首购判定需要会员的历史首单，故订单取全量历史
        List<Map<String, Object>> orders = safeList(behaviorAnalyticsMapper.selectOrders("1970-01-01 00:00:00", eventTo));
        List<Map<String, Object>> allMembers = safeList(behaviorAnalyticsMapper.selectAllMembers());
        Map<String, Long> coverage = loadCoverage();

        // ---------------- 事件索引 ----------------
        Map<LocalDate, Set<String>> activeByDay = new HashMap<>();
        Map<LocalDate, Map<String, Long>> countByDay = new HashMap<>();
        Map<String, Long> countByType = new HashMap<>();
        Map<String, Set<String>> usersByType = new HashMap<>();
        Set<String> rangeActiveUsers = new HashSet<>();
        Set<String> interestUsers = new HashSet<>();
        Set<String> orderEventUsers = new HashSet<>();
        long rangeEventTotal = 0L;

        for (Map<String, Object> row : rawEvents) {
            String userId = str(row.get("userId"));
            String type = str(row.get("eventType"));
            LocalDateTime time = parseTs(row.get("eventTime"));
            if (!StringUtils.hasText(userId) || !EVENT_TYPES.contains(type) || time == null) {
                continue;
            }
            LocalDate day = time.toLocalDate();
            activeByDay.computeIfAbsent(day, k -> new HashSet<>()).add(userId);
            countByDay.computeIfAbsent(day, k -> new HashMap<>()).merge(type, 1L, Long::sum);

            boolean inRange = !day.isBefore(start) && !day.isAfter(end);
            if (!inRange) {
                continue;
            }
            rangeEventTotal++;
            countByType.merge(type, 1L, Long::sum);
            usersByType.computeIfAbsent(type, k -> new HashSet<>()).add(userId);
            rangeActiveUsers.add(userId);
            if ("view".equals(type) || "search".equals(type) || "cart".equals(type)) {
                interestUsers.add(userId);
            }
            if ("order".equals(type)) {
                orderEventUsers.add(userId);
            }
        }

        // ---------------- 数据完整度 ----------------
        Map<String, Object> sources = new LinkedHashMap<>();
        sources.put("loginRows", coverage.getOrDefault("login", 0L));
        sources.put("footprintRows", coverage.getOrDefault("view", 0L));
        sources.put("orderRows", coverage.getOrDefault("order", 0L));
        long trackRows = 0L;
        for (String t : EVENT_TYPES) {
            trackRows += coverage.getOrDefault("event_" + t, 0L);
        }
        sources.put("trackRows", trackRows);

        List<String> collectedTypes = new ArrayList<>();
        List<String> missingTypes = new ArrayList<>();
        for (String t : EVENT_TYPES) {
            long hits = eventSourceRows(coverage, t);
            if (hits > 0) {
                collectedTypes.add(t);
            } else {
                missingTypes.add(t);
            }
        }
        Map<String, Object> dataQuality = new LinkedHashMap<>();
        dataQuality.put("generatedAt", LocalDateTime.now().format(TS));
        dataQuality.put("eventRows", rawEvents.size());
        dataQuality.put("rangeEventRows", rangeEventTotal);
        dataQuality.put("sources", sources);
        dataQuality.put("collectedTypes", collectedTypes);
        dataQuality.put("missingTypes", missingTypes);
        dataQuality.put("note", "登录/浏览/下单来自既有真实表，搜索/加购/分享需接入统一埋点；未采集维度不补 0、不编数。");

        // ---------------- 行为维度 ----------------
        List<Map<String, Object>> behaviorSummary = new ArrayList<>();
        for (String t : EVENT_TYPES) {
            long count = countByType.getOrDefault(t, 0L);
            int users = usersByType.getOrDefault(t, Collections.emptySet()).size();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", t);
            item.put("label", EVENT_LABELS.get(t));
            item.put("count", count);
            item.put("users", users);
            item.put("avgPerUser", users == 0 ? null : round2((double) count / users));
            item.put("share", rangeEventTotal == 0 ? null : round2(count * 100.0 / rangeEventTotal));
            item.put("collected", eventSourceRows(coverage, t) > 0);
            behaviorSummary.add(item);
        }
        List<Map<String, Object>> behaviorTrend = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            Map<String, Long> byType = countByDay.getOrDefault(d, Collections.emptyMap());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", d.toString());
            long total = 0L;
            for (String t : EVENT_TYPES) {
                long c = byType.getOrDefault(t, 0L);
                item.put(t, c);
                total += c;
            }
            item.put("total", total);
            behaviorTrend.add(item);
        }
        Map<String, Object> behavior = new LinkedHashMap<>();
        behavior.put("summary", behaviorSummary);
        behavior.put("trend", behaviorTrend);
        behavior.put("total", rangeEventTotal);

        // ---------------- 活跃维度 ----------------
        List<Map<String, Object>> activeTrend = new ArrayList<>();
        long sumDau = 0L;
        long peakDau = 0L;
        long latestDau = 0L;
        long latestWau = 0L;
        long latestMau = 0L;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            Set<String> dau = activeByDay.getOrDefault(d, Collections.emptySet());
            Set<String> wau = new HashSet<>();
            Set<String> mau = new HashSet<>();
            for (int i = 0; i < 30; i++) {
                Set<String> dayUsers = activeByDay.getOrDefault(d.minusDays(i), Collections.emptySet());
                if (i < 7) {
                    wau.addAll(dayUsers);
                }
                mau.addAll(dayUsers);
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", d.toString());
            item.put("dau", dau.size());
            item.put("wau", wau.size());
            item.put("mau", mau.size());
            activeTrend.add(item);
            sumDau += dau.size();
            peakDau = Math.max(peakDau, dau.size());
            latestDau = dau.size();
            latestWau = wau.size();
            latestMau = mau.size();
        }
        int days = activeTrend.size();
        Map<String, Object> activeSummary = new LinkedHashMap<>();
        activeSummary.put("latestDau", latestDau);
        activeSummary.put("avgDau", days == 0 ? null : round2((double) sumDau / days));
        activeSummary.put("peakDau", peakDau);
        activeSummary.put("latestWau", latestWau);
        activeSummary.put("latestMau", latestMau);
        activeSummary.put("rangeActiveUsers", rangeActiveUsers.size());
        Map<String, Object> active = new LinkedHashMap<>();
        active.put("summary", activeSummary);
        active.put("trend", activeTrend);
        active.put("rule", "DAU 当日去重；WAU / MAU 为截至当日往前 7 / 30 天滚动窗口去重活跃会员数。");

        // ---------------- 留存维度 ----------------
        Map<LocalDate, Set<String>> activeCohorts = new LinkedHashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            Set<String> users = activeByDay.get(d);
            if (users != null && !users.isEmpty()) {
                activeCohorts.put(d, users);
            }
        }
        Map<LocalDate, Set<String>> registerCohorts = new LinkedHashMap<>();
        for (Map<String, Object> row : registrations) {
            LocalDate day = parseDay(row.get("registerDay"));
            String userId = str(row.get("userId"));
            if (day == null || !StringUtils.hasText(userId)) {
                continue;
            }
            registerCohorts.computeIfAbsent(day, k -> new HashSet<>()).add(userId);
        }
        Map<String, Object> retention = new LinkedHashMap<>();
        retention.put("byActive", buildRetention(activeCohorts, activeByDay, today));
        retention.put("byRegister", buildRetention(registerCohorts, activeByDay, today));
        retention.put("newRegistered", registrations.size());
        retention.put("rule", "第 N 日留存＝基准日 cohort 中在第 N 天当日仍有活跃事件的会员占比；观察期未满记「观察中」，不计入分母。");

        // ---------------- 转化漏斗 ----------------
        Set<String> memberIds = new HashSet<>();
        for (Map<String, Object> row : allMembers) {
            String userId = str(row.get("userId"));
            if (StringUtils.hasText(userId)) {
                memberIds.add(userId);
            }
        }
        Map<String, LocalDate> firstValidOrderDay = new HashMap<>();
        Set<String> rangeOrderUsers = new HashSet<>();
        Set<String> rangePaidUsers = new HashSet<>();
        long rangeOrderRows = 0L;
        long rangePaidRows = 0L;
        long totalPaidDays = 0L;
        long totalPaidUsers = 0L;
        for (Map<String, Object> row : orders) {
            String userId = str(row.get("userId"));
            LocalDate day = parseDay(row.get("orderDay"));
            int status = (int) num(row.get("orderStatus"));
            if (!StringUtils.hasText(userId) || day == null) {
                continue;
            }
            boolean valid = status != 1 && status != 7;
            if (valid && !firstValidOrderDay.containsKey(userId)) {
                // 订单已按时间升序，首次写入即该会员首笔有效成交
                firstValidOrderDay.put(userId, day);
            }
            boolean inRange = !day.isBefore(start) && !day.isAfter(end);
            if (!inRange) {
                continue;
            }
            rangeOrderRows++;
            rangeOrderUsers.add(userId);
            if (valid) {
                rangePaidRows++;
                rangePaidUsers.add(userId);
            }
        }
        Set<String> firstPurchaseUsers = new HashSet<>();
        for (Map.Entry<String, LocalDate> e : firstValidOrderDay.entrySet()) {
            LocalDate day = e.getValue();
            if (!day.isBefore(start) && !day.isAfter(end)) {
                firstPurchaseUsers.add(e.getKey());
            }
        }
        for (Map.Entry<String, LocalDate> e : firstValidOrderDay.entrySet()) {
            Map<String, Object> member = findMember(allMembers, e.getKey());
            LocalDate regDay = member == null ? null : parseDay(member.get("registerDay"));
            if (regDay != null) {
                totalPaidDays += ChronoUnit.DAYS.between(regDay, e.getValue());
                totalPaidUsers++;
            }
        }
        Set<String> activated = intersect(memberIds, rangeActiveUsers);
        Set<String> interested = intersect(memberIds, interestUsers);
        Set<String> ordered = new HashSet<>(intersect(memberIds, rangeOrderUsers));
        ordered.addAll(intersect(memberIds, orderEventUsers));
        Set<String> paid = intersect(memberIds, rangePaidUsers);
        Set<String> firstPurchased = intersect(memberIds, firstPurchaseUsers);

        boolean interestCollected = eventSourceRows(coverage, "view") > 0
                || eventSourceRows(coverage, "search") > 0
                || eventSourceRows(coverage, "cart") > 0;
        List<Map<String, Object>> levels = new ArrayList<>();
        long base = memberIds.size();
        long prev = base;
        levels.add(funnelLevel("register", "注册会员", base, base, base, true));
        prev = base;
        levels.add(funnelLevel("activated", "产生行为（激活）", activated.size(), prev, base, !rangeActiveUsers.isEmpty()));
        prev = activated.size();
        levels.add(funnelLevel("interested", "浏览/搜索/加购", interested.size(), prev, base, interestCollected));
        prev = interested.size();
        levels.add(funnelLevel("ordered", "提交订单", ordered.size(), prev, base, true));
        prev = ordered.size();
        levels.add(funnelLevel("paid", "有效成交", paid.size(), prev, base, true));
        prev = paid.size();
        levels.add(funnelLevel("firstPurchase", "完成首购", firstPurchased.size(), prev, base, true));

        Map<String, Object> funnel = new LinkedHashMap<>();
        funnel.put("levels", levels);
        funnel.put("memberTotal", base);
        funnel.put("newRegistered", registrations.size());
        funnel.put("rangeOrderRows", rangeOrderRows);
        funnel.put("rangePaidRows", rangePaidRows);
        funnel.put("avgDaysToFirstOrder", totalPaidUsers == 0 ? null : round2((double) totalPaidDays / totalPaidUsers));
        funnel.put("rule", "漏斗基准＝全量有效注册会员；各级行为按所选时间范围统计，相互独立取数、不强制逐级收窄。未采集维度标记 collected=false，显示「未采集」而非 0。");

        // ---------------- 汇总返回 ----------------
        Map<String, Object> range = new LinkedHashMap<>();
        range.put("start", start.toString());
        range.put("end", end.toString());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("range", range);
        data.put("dataQuality", dataQuality);
        data.put("behavior", behavior);
        data.put("active", active);
        data.put("retention", retention);
        data.put("funnel", funnel);
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult track(BehaviorTrackDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getUserId()) || !EVENT_TYPES.contains(dto.getEventType())) {
            return ResponseResult.fail("userId 不能为空，eventType 必须为 login/view/search/cart/order/share 之一");
        }
        if (!StringUtils.hasText(dto.getEventTime())) {
            dto.setEventTime(LocalDateTime.now().format(TS));
        } else {
            try {
                dto.setEventTime(LocalDateTime.parse(dto.getEventTime(), TS).format(TS));
            } catch (Exception e) {
                return ResponseResult.fail("eventTime 格式应为 yyyy-MM-dd HH:mm:ss");
            }
        }
        try {
            behaviorAnalyticsMapper.insertEvent(dto);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("userId", dto.getUserId());
            data.put("eventType", dto.getEventType());
            data.put("eventTime", dto.getEventTime());
            return ResponseResult.success(data);
        } catch (Exception e) {
            log.warn("行为埋点写入失败: {}", e.getMessage());
            return ResponseResult.fail("行为埋点写入失败");
        }
    }

    // ==================== 内部计算 ====================

    private Map<String, Object> buildRetention(Map<LocalDate, Set<String>> cohorts,
                                               Map<LocalDate, Set<String>> activeByDay,
                                               LocalDate today) {
        long[] eligible = new long[RETENTION_OFFSETS.length];
        long[] retained = new long[RETENTION_OFFSETS.length];
        List<Map<String, Object>> detail = new ArrayList<>();
        for (Map.Entry<LocalDate, Set<String>> e : cohorts.entrySet()) {
            LocalDate day = e.getKey();
            Set<String> users = e.getValue();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", day.toString());
            row.put("baseUsers", users.size());
            for (int i = 0; i < RETENTION_OFFSETS.length; i++) {
                int offset = RETENTION_OFFSETS[i];
                String key = "d" + offset;
                LocalDate target = day.plusDays(offset);
                if (target.isAfter(today)) {
                    row.put(key + "Users", null);
                    row.put(key + "Rate", null);
                    row.put(key + "Pending", true);
                    continue;
                }
                Set<String> active = activeByDay.getOrDefault(target, Collections.emptySet());
                int hit = 0;
                for (String u : users) {
                    if (active.contains(u)) {
                        hit++;
                    }
                }
                eligible[i] += users.size();
                retained[i] += hit;
                row.put(key + "Users", hit);
                row.put(key + "Rate", users.isEmpty() ? null : round2(hit * 100.0 / users.size()));
                row.put(key + "Pending", false);
            }
            detail.add(row);
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        for (int i = 0; i < RETENTION_OFFSETS.length; i++) {
            Map<String, Object> cell = new LinkedHashMap<>();
            cell.put("eligible", eligible[i]);
            cell.put("retained", retained[i]);
            cell.put("rate", eligible[i] == 0 ? null : round2(retained[i] * 100.0 / eligible[i]));
            summary.put("d" + RETENTION_OFFSETS[i], cell);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("cohortTotal", cohorts.values().stream().mapToInt(Set::size).sum());
        out.put("cohortDays", cohorts.size());
        out.put("summary", summary);
        out.put("cohorts", detail);
        return out;
    }

    private Map<String, Object> funnelLevel(String key, String name, long count, long prev, long base, boolean collected) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("name", name);
        item.put("count", count);
        item.put("collected", collected);
        item.put("stepRate", prev > 0 ? round2(count * 100.0 / prev) : null);
        item.put("totalRate", base > 0 ? round2(count * 100.0 / base) : null);
        return item;
    }

    private Map<String, Long> loadCoverage() {
        Map<String, Long> coverage = new HashMap<>();
        for (Map<String, Object> row : safeList(behaviorAnalyticsMapper.selectLegacyCounts())) {
            coverage.put(str(row.get("src")), num(row.get("cnt")));
        }
        for (Map<String, Object> row : safeList(behaviorAnalyticsMapper.selectEventTypeCounts())) {
            coverage.put(str(row.get("src")), num(row.get("cnt")));
        }
        return coverage;
    }

    /** 某类行为在「全库」是否有采集来源，用于区分「未采集」与「本区间 0 次」 */
    private long eventSourceRows(Map<String, Long> coverage, String type) {
        long legacy = 0L;
        if ("login".equals(type) || "view".equals(type) || "order".equals(type)) {
            legacy = coverage.getOrDefault(type, 0L);
        }
        return legacy + coverage.getOrDefault("event_" + type, 0L);
    }

    private Map<String, Object> findMember(List<Map<String, Object>> members, String userId) {
        for (Map<String, Object> m : members) {
            if (userId.equals(str(m.get("userId")))) {
                return m;
            }
        }
        return null;
    }

    private Set<String> intersect(Set<String> a, Set<String> b) {
        Set<String> out = new HashSet<>(a);
        out.retainAll(b);
        return out;
    }

    private List<Map<String, Object>> safeList(List<Map<String, Object>> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private String str(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private long num(Object v) {
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private LocalDate parseDate(String v, LocalDate fallback) {
        if (!StringUtils.hasText(v)) {
            return fallback;
        }
        try {
            return LocalDate.parse(v.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private LocalDate parseDay(Object v) {
        String s = str(v);
        if (!StringUtils.hasText(s)) {
            return null;
        }
        try {
            return LocalDate.parse(s.length() > 10 ? s.substring(0, 10) : s);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseTs(Object v) {
        String s = str(v);
        if (!StringUtils.hasText(s)) {
            return null;
        }
        try {
            return LocalDateTime.parse(s, TS);
        } catch (Exception e) {
            return null;
        }
    }
}
