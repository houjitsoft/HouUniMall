package com.houjiang.userms.service;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.util.RequestTokenUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 把最新组织成员关系写回当前请求的 Redis 登录会话。
 *
 * <p>公司登记 / 绑组织只更新了库表时，前端 localStorage 可能已有 orgList，
 * 但后端 {@code ParseTokenUtils} 读的仍是会话快照；不回写会导致智能入库等
 * 按组织隔离的接口误判「未加入任何组织」。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionOrganizationSync {

    private final RedisCache redisCache;

    public void syncCurrentSession(String userId, String orgId, List<Map<String, Object>> orgList) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        String token = RequestTokenUtils.getTokenFromRequest();
        if (token == null || token.isBlank()) {
            log.debug("无当前 token，跳过会话组织回写 userId={}", userId);
            return;
        }
        String redisKey = HouConfig.HOU_USER_TOKEN_USER + token;
        try {
            Object raw = redisCache.getCacheObject(redisKey);
            if (raw == null) {
                log.warn("会话不存在，无法回写组织 userId={}", userId);
                return;
            }
            String json = raw instanceof String ? (String) raw : JSON.toJSONString(raw);
            UserInfoDTO dto = JSON.parseObject(json, UserInfoDTO.class);
            if (dto == null || dto.getUser() == null) {
                log.warn("会话反序列化失败，无法回写组织 userId={}", userId);
                return;
            }
            UserInfoVO user = dto.getUser();
            if (!userId.equals(String.valueOf(user.getUserId()))) {
                log.warn("会话 userId 与请求不一致，跳过回写 session={}, request={}",
                        user.getUserId(), userId);
                return;
            }
            if (orgList != null) {
                user.setOrgList(orgList);
            }
            if (orgId != null && !orgId.isBlank()) {
                user.setOrgId(orgId.trim());
            } else if ((user.getOrgId() == null || user.getOrgId().isBlank())
                    && orgList != null && !orgList.isEmpty()) {
                Object first = orgList.get(0).get("org_id");
                if (first == null) first = orgList.get(0).get("orgId");
                if (first != null) user.setOrgId(String.valueOf(first));
            }
            dto.setUser(user);
            Long ttl = redisCache.getExpire(redisKey);
            if (ttl != null && ttl > 0) {
                redisCache.setCacheObject(redisKey, JSON.toJSONString(dto), ttl.intValue(), TimeUnit.SECONDS);
            } else {
                redisCache.setCacheObject(redisKey, JSON.toJSONString(dto),
                        HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
            }
            log.info("已回写登录会话组织 userId={}, orgId={}, orgCount={}",
                    userId, user.getOrgId(), orgList == null ? 0 : orgList.size());
        } catch (Exception e) {
            log.warn("回写登录会话组织失败 userId={}: {}", userId, e.getMessage());
        }
    }
}
