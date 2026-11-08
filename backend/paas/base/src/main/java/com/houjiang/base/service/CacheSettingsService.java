package com.houjiang.base.service;

import com.houjiang.commons.dto.cache.CacheSettingsDTO;
import com.houjiang.commons.util.ResponseResult;

/**
 * 缓存设置（运行时 + 持久化）
 * @author Amos
 */
public interface CacheSettingsService {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    ResponseResult getSettings();

    /**
     * TODO 补充方法说明
     * @param settings TODO
     * @return TODO
     */
    ResponseResult saveSettings(CacheSettingsDTO settings);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    ResponseResult testConnection();
}
