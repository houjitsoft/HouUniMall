package com.houjiang.thirdparty.wecom.service.impl;

import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.thirdparty.dingtalk.dto.AttendancePlatformConfigDTO;
import com.houjiang.thirdparty.dingtalk.service.DingtalkIntegrationService;
import com.houjiang.thirdparty.dingtalk.support.AttendancePlatformConfigSupport;
import com.houjiang.thirdparty.mapper.oa.OaWecomStaffUserIdMapper;
import com.houjiang.thirdparty.service.ThirdPartyConfigService;
import com.houjiang.thirdparty.wecom.client.WecomApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WecomAttendanceSyncServiceImplTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    @Mock
    private WecomApiClient wecomApiClient;
    @Mock
    private ThirdPartyConfigService thirdPartyConfigService;
    @Mock
    private DingtalkIntegrationService dingtalkIntegrationService;
    @Mock
    private AttendancePlatformConfigSupport attendancePlatformConfigSupport;
    @Mock
    private OaWecomStaffUserIdMapper oaWecomStaffUserIdMapper;

    private WecomAttendanceSyncServiceImpl service;

    @BeforeEach
    void setUp() {
        AttendanceSyncDateRangeResolver resolver = new AttendanceSyncDateRangeResolver(
                Clock.fixed(Instant.parse("2026-09-10T02:00:00Z"), ZONE));
        service = new WecomAttendanceSyncServiceImpl(
                wecomApiClient,
                thirdPartyConfigService,
                dingtalkIntegrationService,
                attendancePlatformConfigSupport,
                oaWecomStaffUserIdMapper,
                resolver);

        when(attendancePlatformConfigSupport.resolveConfigOrgId("1")).thenReturn("1");
    }

    @Test
    void currentMonthUsesYesterdayAndFiltersTodayFromResponse() {
        configureEnabledWecom();
        when(wecomApiClient.getAccessToken("corp-id", "general-secret")).thenReturn("list-token");
        when(wecomApiClient.getAccessToken("corp-id", "checkin-secret")).thenReturn("checkin-token");
        long yesterday = LocalDate.of(2026, 9, 9).atTime(9, 0).atZone(ZONE).toEpochSecond();
        long today = LocalDate.of(2026, 9, 10).atTime(9, 0).atZone(ZONE).toEpochSecond();
        when(wecomApiClient.getCheckinData(eq("checkin-token"), anyLong(), anyLong(), anyList()))
                .thenReturn(new WecomApiClient.CheckinDataResult(
                        List.of(checkin("ChenHao", yesterday), checkin("ChenHao", today)), null, null));

        Map<String, Object> result = service.syncMonth("1", Map.of(
                "month", "2026-09",
                "userIds", List.of("ChenHao")));

        AttendanceSyncDateRange range = new AttendanceSyncDateRangeResolver(
                Clock.fixed(Instant.parse("2026-09-10T02:00:00Z"), ZONE)).resolve("2026-09");
        verify(wecomApiClient).getCheckinData(
                "checkin-token",
                range.startEpochSecond(),
                range.endEpochSecond(),
                List.of("ChenHao"));
        assertEquals("2026-09", result.get("syncMonth"));
        assertEquals("2026-09-01", result.get("syncStartDate"));
        assertEquals("2026-09-09", result.get("syncEndDate"));
        assertEquals(1, result.get("recordCount"));
        List<?> records = (List<?>) result.get("records");
        assertEquals(1, records.size());
        assertTrue(records.getFirst().toString().contains("2026-09-09"));
    }

    @Test
    void futureMonthSkipsBeforeRequestingTokensOrCheckins() {
        Map<String, Object> result = service.syncMonth("1", Map.of(
                "month", "2026-10",
                "userIds", List.of("ChenHao")));

        assertEquals(true, result.get("skipped"));
        assertEquals("2026-10", result.get("syncMonth"));
        assertEquals("未来月份暂无可同步考勤", result.get("message"));
        verify(wecomApiClient, never()).getAccessToken(eq("corp-id"), eq("general-secret"));
        verify(wecomApiClient, never()).getAccessToken(eq("corp-id"), eq("checkin-secret"));
        verify(wecomApiClient, never()).getCheckinData(eq("checkin-token"), anyLong(), anyLong(), anyList());
    }

    private static Map<String, Object> checkin(String userId, long epochSecond) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("userid", userId);
        record.put("checkin_time", epochSecond);
        record.put("checkin_type", 1);
        record.put("exception_type", "");
        record.put("location_title", "上海");
        return record;
    }

    private void configureEnabledWecom() {
        AttendancePlatformConfigDTO platform = new AttendancePlatformConfigDTO();
        platform.setSource("wechat-work");
        platform.setEnabled(true);
        when(dingtalkIntegrationService.getAttendancePlatformConfig("1")).thenReturn(platform);

        ThirdPartyConfigDTO.WechatWorkConfig wechatWork = new ThirdPartyConfigDTO.WechatWorkConfig();
        wechatWork.setCorpId("corp-id");
        wechatWork.setSecret("general-secret");
        wechatWork.setAddressBookSecret("general-secret");
        wechatWork.setCheckinSecret("checkin-secret");
        ThirdPartyConfigDTO config = new ThirdPartyConfigDTO();
        config.setWechatWork(wechatWork);
        when(thirdPartyConfigService.getConfigByPlatform("wechat-work", "1")).thenReturn(config);
    }
}
