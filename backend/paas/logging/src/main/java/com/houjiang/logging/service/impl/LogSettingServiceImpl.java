package com.houjiang.logging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.logging.entity.LogSettingEntity;
import com.houjiang.logging.mapper.LogSettingMapper;
import com.houjiang.logging.service.LogSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 日志设置 Service 实现类
 *
 * @author Amos
 * @since 2026-04-13
 */
@Slf4j
@Service
public class LogSettingServiceImpl extends ServiceImpl<LogSettingMapper, LogSettingEntity>
        implements LogSettingService {

    /**
     * 获取所有配置项
     *
     * @return 配置项列表
     */
    @Override
    public List<LogSettingEntity> getAllSettings() {
        return list(new QueryWrapper<LogSettingEntity>().orderByAsc("id"));
    }

    /**
     * 根据配置键名获取配置项
     *
     * @param key 配置键名
     * @return 配置项实体，不存在时返回 null
     */
    @Override
    public LogSettingEntity getByKey(String key) {
        return getOne(new QueryWrapper<LogSettingEntity>().eq("setting_key", key));
    }

    /**
     * 更新配置项的值
     *
     * @param key   配置键名
     * @param value 新的配置值
     * @return 更新是否成功
     */
    @Override
    public boolean updateSetting(String key, String value) {
        UpdateWrapper<LogSettingEntity> wrapper = new UpdateWrapper<>();
        wrapper.eq("setting_key", key).set("setting_value", value);
        boolean result = update(wrapper);
        log.info("[LogSettingService] 更新配置 key={} value={} result={}", key, value, result);
        return result;
    }
}
