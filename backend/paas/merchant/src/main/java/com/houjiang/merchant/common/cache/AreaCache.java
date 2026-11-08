package com.houjiang.merchant.common.cache;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.Area;
import com.houjiang.merchant.common.mapper.AreaMapper;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.merchant.common.util.SpringUtils;

import java.util.List;

/**
 * @author Amos
 */
public class AreaCache {

    private static final String PREFIX = "merchant:area_cache_";

    /**
     * 获取 RedisCache 实例
     *
     * @return RedisCache
     */
    private static RedisCache getRedisCache() {
        return SpringUtils.getBean(RedisCache.class);
    }

    public static List<Area> getAreaCacheList(Integer parentId) {
        AreaMapper areaMapper = SpringUtils.getBean(AreaMapper.class);
        String key = PREFIX + "lists:";

        if (parentId != null) {
            key = key + parentId;
        }

        Object cache = getRedisCache().getCacheObject(key);
        if (cache != null) {
            List<Area> result = JSON.parseArray(cache.toString(), Area.class);
            // 从缓存中获取到了数据
            return result;
        }
        // 从数据库中获取数据并更新缓存
        List<Area> areas = areaMapper.selectList(new QueryWrapper<Area>().eq("parent_id", parentId));
        getRedisCache().setCacheObject(key, JSON.toJSONString(areas));
        return areas;
    }

    public static Area getAreaCacheDetail(Integer id) {
        AreaMapper areaMapper = SpringUtils.getBean(AreaMapper.class);
        String key = PREFIX + id;

        Object cache = getRedisCache().getCacheObject(key);
        if (cache != null) {
            Area result = JSON.parseObject(cache.toString(), Area.class);
            // 从缓存中获取到了数据
            return result;
        }
        // 从数据库中获取数据并更新缓存
        Area area = areaMapper.selectOne(new QueryWrapper<Area>().eq("id", id));
        getRedisCache().setCacheObject(key, JSON.toJSONString(area));
        return area;
    }


}
