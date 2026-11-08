package com.houjiang.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.base.entity.PlatformSiteConsentEntity;
import com.houjiang.base.entity.PlatformSiteEntity;
import com.houjiang.base.mapper.PlatformSiteConsentMapper;
import com.houjiang.base.mapper.PlatformSiteMapper;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 平台侧：站点协议签署记录回传服务。
 *
 * <p><b>为什么要有这一层</b>：协议是平台统一发布的，站点（客户部署的站）里用户同意后，
 * 记录原本只落在站内的 {@code hou_user_agreement_consent}。平台需要留存各站点、各用户的
 * 签署凭据用于合规举证与统计，所以站点在写完站内记录后回传一份到平台。</p>
 *
 * <p><b>口径（务必保持）</b>：</p>
 * <ul>
 *   <li>平台侧留痕<b>不替代</b>站内记录，它是副本；</li>
 *   <li>回传失败<b>不阻断</b>站内留痕，也不该让站点用户看到报错——合规主记录始终在站内；</li>
 *   <li>按 {@code (site_code, user_id, agreement_code, version_no)} 幂等，重复上报只更新
 *       最近一次时间与来源，不产生重复行。</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
@Service
public class PlatformConsentService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private PlatformSiteConsentMapper consentMapper;

    @Autowired
    private PlatformSiteMapper siteMapper;

    /**
     * 落库站点回传的签署记录（幂等）。
     *
     * @param items    签署记录列表
     * @param siteCode 站点标识
     * @param siteName 站点名称
     * @return 成功响应，data 含 accepted（落库条数）/ skipped（非法条数）
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult report(List<Map<String, Object>> items, String siteCode, String siteName) {
        if (!StringUtils.hasText(siteCode)) {
            return ResponseResult.error("缺少站点标识 siteCode");
        }
        if (items == null || items.isEmpty()) {
            return ResponseResult.error("签署记录为空");
        }
        int accepted = 0;
        int skipped = 0;
        for (Map<String, Object> item : items) {
            if (item == null) {
                skipped++;
                continue;
            }
            String agreementCode = str(item.get("agreementCode"));
            String versionNo = str(item.get("versionNo"));
            if (!StringUtils.hasText(agreementCode) || !StringUtils.hasText(versionNo)) {
                skipped++;
                continue;
            }
            String userId = str(item.get("userId"));
            upsertOne(item, siteCode, siteName, userId, agreementCode, versionNo);
            accepted++;
        }
        touchSite(siteCode, siteName, accepted);
        Map<String, Object> data = new HashMap<>(4);
        data.put("accepted", accepted);
        data.put("skipped", skipped);
        return ResponseResult.success(data);
    }

    /**
     * 平台后台：签署记录分页查询。
     *
     * @param param siteCode / agreementCode / userId / username / scene / page / limit
     * @return 分页结果
     */
    public ResponseResult page(Map<String, Object> param) {
        try {
            int page = asInt(param, "page", 1);
            int limit = asInt(param, "limit", 20);
            LambdaQueryWrapper<PlatformSiteConsentEntity> qw = new LambdaQueryWrapper<>();
            String siteCode = asStr(param, "siteCode");
            String agreementCode = asStr(param, "agreementCode");
            String userId = asStr(param, "userId");
            String username = asStr(param, "username");
            String scene = asStr(param, "scene");
            if (StringUtils.hasText(siteCode)) {
                qw.eq(PlatformSiteConsentEntity::getSiteCode, siteCode);
            }
            if (StringUtils.hasText(agreementCode)) {
                qw.eq(PlatformSiteConsentEntity::getAgreementCode, agreementCode);
            }
            if (StringUtils.hasText(userId)) {
                qw.eq(PlatformSiteConsentEntity::getUserId, userId);
            }
            if (StringUtils.hasText(username)) {
                qw.like(PlatformSiteConsentEntity::getUsername, username);
            }
            if (StringUtils.hasText(scene)) {
                qw.eq(PlatformSiteConsentEntity::getScene, scene);
            }
            qw.orderByDesc(PlatformSiteConsentEntity::getConsentTime)
                    .orderByDesc(PlatformSiteConsentEntity::getId);
            Page<PlatformSiteConsentEntity> pageObj = consentMapper.selectPage(new Page<>(page, limit), qw);
            return ResponseResult.success(PageUtils.getPage(pageObj));
        } catch (Exception e) {
            log.error("平台侧协议签署记录分页查询失败, param=" + param, e);
            return ResponseResult.error("平台侧协议签署记录分页查询失败");
        }
    }

    /**
     * 平台后台：已接入回传的站点清单。
     */
    public ResponseResult sites() {
        try {
            List<PlatformSiteEntity> list = siteMapper.selectList(
                    new LambdaQueryWrapper<PlatformSiteEntity>()
                            .orderByDesc(PlatformSiteEntity::getLastReportTime));
            return ResponseResult.success(list == null ? new ArrayList<>() : list);
        } catch (Exception e) {
            log.error("查询已接入站点失败", e);
            return ResponseResult.error("查询已接入站点失败");
        }
    }

    // ================= 内部实现 =================

    private void upsertOne(Map<String, Object> item, String siteCode, String siteName,
                           String userId, String agreementCode, String versionNo) {
        Date consentTime = parseTime(item.get("consentTime"));
        PlatformSiteConsentEntity entity = consentMapper.selectOne(
                new LambdaQueryWrapper<PlatformSiteConsentEntity>()
                        .eq(PlatformSiteConsentEntity::getSiteCode, siteCode)
                        .eq(PlatformSiteConsentEntity::getUserId, userId)
                        .eq(PlatformSiteConsentEntity::getAgreementCode, agreementCode)
                        .eq(PlatformSiteConsentEntity::getVersionNo, versionNo)
                        .last("LIMIT 1"));
        boolean insert;
        if (entity == null) {
            entity = new PlatformSiteConsentEntity();
            insert = true;
        } else {
            insert = false;
        }
        entity.setSiteCode(siteCode);
        entity.setSiteName(siteName);
        entity.setUserId(userId);
        entity.setUsername(str(item.get("username")));
        entity.setAgreementCode(agreementCode);
        entity.setAgreementName(str(item.get("agreementName")));
        entity.setVersionNo(versionNo);
        entity.setScene(str(item.get("scene")));
        entity.setSource(str(item.get("source")));
        entity.setIp(str(item.get("ip")));
        String ua = str(item.get("userAgent"));
        entity.setUserAgent(ua.length() > 500 ? ua.substring(0, 500) : ua);
        entity.setConsentTime(consentTime == null ? new Date() : consentTime);
        try {
            if (insert) {
                consentMapper.insert(entity);
            } else {
                consentMapper.updateById(entity);
            }
        } catch (DuplicateKeyException dup) {
            // 并发下同一条被另一个请求先插入：退化为更新，保证幂等而不是把异常抛给站点。
            PlatformSiteConsentEntity existed = consentMapper.selectOne(
                    new LambdaQueryWrapper<PlatformSiteConsentEntity>()
                            .eq(PlatformSiteConsentEntity::getSiteCode, siteCode)
                            .eq(PlatformSiteConsentEntity::getUserId, userId)
                            .eq(PlatformSiteConsentEntity::getAgreementCode, agreementCode)
                            .eq(PlatformSiteConsentEntity::getVersionNo, versionNo)
                            .last("LIMIT 1"));
            if (existed != null) {
                entity.setId(existed.getId());
                consentMapper.updateById(entity);
            }
        }
    }

    private void touchSite(String siteCode, String siteName, int accepted) {
        try {
            PlatformSiteEntity site = siteMapper.selectOne(
                    new LambdaQueryWrapper<PlatformSiteEntity>()
                            .eq(PlatformSiteEntity::getSiteCode, siteCode)
                            .last("LIMIT 1"));
            Date now = new Date();
            if (site == null) {
                site = new PlatformSiteEntity();
                site.setSiteCode(siteCode);
                site.setSiteName(siteName);
                site.setFirstReportTime(now);
                site.setLastReportTime(now);
                site.setReportCount((long) accepted);
                try {
                    siteMapper.insert(site);
                    return;
                } catch (DuplicateKeyException dup) {
                    // 并发首次上报：转入下面的更新分支
                }
            }
            PlatformSiteEntity update = new PlatformSiteEntity();
            update.setSiteCode(siteCode);
            update.setLastReportTime(now);
            siteMapper.update(update, new LambdaUpdateWrapper<PlatformSiteEntity>()
                    .eq(PlatformSiteEntity::getSiteCode, siteCode));
            // report_count 用 SQL 累加，避免并发回传互相覆盖
            siteMapper.update(null, new LambdaUpdateWrapper<PlatformSiteEntity>()
                    .eq(PlatformSiteEntity::getSiteCode, siteCode)
                    .setSql("report_count = report_count + " + Math.max(accepted, 0)));
            if (StringUtils.hasText(siteName)) {
                siteMapper.update(null, new LambdaUpdateWrapper<PlatformSiteEntity>()
                        .eq(PlatformSiteEntity::getSiteCode, siteCode)
                        .isNull(PlatformSiteEntity::getSiteName)
                        .set(PlatformSiteEntity::getSiteName, siteName));
            }
        } catch (Exception e) {
            // 站点统计不影响签署记录落库，失败只记日志
            log.warn("更新平台站点统计失败, siteCode={}, reason={}", siteCode, e.getMessage());
        }
    }

    private static String str(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    private static Date parseTime(Object v) {
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return new Date(Long.parseLong(s));
        } catch (NumberFormatException ignored) {
            // 不是时间戳，按字符串解析
        }
        try {
            LocalDateTime dt = LocalDateTime.parse(s.replace(' ', 'T'), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return Date.from(dt.atZone(java.time.ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) {
            log.debug("无法解析同意时间: {}", s);
            return null;
        }
    }

    private static int asInt(Map<String, Object> param, String key, int def) {
        if (param == null) {
            return def;
        }
        Object v = param.get(key);
        if (v == null) {
            return def;
        }
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static String asStr(Map<String, Object> param, String key) {
        if (param == null) {
            return "";
        }
        return str(param.get(key));
    }
}
