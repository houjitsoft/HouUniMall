package com.houjiang.thirdparty.wecom.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * 统一企业微信考勤同步日期边界。
 * @author Amos
 */
@Component
public class AttendanceSyncDateRangeResolver {

    static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final Clock clock;

    public AttendanceSyncDateRangeResolver() {
        this(Clock.system(ZONE));
    }

    AttendanceSyncDateRangeResolver(Clock clock) {
        this.clock = clock;
    }

    AttendanceSyncDateRange resolve(String month) {
        LocalDate today = LocalDate.now(clock.withZone(ZONE));
        YearMonth currentMonth = YearMonth.from(today);
        YearMonth targetMonth = parseMonth(month, currentMonth);

        if (targetMonth.isAfter(currentMonth)) {
            return AttendanceSyncDateRange.unavailable(targetMonth, "未来月份暂无可同步考勤");
        }

        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.equals(currentMonth)
                ? today.minusDays(1)
                : targetMonth.atEndOfMonth();
        if (endDate.isBefore(startDate)) {
            return AttendanceSyncDateRange.unavailable(targetMonth,
                    "当月尚无已完结日期，暂无可同步考勤");
        }
        return AttendanceSyncDateRange.available(targetMonth, startDate, endDate);
    }

    private YearMonth parseMonth(String month, YearMonth fallback) {
        if (StringUtils.hasText(month)) {
            try {
                return YearMonth.parse(month.trim());
            } catch (Exception ignored) {
                // 保持旧行为：非法月份按当前月处理。
            }
        }
        return fallback;
    }
}

/** 企微考勤实际请求日期区间。 */
record AttendanceSyncDateRange(
        YearMonth month,
        LocalDate startDate,
        LocalDate endDate,
        String unavailableReason) {

    static AttendanceSyncDateRange available(YearMonth month, LocalDate startDate, LocalDate endDate) {
        return new AttendanceSyncDateRange(month, startDate, endDate, "");
    }

    static AttendanceSyncDateRange unavailable(YearMonth month, String reason) {
        return new AttendanceSyncDateRange(month, null, null, reason);
    }

    boolean available() {
        return startDate != null && endDate != null;
    }

    long startEpochSecond() {
        if (!available()) {
            throw new IllegalStateException("Attendance sync range is unavailable: " + unavailableReason);
        }
        return startDate.atStartOfDay(AttendanceSyncDateRangeResolver.ZONE).toEpochSecond();
    }

    long endEpochSecond() {
        if (!available()) {
            throw new IllegalStateException("Attendance sync range is unavailable: " + unavailableReason);
        }
        return endDate.atTime(23, 59, 59)
                .atZone(AttendanceSyncDateRangeResolver.ZONE)
                .toEpochSecond();
    }
}
