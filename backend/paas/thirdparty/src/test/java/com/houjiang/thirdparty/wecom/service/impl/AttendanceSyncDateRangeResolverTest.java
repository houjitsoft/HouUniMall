package com.houjiang.thirdparty.wecom.service.impl;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttendanceSyncDateRangeResolverTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final AttendanceSyncDateRangeResolver resolver = new AttendanceSyncDateRangeResolver(
            Clock.fixed(Instant.parse("2026-09-10T02:00:00Z"), ZONE));

    @Test
    void currentMonthEndsAtYesterday() {
        AttendanceSyncDateRange range = resolver.resolve("2026-09");

        assertTrue(range.available());
        assertEquals(LocalDate.of(2026, 9, 1), range.startDate());
        assertEquals(LocalDate.of(2026, 9, 9), range.endDate());
        assertEquals(1788969599L, range.endEpochSecond());
    }

    @Test
    void historicalMonthEndsAtMonthEnd() {
        AttendanceSyncDateRange range = resolver.resolve("2026-08");

        assertTrue(range.available());
        assertEquals(LocalDate.of(2026, 8, 1), range.startDate());
        assertEquals(LocalDate.of(2026, 8, 31), range.endDate());
    }

    @Test
    void futureMonthIsUnavailable() {
        AttendanceSyncDateRange range = resolver.resolve("2026-10");

        assertFalse(range.available());
        assertEquals("未来月份暂无可同步考勤", range.unavailableReason());
    }

    @Test
    void firstDayOfMonthHasNoCompletedDateInCurrentMonth() {
        AttendanceSyncDateRangeResolver firstDayResolver = new AttendanceSyncDateRangeResolver(
                Clock.fixed(Instant.parse("2026-10-01T00:00:00Z"), ZONE));

        AttendanceSyncDateRange range = firstDayResolver.resolve("2026-10");

        assertFalse(range.available());
        assertEquals("当月尚无已完结日期，暂无可同步考勤", range.unavailableReason());
    }
}
