package com.houjiang.logging.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.logging.entity.LogSettingEntity;

import java.util.List;

/**
 * 日志设置 Service 接口
 *
 * <p>提供日志配置项的查询与更新功能。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
public interface LogSettingService extends IService<LogSettingEntity> {

    /**
     * 获取所有配置项
     *
     * @return 配置项列表
     */
    List<LogSettingEntity> getAllSettings();

    /**
     * 根据配置键名获取配置项
     *
     * @param key 配置键名
     * @return 配置项实体，不存在时返回 null
     */
    LogSettingEntity getByKey(String key);

    /**
     * 更新配置项的值
     *
     * @param key   配置键名
     * @param value 新的配置值
     * @return 更新是否成功
     */
    boolean updateSetting(String key, String value);
}
