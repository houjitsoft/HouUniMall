package com.houjiang.thirdparty.wecom.service.impl;

import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.thirdparty.dingtalk.dto.AttendancePlatformConfigDTO;
import com.houjiang.thirdparty.dingtalk.service.DingtalkIntegrationService;
import com.houjiang.thirdparty.dingtalk.support.AttendancePlatformConfigSupport;
import com.houjiang.thirdparty.mapper.oa.OaWecomStaffUserIdMapper;
import com.houjiang.thirdparty.service.ThirdPartyConfigService;
import com.houjiang.thirdparty.wecom.client.WecomApiClient;
import com.houjiang.thirdparty.wecom.client.WecomApiResult;
import com.houjiang.thirdparty.wecom.service.WecomAttendanceSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WecomAttendanceSyncServiceImpl implements WecomAttendanceSyncService {

    private static final String SOURCE_WECHAT = "wechat-work";
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 公司标准上班时刻：迟到判定基准之一（与排班取较晚者，两者都晚才算迟到）。
     * 可通过 Nacos 配置 houcloud.attendance.standard-start-time 调整。
     */
    @Value("${houcloud.attendance.standard-start-time:09:00}")
    private String standardStartTime = "09:00";

    /**
     * 公司标准下班时刻：早退判定基准之一（与排班取较早者）。
     * ⚠️ 这里是「公司制度下班时间」，与「餐补 20:00 门槛」是两回事，不要混用。
     * 可通过 Nacos 配置 houcloud.attendance.standard-end-time 调整。
     */
    @Value("${houcloud.attendance.standard-end-time:18:00}")
    private String standardEndTime = "18:00";

    private final WecomApiClient wecomApiClient;
    private final ThirdPartyConfigService thirdPartyConfigService;
    private final DingtalkIntegrationService dingtalkIntegrationService;
    private final AttendancePlatformConfigSupport attendancePlatformConfigSupport;
    private final OaWecomStaffUserIdMapper oaWecomStaffUserIdMapper;
    private final AttendanceSyncDateRangeResolver dateRangeResolver;

    @Override
    public Map<String, Object> syncMonth(String orgId, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> req = params != null ? params : Map.of();
        String month = str(req.get("month"));
        if (!StringUtils.hasText(orgId)) {
            result.put("skipped", true);
            result.put("message", "当前组织未确定，跳过企业微信考勤同步");
            result.put("records", List.of());
            result.put("rawCount", 0);
            result.put("recordCount", 0);
            return result;
        }
        String dataOrgId = orgId.trim();
        String configOrgId = attendancePlatformConfigSupport.resolveConfigOrgId(dataOrgId);
        AttendanceSyncDateRange range = dateRangeResolver.resolve(month);
        result.put("syncMonth", range.month().toString());
        if (!range.available()) {
            result.put("skipped", true);
            result.put("message", range.unavailableReason());
            result.put("records", List.of());
            result.put("rawCount", 0);
            result.put("recordCount", 0);
            return result;
        }
        result.put("syncStartDate", range.startDate().toString());
        result.put("syncEndDate", range.endDate().toString());
        if (!StringUtils.hasText(configOrgId)) {
            result.put("skipped", true);
            result.put("message", "当前组织未配置考勤来源，跳过企业微信考勤同步");
            result.put("records", List.of());
            result.put("rawCount", 0);
            result.put("recordCount", 0);
            return result;
        }
        if (!configOrgId.equals(dataOrgId)) {
            log.info("考勤配置 org 回退: requestOrgId={} -> configOrgId={}", dataOrgId, configOrgId);
        }

        AttendancePlatformConfigDTO platform = dingtalkIntegrationService.getAttendancePlatformConfig(configOrgId);
        if (platform == null || !SOURCE_WECHAT.equals(platform.getSource())) {
            result.put("skipped", true);
            result.put("message", "考勤来源未配置为企业微信（orgId=" + dataOrgId
                    + "，请在「考勤设置」选择企业微信并保存）");
            result.put("records", List.of());
            return result;
        }
        if (!Boolean.TRUE.equals(platform.getEnabled())) {
            result.put("skipped", true);
            result.put("message", "企业微信考勤同步未启用");
            result.put("records", List.of());
            return result;
        }

        ThirdPartyConfigDTO wechatConfig = thirdPartyConfigService.getConfigByPlatform(SOURCE_WECHAT, configOrgId);
        ThirdPartyConfigDTO.WechatWorkConfig ww = wechatConfig != null ? wechatConfig.getWechatWork() : null;
        if (ww == null || !StringUtils.hasText(ww.getCorpId()) || !StringUtils.hasText(ww.getSecret())) {
            result.put("skipped", true);
            result.put("message", "企业微信 CorpId/Secret 未配置");
            result.put("records", List.of());
            return result;
        }

        String listSecret = StringUtils.hasText(ww.getAddressBookSecret()) ? ww.getAddressBookSecret() : ww.getSecret();
        // 考勤拉取必须用「打卡」应用 Secret，勿回退到厚匠科技等自建应用 Secret（会 errcode=0 但 rawCount=0）
        if (!StringUtils.hasText(ww.getCheckinSecret())) {
            result.put("skipped", true);
            result.put("message", "未配置「打卡 Secret」。请在系统设置 → 企业微信填写："
                    + "企微管理后台 → 应用管理 →「打卡」→ Secret（与 AgentID 1000012 的厚匠科技 Secret 不是同一个）");
            result.put("records", List.of());
            result.put("rawCount", 0);
            result.put("userCount", 0);
            result.put("recordCount", 0);
            return result;
        }
        String checkinSecret = ww.getCheckinSecret().trim();

        String listToken = wecomApiClient.getAccessToken(ww.getCorpId(), listSecret);
        if (!StringUtils.hasText(listToken)) {
            result.put("skipped", true);
            result.put("message", "无法获取企业微信 access_token，请检查 CorpId/Secret");
            result.put("records", List.of());
            return result;
        }
        String checkinToken = checkinSecret.equals(listSecret)
                ? listToken
                : wecomApiClient.getAccessToken(ww.getCorpId(), checkinSecret);
        if (!StringUtils.hasText(checkinToken)) {
            result.put("skipped", true);
            result.put("message", "无法获取打卡应用 access_token，请检查「打卡 Secret」");
            result.put("records", List.of());
            return result;
        }

        List<String> presetUserIds = parseUserIdList(req.get("userIds"));
        if (presetUserIds.isEmpty()) {
            presetUserIds = loadStaffUserIdsFromOa(dataOrgId);
        }
        Map<String, Map<String, Object>> userMeta = new HashMap<>();
        List<String> userIds = new ArrayList<>();
        if (!presetUserIds.isEmpty()) {
            userIds.addAll(presetUserIds);
            log.info("考勤同步使用预设 userIds（请求或本地员工表），共 {} 人", userIds.size());
        } else {
            ListUsersResult listUsers = resolveWecomUsers(ww, listToken);
            if (listUsers.users().isEmpty()) {
                result.put("skipped", true);
                result.put("message", listUsers.message());
                result.put("records", List.of());
                return result;
            }
            for (Map<String, Object> u : listUsers.users()) {
                String userid = str(u.get("userid"));
                if (!StringUtils.hasText(userid)) {
                    continue;
                }
                userIds.add(userid);
                userMeta.put(userid, u);
            }
        }

        WecomApiClient.CheckinDataResult fetch = wecomApiClient.getCheckinData(
                checkinToken, range.startEpochSecond(), range.endEpochSecond(), userIds);
        List<Map<String, Object>> rawCheckins = fetch.records() != null ? fetch.records() : List.of();
        List<Map<String, Object>> dailyRecords = aggregateDaily(dataOrgId, range, rawCheckins, userMeta);

        result.put("skipped", false);
        result.put("userCount", userIds.size());
        result.put("rawCount", rawCheckins.size());
        result.put("recordCount", dailyRecords.size());
        result.put("records", dailyRecords);
        if (fetch.errcode() != null) {
            result.put("apiError", true);
            result.put("errcode", fetch.errcode());
            result.put("errmsg", fetch.errmsg());
            result.put("message", buildCheckinApiMessage(fetch.errcode(), fetch.errmsg()));
        } else if (rawCheckins.isEmpty() && !userIds.isEmpty()) {
            result.put("message", "企微 getcheckindata 未返回打卡记录(rawCount=0)。请确认："
                    + "① 使用的是「打卡」应用 Secret；② 企微可信 IP 含服务器公网 IP；"
                    + "③ 同步月份与报表一致；④ 考勤机已勾选「打卡应用消息推送」且成员 userid 正确");
        } else {
            result.put("message", "同步完成");
        }
        log.info("企微考勤同步完成: orgId={}, month={}, range={}~{}, rawCount={}, recordCount={}",
                dataOrgId, range.month(), range.startDate(), range.endDate(), rawCheckins.size(), dailyRecords.size());
        return result;
    }

    private List<Map<String, Object>> aggregateDaily(String orgId, AttendanceSyncDateRange range,
                                                     List<Map<String, Object>> rawCheckins,
                                                     Map<String, Map<String, Object>> userMeta) {
        Map<String, Map<String, Object>> bucket = new LinkedHashMap<>();
        for (Map<String, Object> row : rawCheckins) {
            String userid = str(row.get("userid"));
            if (!StringUtils.hasText(userid)) {
                continue;
            }
            long checkinSec = toLong(row.get("checkin_time"));
            if (checkinSec <= 0) {
                continue;
            }
            LocalDate date = Instant.ofEpochSecond(checkinSec).atZone(ZONE).toLocalDate();
            if (date.isBefore(range.startDate()) || date.isAfter(range.endDate())) {
                continue;
            }
            String key = userid + "|" + date;
            Map<String, Object> day = bucket.computeIfAbsent(key, k -> {
                Map<String, Object> m = new HashMap<>();
                m.put("orgId", orgId);
                m.put("dataSource", SOURCE_WECHAT);
                m.put("externalUserId", userid);
                Map<String, Object> meta = userMeta.get(userid);
                if (meta != null) {
                    m.put("employeeName", str(meta.get("name")));
                    m.put("mobile", str(meta.get("mobile")));
                    m.put("departmentName", firstDepartmentName(meta));
                }
                m.put("attendanceDate", date.toString());
                m.put("lateMinutes", 0);
                m.put("earlyLeaveMinutes", 0);
                m.put("attendanceStatus", "正常");
                return m;
            });

            String type = resolveCheckinType(row.get("checkin_type"));
            String exceptionType = str(row.get("exception_type"));
            // 企微「未打卡」会返回标准班次时刻作为占位，不得当作真实打卡落入签到/签退
            boolean missingPunch = isMissingPunchException(exceptionType);
            if (missingPunch) {
                mergeAttendanceStatus(day, mapException(exceptionType, type));
                if (type.contains("上班") || type.contains("外出")) {
                    day.put("missingCheckIn", true);
                } else if (type.contains("下班")) {
                    day.put("missingCheckOut", true);
                } else {
                    day.put("missingCheckIn", true);
                    day.put("missingCheckOut", true);
                }
                continue;
            }

            LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochSecond(checkinSec), ZONE);
            long schSec = toLong(row.get("sch_checkin_time"));
            if ((type.contains("上班") || type.contains("外出")) && (!day.containsKey("checkInTime")
                    || dt.isBefore((LocalDateTime) day.get("checkInTime")))) {
                day.put("checkInTime", dt);
                day.put("checkInAddress", str(row.get("location_title")));
                if (schSec > 0) {
                    day.put("schCheckInSec", schSec);
                }
            }
            if (type.contains("下班")) {
                if (!day.containsKey("checkOutTime") || dt.isAfter((LocalDateTime) day.get("checkOutTime"))) {
                    day.put("checkOutTime", dt);
                    day.put("checkOutAddress", str(row.get("location_title")));
                    if (schSec > 0) {
                        day.put("schCheckOutSec", schSec);
                    }
                }
            }
            // 考勤机 /「仅记录打卡」等：无上下班字样时，最早作签到、最晚作签退
            if (!type.contains("上班") && !type.contains("外出") && !type.contains("下班")) {
                if (!day.containsKey("checkInTime") || dt.isBefore((LocalDateTime) day.get("checkInTime"))) {
                    day.put("checkInTime", dt);
                    day.put("checkInAddress", str(row.get("location_title")));
                    if (schSec > 0) {
                        day.put("schCheckInSec", schSec);
                    }
                }
                if (!day.containsKey("checkOutTime") || dt.isAfter((LocalDateTime) day.get("checkOutTime"))) {
                    day.put("checkOutTime", dt);
                    day.put("checkOutAddress", str(row.get("location_title")));
                    if (schSec > 0) {
                        day.put("schCheckOutSec", schSec);
                    }
                }
            }
            if (StringUtils.hasText(exceptionType) && !"0".equals(exceptionType)) {
                mergeAttendanceStatus(day, mapException(exceptionType, type));
            }
        }

        for (Map<String, Object> day : bucket.values()) {
            LocalDateTime in = (LocalDateTime) day.get("checkInTime");
            LocalDateTime out = (LocalDateTime) day.get("checkOutTime");
            boolean missingIn = Boolean.TRUE.equals(day.get("missingCheckIn"));
            boolean missingOut = Boolean.TRUE.equals(day.get("missingCheckOut"));
            if (in != null && out != null && out.isAfter(in)) {
                double hours = java.time.Duration.between(in, out).toMinutes() / 60.0;
                day.put("workHours", Math.round(hours * 100.0) / 100.0);
                if (hours > 8) {
                    day.put("overtimeHours", Math.round((hours - 8) * 100.0) / 100.0);
                } else {
                    day.put("overtimeHours", 0);
                }
            } else {
                day.put("workHours", 0);
                day.put("overtimeHours", 0);
            }
            // 仅「确实没有有效打卡」才标缺勤；不能因企微同时回了「未打卡」占位就把已有打卡日打成缺勤
            if (in == null && out == null) {
                day.put("attendanceStatus", "缺勤");
            } else if (missingIn && in == null) {
                mergeAttendanceStatus(day, "未打卡");
            } else if (missingOut && out == null) {
                mergeAttendanceStatus(day, "未打卡");
            }
            if (in != null) {
                // 迟到基准：企微排班上班时刻（缺失时用公司标准上班时间 09:00）
                LocalDateTime deadline = resolveScheduleTime(day.get("schCheckInSec"), in.toLocalDate(),
                        standardStart().getHour(), standardStart().getMinute());
                if (in.isAfter(deadline)) {
                    int late = (int) java.time.Duration.between(deadline, in).toMinutes();
                    day.put("lateMinutes", late);
                    if (late > 0 && shouldUpgradeToLateOrEarly(str(day.get("attendanceStatus")))) {
                        day.put("attendanceStatus", "迟到");
                    }
                } else {
                    // 显式归零：重同步时不能保留历史迟到分钟数
                    day.put("lateMinutes", 0);
                }
            } else {
                day.put("lateMinutes", 0);
            }
            if (out != null) {
                // 早退基准 = min(公司制度下班时间, 企微排班下班时刻)
                // ⚠️ 企微打卡应用的班次下班常配 19:30（宽松打卡窗口），不等于公司制度下班时间；
                //    公司制度下班是 18:00，20:00 是餐补门槛（薪资侧另一条规则），三者不可混用。
                LocalDateTime deadline = resolveEarlyLeaveDeadline(day.get("schCheckOutSec"), out.toLocalDate());
                if (out.isBefore(deadline)) {
                    int early = (int) java.time.Duration.between(out, deadline).toMinutes();
                    day.put("earlyLeaveMinutes", early);
                    if (early > 0 && shouldUpgradeToLateOrEarly(str(day.get("attendanceStatus")))) {
                        day.put("attendanceStatus", "早退");
                    }
                } else {
                    // 显式归零：重同步时不能保留历史早退分钟数
                    day.put("earlyLeaveMinutes", 0);
                    // 企微自身按其班次(19:30)判定的「时间异常」不能作为早退依据，降级回正常/迟到
                    if ("早退".equals(str(day.get("attendanceStatus")))) {
                        Integer lateMin = (Integer) day.get("lateMinutes");
                        day.put("attendanceStatus", lateMin != null && lateMin > 0 ? "迟到" : "正常");
                    }
                }
            } else {
                day.put("earlyLeaveMinutes", 0);
            }
            // 内部字段不落库
            day.remove("schCheckInSec");
            day.remove("schCheckOutSec");
            day.remove("missingCheckIn");
            day.remove("missingCheckOut");
        }
        return new ArrayList<>(bucket.values());
    }

    private String buildCheckinApiMessage(Integer errcode, String errmsg) {
        if (errcode != null && errcode == 48002) {
            return "企微拒绝打卡接口(48002 api forbidden)：当前 Secret 无「读取打卡数据」权限。"
                    + "请在企微管理后台打开「打卡」应用，将其 Secret 填入系统设置的主 Secret 或「打卡 Secret」后重试";
        }
        String tail = StringUtils.hasText(errmsg) ? errmsg : "";
        return errcode != null
                ? "企微 getcheckindata 失败(errcode=" + errcode + "): " + tail
                : "企微 getcheckindata 失败: " + tail;
    }

    /**
     * 企微 getcheckindata 的 exception_type 为中文（时间异常/地点异常/未打卡/wifi异常/非常用设备），
     * 旧版数字码仅作兼容。
     */
    private String mapException(String exceptionType, String checkinType) {
        String ex = str(exceptionType);
        if (!StringUtils.hasText(ex) || "0".equals(ex)) {
            return "";
        }
        if (ex.contains("未打卡")) {
            return "未打卡";
        }
        if (ex.contains("时间异常")) {
            String type = str(checkinType);
            if (type.contains("下班")) {
                return "早退";
            }
            return "迟到";
        }
        if (ex.contains("地点异常") || ex.contains("wifi异常") || ex.contains("WiFi异常")
                || ex.contains("非常用设备")) {
            return "异常";
        }
        return switch (ex) {
            case "1", "2", "3" -> "迟到";
            case "4", "5" -> "早退";
            case "6" -> "缺勤";
            default -> "异常";
        };
    }

    private boolean isMissingPunchException(String exceptionType) {
        return str(exceptionType).contains("未打卡");
    }

    /**
     * 状态优先级：缺勤 > 未打卡 > 早退 > 迟到 > 异常 > 正常
     */
    private void mergeAttendanceStatus(Map<String, Object> day, String incoming) {
        if (!StringUtils.hasText(incoming)) {
            return;
        }
        String current = str(day.get("attendanceStatus"));
        if (!StringUtils.hasText(current) || "正常".equals(current)) {
            day.put("attendanceStatus", incoming);
            return;
        }
        if (statusRank(incoming) > statusRank(current)) {
            day.put("attendanceStatus", incoming);
        }
    }

    private int statusRank(String status) {
        if (status == null) {
            return 0;
        }
        if (status.contains("缺勤")) {
            return 50;
        }
        if (status.contains("未打卡")) {
            return 40;
        }
        if (status.contains("早退")) {
            return 30;
        }
        if (status.contains("迟到")) {
            return 20;
        }
        if (status.contains("异常")) {
            return 10;
        }
        return 0;
    }

    /** 仅在正常态时由班次时刻升级为迟到/早退；地点异常等不因本地班次重算改成迟到 */
    private boolean shouldUpgradeToLateOrEarly(String status) {
        return !StringUtils.hasText(status) || "正常".equals(status) || "迟到".equals(status);
    }

    private LocalDateTime resolveScheduleTime(Object schSecObj, LocalDate date, int defaultHour, int defaultMinute) {
        long schSec = toLong(schSecObj);
        if (schSec > 0) {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(schSec), ZONE);
        }
        return date.atTime(defaultHour, defaultMinute);
    }

    /**
     * 早退判定基准 = min(公司制度下班时间, 企微排班下班时刻)。
     * <p>
     * 企微打卡应用的班次下班时刻通常配 19:30（宽松打卡窗口），若直接拿它当早退基准，
     * 18:00-19:30 之间离开的员工会被误判早退。公司的制度下班时间是 18:00，
     * 20:00 是「餐补门槛」（薪资侧独立规则），两者不是一回事。
     * 若某天排班本身早于制度下班时间（半天班/调休），仍以排班为准，取较早者。
     */
    private LocalDateTime resolveEarlyLeaveDeadline(Object schSecObj, LocalDate date) {
        LocalDateTime standard = date.atTime(standardEnd());
        long schSec = toLong(schSecObj);
        if (schSec <= 0) {
            return standard;
        }
        LocalDateTime sch = LocalDateTime.ofInstant(Instant.ofEpochSecond(schSec), ZONE);
        return sch.isBefore(standard) ? sch : standard;
    }

    private LocalTime standardStart() {
        return parseTime(standardStartTime, LocalTime.of(9, 0));
    }

    private LocalTime standardEnd() {
        return parseTime(standardEndTime, LocalTime.of(18, 0));
    }

    /** 解析 "HH:mm" / "HH:mm:ss" 形式的时刻，非法值回退默认 */
    private LocalTime parseTime(String text, LocalTime fallback) {
        if (!StringUtils.hasText(text)) {
            return fallback;
        }
        try {
            String[] parts = text.trim().split(":");
            int hour = Integer.parseInt(parts[0].trim());
            int minute = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
            int second = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 0;
            return LocalTime.of(hour, minute, second);
        } catch (Exception e) {
            log.warn("考勤标准时刻配置解析失败: {}，回退默认 {}", text, fallback);
            return fallback;
        }
    }

    @SuppressWarnings("unchecked")
    private String firstDepartmentName(Map<String, Object> user) {
        Object dept = user.get("department");
        if (dept instanceof List<?> list && !list.isEmpty()) {
            return "部门" + list.get(0);
        }
        return "";
    }

    private String resolveCheckinType(Object raw) {
        if (raw == null) {
            return "";
        }
        if (raw instanceof Number n) {
            return switch (n.intValue()) {
                case 1 -> "上班";
                case 2 -> "下班";
                case 3 -> "外出";
                default -> raw.toString();
            };
        }
        return str(raw);
    }

    private String str(Object o) {
        return o == null ? "" : o.toString().trim();
    }

    private long toLong(Object o) {
        if (o instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(str(o));
        } catch (Exception e) {
            return 0L;
        }
    }

    /** 通讯录 API 常因应用可见范围返回空，回退 OA 表 hou_oa_wecom_staff */
    private List<String> loadStaffUserIdsFromOa(String orgId) {
        try {
            List<String> ids = oaWecomStaffUserIdMapper.listUserIdsByOrgId(orgId);
            if (ids != null && !ids.isEmpty()) {
                log.info("考勤同步回退 hou_oa_wecom_staff，orgId={}，共 {} 人", orgId, ids.size());
                return new ArrayList<>(ids);
            }
        } catch (Exception e) {
            log.warn("读取 hou_oa_wecom_staff 失败: {}", e.getMessage());
        }
        return List.of();
    }

    private record ListUsersResult(List<Map<String, Object>> users, String message) {}

    /**
     * 通讯录拉成员：先通讯录 Secret，再回退主 Secret；返回明确 errcode 便于排查。
     */
    private ListUsersResult resolveWecomUsers(ThirdPartyConfigDTO.WechatWorkConfig ww, String listToken) {
        WecomApiResult<List<Map<String, Object>>> first = wecomApiClient.listDepartmentUsersDetailed(listToken, 1);
        if (first.isOk() && first.getData() != null && !first.getData().isEmpty()) {
            return new ListUsersResult(first.getData(), "");
        }
        String firstErr = formatUserListError(first);
        if (StringUtils.hasText(ww.getAddressBookSecret()) && StringUtils.hasText(ww.getSecret())) {
            String fallbackToken = wecomApiClient.getAccessToken(ww.getCorpId(), ww.getSecret());
            if (StringUtils.hasText(fallbackToken) && !fallbackToken.equals(listToken)) {
                WecomApiResult<List<Map<String, Object>>> second =
                        wecomApiClient.listDepartmentUsersDetailed(fallbackToken, 1);
                if (second.isOk() && second.getData() != null && !second.getData().isEmpty()) {
                    log.info("通讯录 Secret 无成员，已回退主应用 Secret 拉取 user/list");
                    return new ListUsersResult(second.getData(), "");
                }
                firstErr = formatUserListError(second);
            }
        }
        return new ListUsersResult(List.of(),
                "未拉取到企业微信成员。" + firstErr
                        + " 建议：① 企微管理→应用管理→厚匠科技 扩大可见范围；② 或先在「企微管理→员工管理」同步员工；"
                        + "③ 通讯录 Secret 无权限时可改用主 Secret 并开启通讯录只读权限");
    }

    private String formatUserListError(WecomApiResult<List<Map<String, Object>>> res) {
        if (res == null) {
            return "接口无响应";
        }
        if (!res.isOk()) {
            return res.toUserMessage();
        }
        return "user/list 返回空列表（请检查应用可见范围是否包含员工）";
    }

    @SuppressWarnings("unchecked")
    private List<String> parseUserIdList(Object raw) {
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        for (Object item : list) {
            if (item == null) {
                continue;
            }
            String uid = item.toString().trim();
            if (StringUtils.hasText(uid) && !ids.contains(uid)) {
                ids.add(uid);
            }
        }
        return ids;
    }
}
