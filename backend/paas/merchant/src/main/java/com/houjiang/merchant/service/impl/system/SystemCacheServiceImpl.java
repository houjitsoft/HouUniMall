package com.houjiang.merchant.service.impl.system;

import com.houjiang.merchant.service.system.ISystemCacheService;
import com.houjiang.merchant.common.cache.ConfigCache;
import org.springframework.stereotype.Service;

/**
 * 系统缓存实现类
 * @author Amos
 */
@Service
public class SystemCacheServiceImpl implements ISystemCacheService {

    /**
     * 清除系统缓存
     */
    @Override
    public void clear() {
        ConfigCache.clear();
    }


}
