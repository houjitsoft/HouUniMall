package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.UserAgentUtils;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.userms.entity.UserAgreementConsentEntity;
import com.houjiang.userms.mapper.UserAgreementConsentMapper;
import com.houjiang.userms.service.UserAgreementConsentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户协议同意记录ServiceImpl。
 *
 * @author Amos
 */
@Service
public class UserAgreementConsentServiceImpl
        extends ServiceImpl<UserAgreementConsentMapper, UserAgreementConsentEntity>
        implements UserAgreementConsentService {

    /** 状态：有效 */
    private static final int STATUS_VALID = 1;
    /** 状态：已失效 */
    private static final int STATUS_INVALID = 0;

    /** 导出上限，防止一次拉全表 */
    private static final int EXPORT_LIMIT = 20000;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int recordConsents(String userId, String username, List<Map<String, Object>> consents,
                              String ip, String ua, String source, String scene) {
        if (userId == null || userId.trim().isEmpty() || consents == null || consents.isEmpty()) {
            return 0;
        }
        Map<String, String> uaInfo = UserAgentUtils.parse(ua);
        Date now = new Date();
        String finalIp = ip == null ? "" : truncate(ip, 50);
        String finalUa = ua == null ? "" : truncate(ua, 500);
        String finalUser = username == null ? "" : truncate(username, 100);
        String finalSource = (source == null || source.trim().isEmpty()) ? "web" : truncate(source, 30);
        String finalScene = (scene == null || scene.trim().isEmpty()) ? "register" : truncate(scene, 30);

        int affected = 0;
        for (Map<String, Object> item : consents) {
            if (item == null) {
                continue;
            }
            String code = asStr(item.get("code"));
            String versionNo = asStr(item.get("versionNo"));
            if (code.isEmpty() || versionNo.isEmpty()) {
                continue;
            }
            // 1. 同协议的其它版本记录置为失效（保留历史，不物理删除）
            LambdaQueryWrapper<UserAgreementConsentEntity> stale = new LambdaQueryWrapper<>();
            stale.eq(UserAgreementConsentEntity::getUserId, userId)
                    .eq(UserAgreementConsentEntity::getAgreementCode, code)
                    .eq(UserAgreementConsentEntity::getStatus, STATUS_VALID)
                    .ne(UserAgreementConsentEntity::getVersionNo, versionNo);
            List<UserAgreementConsentEntity> staleList = this.list(stale);
            for (UserAgreementConsentEntity old : staleList) {
                old.setStatus(STATUS_INVALID);
                this.updateById(old);
            }

            // 2. 目标版本记录 upsert（唯一索引 user_id + agreement_code + version_no 兜底）
            LambdaQueryWrapper<UserAgreementConsentEntity> target = new LambdaQueryWrapper<>();
            target.eq(UserAgreementConsentEntity::getUserId, userId)
                    .eq(UserAgreementConsentEntity::getAgreementCode, code)
                    .eq(UserAgreementConsentEntity::getVersionNo, versionNo);
            UserAgreementConsentEntity exists = this.getOne(target, false);

            if (exists == null) {
                UserAgreementConsentEntity entity = new UserAgreementConsentEntity();
                entity.setUserId(truncate(userId, 64));
                entity.setUsername(finalUser);
                entity.setAgreementId(asInt(item.get("agreementId")));
                entity.setAgreementCode(truncate(code, 50));
                entity.setAgreementName(truncate(asStr(item.get("agreementName")), 100));
                entity.setVersionId(asInt(item.get("versionId")));
                entity.setVersionNo(truncate(versionNo, 20));
                entity.setScene(finalScene);
                entity.setConsentTime(now);
                entity.setIp(finalIp);
                entity.setIpLocation("");
                entity.setUa(finalUa);
                entity.setDevice(truncate(uaInfo.get("device"), 100));
                entity.setBrowser(truncate(uaInfo.get("browser"), 100));
                entity.setOs(truncate(uaInfo.get("os"), 100));
                entity.setSource(finalSource);
                entity.setStatus(STATUS_VALID);
                entity.setCreateTime(now);
                this.save(entity);
            } else {
                // 重复同意同一版本：刷新同意时间与客户端信息，重新置为有效
                exists.setUsername(finalUser);
                exists.setAgreementName(truncate(asStr(item.get("agreementName")), 100));
                exists.setVersionId(asInt(item.get("versionId")));
                exists.setScene(finalScene);
                exists.setConsentTime(now);
                exists.setIp(finalIp);
                exists.setUa(finalUa);
                exists.setDevice(truncate(uaInfo.get("device"), 100));
                exists.setBrowser(truncate(uaInfo.get("browser"), 100));
                exists.setOs(truncate(uaInfo.get("os"), 100));
                exists.setSource(finalSource);
                exists.setStatus(STATUS_VALID);
                this.updateById(exists);
            }
            affected++;
        }
        return affected;
    }

    @Override
    public ResponseResult listByUser(Map<String, Object> param) {
        try {
            String userId = asStr(param == null ? null : param.get("userId"));
            if (userId.isEmpty()) {
                return ResponseResult.error("userId 不能为空");
            }
            LambdaQueryWrapper<UserAgreementConsentEntity> qw = new LambdaQueryWrapper<>();
            qw.eq(UserAgreementConsentEntity::getUserId, userId);
            if (!asBool(param.get("includeInvalid"))) {
                qw.eq(UserAgreementConsentEntity::getStatus, STATUS_VALID);
            }
            qw.orderByAsc(UserAgreementConsentEntity::getAgreementCode)
                    .orderByDesc(UserAgreementConsentEntity::getConsentTime);
            return ResponseResult.success(this.list(qw));
        } catch (Exception e) {
            log.error("查询用户同意记录失败, param=" + param, e);
            return ResponseResult.error("查询用户同意记录失败");
        }
    }

    @Override
    public ResponseResult adminPage(Map<String, Object> param) {
        try {
            int page = asInt(param == null ? null : param.get("page"), 1);
            int limit = asInt(param == null ? null : param.get("limit"), 10);
            Page<UserAgreementConsentEntity> pageObj =
                    this.page(new Page<>(page, limit), buildQuery(param));
            return ResponseResult.success(PageUtils.getPage(pageObj));
        } catch (Exception e) {
            log.error("分页查询同意记录失败, param=" + param, e);
            return ResponseResult.error("分页查询同意记录失败");
        }
    }

    @Override
    public ResponseResult adminExport(Map<String, Object> param) {
        try {
            List<UserAgreementConsentEntity> list = this.list(buildQuery(param));
            if (list.size() > EXPORT_LIMIT) {
                list = new ArrayList<>(list.subList(0, EXPORT_LIMIT));
            }
            return ResponseResult.success(list);
        } catch (Exception e) {
            log.error("导出同意记录失败, param=" + param, e);
            return ResponseResult.error("导出同意记录失败");
        }
    }

    /**
     * 构造后台查询条件：用户 / 协议类型 / 时间范围 / 状态
     */
    private LambdaQueryWrapper<UserAgreementConsentEntity> buildQuery(Map<String, Object> param) {
        LambdaQueryWrapper<UserAgreementConsentEntity> qw = new LambdaQueryWrapper<>();
        if (param == null) {
            qw.orderByDesc(UserAgreementConsentEntity::getConsentTime);
            return qw;
        }
        String userId = asStr(param.get("userId"));
        String username = asStr(param.get("username"));
        String agreementCode = asStr(param.get("agreementCode"));
        String agreementName = asStr(param.get("agreementName"));
        String ip = asStr(param.get("ip"));
        if (!userId.isEmpty()) {
            qw.eq(UserAgreementConsentEntity::getUserId, userId);
        }
        if (!username.isEmpty()) {
            qw.like(UserAgreementConsentEntity::getUsername, username);
        }
        if (!agreementCode.isEmpty()) {
            // 支持逗号分隔多选
            String[] codes = agreementCode.split(",");
            List<String> codeList = new ArrayList<>();
            for (String c : codes) {
                if (c != null && !c.trim().isEmpty()) {
                    codeList.add(c.trim());
                }
            }
            if (codeList.size() == 1) {
                qw.eq(UserAgreementConsentEntity::getAgreementCode, codeList.get(0));
            } else if (codeList.size() > 1) {
                qw.in(UserAgreementConsentEntity::getAgreementCode, codeList);
            }
        }
        if (!agreementName.isEmpty()) {
            qw.like(UserAgreementConsentEntity::getAgreementName, agreementName);
        }
        if (!ip.isEmpty()) {
            qw.like(UserAgreementConsentEntity::getIp, ip);
        }
        Object status = param.get("status");
        if (status != null && !String.valueOf(status).trim().isEmpty()) {
            qw.eq(UserAgreementConsentEntity::getStatus, asInt(status, STATUS_VALID));
        }
        Date begin = parseDate(param.get("beginTime"));
        if (begin != null) {
            qw.ge(UserAgreementConsentEntity::getConsentTime, begin);
        }
        Date end = parseDate(param.get("endTime"));
        if (end != null) {
            qw.le(UserAgreementConsentEntity::getConsentTime, end);
        }
        qw.orderByDesc(UserAgreementConsentEntity::getConsentTime)
                .orderByDesc(UserAgreementConsentEntity::getId);
        return qw;
    }

    // ================= 工具方法 =================

    private String asStr(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value).trim();
    }

    private int asInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Integer asInt(Object value) {
        String s = asStr(value);
        if (s.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean asBool(Object value) {
        if (value == null) {
            return false;
        }
        String s = String.valueOf(value).trim();
        return "true".equalsIgnoreCase(s) || "1".equals(s);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private Date parseDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
            return null;
        }
        String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd"};
        for (String p : patterns) {
            try {
                return new SimpleDateFormat(p).parse(s);
            } catch (Exception ignored) {
                // 继续尝试下一个格式
            }
        }
        return null;
    }
}
