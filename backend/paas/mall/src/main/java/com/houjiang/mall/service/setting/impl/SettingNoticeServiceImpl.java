package com.houjiang.mall.service.setting.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.mall.entity.setting.SettingNoticeSetting;
import com.houjiang.mall.mapper.setting.SettingNoticeSettingMapper;
import com.houjiang.mall.service.setting.SettingNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 通知设置 Service 实现。对应 like-admin SettingNoticeServiceImpl。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingNoticeServiceImpl implements SettingNoticeService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_REF = new TypeReference<Map<String, Object>>() {};

    private final SettingNoticeSettingMapper noticeSettingMapper;

    @Override
    public List<Map<String, Object>> list(Integer recipient) {
        QueryWrapper<SettingNoticeSetting> qw = new QueryWrapper<>();
        if (recipient != null) {
            qw.eq("recipient", recipient);
        }
        qw.orderByAsc("id");
        List<SettingNoticeSetting> rows = noticeSettingMapper.selectList(qw);
        List<Map<String, Object>> list = new ArrayList<>();
        for (SettingNoticeSetting n : rows) {
            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("id", n.getId());
            vo.put("sceneName", n.getSceneName());
            vo.put("type", n.getType());
            vo.put("typeText", n.getType() != null && n.getType() == 1 ? "业务通知" : "验证码");

            // 与 like-admin 对齐：按 system→sms→oa→mnp 顺序，最后一个 status=1 的覆盖到 smsNotice
            // （前端 List 用 smsNotice 字段做"已启用通道概览"展示）
            Map<String, Object> systemNotice = parseJson(n.getSystemNotice());
            Map<String, Object> smsNotice = parseJson(n.getSmsNotice());
            Map<String, Object> oaNotice = parseJson(n.getOaNotice());
            Map<String, Object> mnpNotice = parseJson(n.getMnpNotice());
            Map<String, Object> previewNotice = null;
            if (isEnabled(systemNotice)) previewNotice = systemNotice;
            if (isEnabled(smsNotice)) previewNotice = smsNotice;
            if (isEnabled(oaNotice)) previewNotice = oaNotice;
            if (isEnabled(mnpNotice)) previewNotice = mnpNotice;
            vo.put("smsNotice", previewNotice);
            list.add(vo);
        }
        return list;
    }

    @Override
    public Map<String, Object> detail(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id 参数无效");
        }
        SettingNoticeSetting n = noticeSettingMapper.selectOne(
                new QueryWrapper<SettingNoticeSetting>().eq("id", id).last("limit 1"));
        if (n == null) {
            throw new IllegalArgumentException("通知设置不存在: id=" + id);
        }
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("id", n.getId());
        vo.put("sceneId", n.getSceneId());
        vo.put("sceneName", n.getSceneName());
        vo.put("sceneDesc", n.getSceneDesc());
        vo.put("type", n.getType() != null && n.getType() == 1 ? "业务通知" : "验证码");
        vo.put("systemNotice", parseJson(n.getSystemNotice()));
        vo.put("smsNotice", parseJson(n.getSmsNotice()));
        vo.put("oaNotice", parseJson(n.getOaNotice()));
        vo.put("mnpNotice", parseJson(n.getMnpNotice()));
        return vo;
    }

    @Override
    public void save(Map<String, Object> params) {
        if (params == null || params.get("id") == null) {
            throw new IllegalArgumentException("id 参数缺失");
        }
        Integer id = Integer.parseInt(params.get("id").toString());
        SettingNoticeSetting n = noticeSettingMapper.selectOne(
                new QueryWrapper<SettingNoticeSetting>().eq("id", id).last("limit 1"));
        if (n == null) {
            throw new IllegalArgumentException("通知设置不存在: id=" + id);
        }

        Object templateObj = params.get("template");
        if (!(templateObj instanceof Map)) {
            throw new IllegalArgumentException("template 参数缺失或格式错误");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> template = (Map<String, Object>) templateObj;

        n.setSystemNotice(toJson(template.get("system_notice")));
        n.setSmsNotice(toJson(template.get("sms_notice")));
        n.setOaNotice(toJson(template.get("oa_notice")));
        n.setMnpNotice(toJson(template.get("mnp_notice")));
        n.setUpdateTime(System.currentTimeMillis() / 1000);
        noticeSettingMapper.updateById(n);
    }

    private Map<String, Object> parseJson(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            String normalized = "[]".equals(raw.trim()) ? "{}" : raw;
            return OBJECT_MAPPER.readValue(normalized, MAP_REF);
        } catch (Exception e) {
            log.warn("解析通知配置 JSON 失败: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private boolean isEnabled(Map<String, Object> notice) {
        if (notice == null || notice.isEmpty()) {
            return false;
        }
        Object status = notice.get("status");
        return status != null && "1".equals(status.toString());
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return "{}";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException("通知配置序列化失败: " + e.getMessage(), e);
        }
    }
}
