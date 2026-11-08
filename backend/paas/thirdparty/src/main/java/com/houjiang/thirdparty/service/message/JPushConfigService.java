package com.houjiang.thirdparty.service.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.thirdparty.entity.message.JPushConfigEntity;
import com.houjiang.thirdparty.mapper.message.JPushConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 极光推送配置服务
 *
 * @author Amos
 * @since 2025-07-02
 */
@Slf4j
@Service
public class JPushConfigService {

    @Autowired
    private JPushConfigMapper jPushConfigMapper;

    /**
     * 获取组织极光推送配置
     */
    public JPushConfigEntity getConfigByOrgId(String orgId) {
        if (!StringUtils.hasText(orgId)) {
            return null;
        }
        LambdaQueryWrapper<JPushConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JPushConfigEntity::getOrgId, orgId);
        JPushConfigEntity config = jPushConfigMapper.selectOne(wrapper);
        // 脱敏 MasterSecret
        if (config != null && StringUtils.hasText(config.getMasterSecret())) {
            String secret = config.getMasterSecret();
            if (secret.length() > 8) {
                config.setMasterSecret(secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4));
            }
        }
        return config;
    }

    /**
     * 获取完整配置（不脱敏，内部使用）
     */
    public JPushConfigEntity getFullConfigByOrgId(String orgId) {
        if (!StringUtils.hasText(orgId)) {
            return null;
        }
        LambdaQueryWrapper<JPushConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JPushConfigEntity::getOrgId, orgId);
        return jPushConfigMapper.selectOne(wrapper);
    }

    /**
     * 保存/更新配置
     */
    public boolean saveOrUpdateConfig(JPushConfigEntity config) {
        if (config == null || !StringUtils.hasText(config.getOrgId())) {
            return false;
        }
        // 查询是否已存在
        LambdaQueryWrapper<JPushConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JPushConfigEntity::getOrgId, config.getOrgId());
        JPushConfigEntity existing = jPushConfigMapper.selectOne(wrapper);

        if (existing != null) {
            config.setId(existing.getId());
            // 如果 masterSecret 包含 **** 说明前端未修改，保留原值
            if (config.getMasterSecret() != null && config.getMasterSecret().contains("****")) {
                config.setMasterSecret(existing.getMasterSecret());
            }
            config.setUpdateTime(LocalDateTime.now());
            return jPushConfigMapper.updateById(config) > 0;
        } else {
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            return jPushConfigMapper.insert(config) > 0;
        }
    }

    /**
     * 测试极光推送连接
     */
    public Map<String, Object> testConnection(String orgId) {
        Map<String, Object> result = new HashMap<>();
        try {
            JPushConfigEntity config = getFullConfigByOrgId(orgId);
            if (config == null) {
                result.put("success", false);
                result.put("message", "未配置极光推送");
                return result;
            }
            if (!StringUtils.hasText(config.getAppKey()) || !StringUtils.hasText(config.getMasterSecret())) {
                result.put("success", false);
                result.put("message", "AppKey 或 MasterSecret 未配置");
                return result;
            }
            // 调用极光 API 验证凭证（使用极光 REST API v3）
            String credentials = okhttp3.Credentials.basic(config.getAppKey(), config.getMasterSecret());
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url("https://api.jpush.cn/v3/push")
                    .header("Authorization", credentials)
                    .get()
                    .build();
            try (okhttp3.Response response = new okhttp3.OkHttpClient().newCall(request).execute()) {
                int status = response.code();
                if (status == 200 || status == 400) {
                    result.put("success", true);
                    result.put("message", "连接成功");
                } else if (status == 401) {
                    result.put("success", false);
                    result.put("message", "认证失败：AppKey 或 MasterSecret 不正确");
                } else {
                    result.put("success", false);
                    result.put("message", "连接异常，HTTP状态码：" + status);
                }
            }
        } catch (Exception e) {
            log.error("测试极光推送连接失败 orgId={}", orgId, e);
            result.put("success", false);
            result.put("message", "连接失败：" + e.getMessage());
        }
        return result;
    }
}
