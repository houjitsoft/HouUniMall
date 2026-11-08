package com.houjiang.commons.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Amos
 */
@Component
public class RedisCache
{
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value)
    {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     * @param timeout 时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit)
    {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout)
    {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit)
    {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key)
    {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public boolean deleteObject(final String key)
    {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    public long deleteObject(final Collection collection)
    {
        return redisTemplate.delete(collection);
    }

    /**
     * 缓存List数据
     *
     * @param key 缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList)
    {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key)
    {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存Set
     *
     * @param key 缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet)
    {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext())
        {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(final String key)
    {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap)
    {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key)
    {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value)
    {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey)
    {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 删除Hash中的数据
     *
     * @param key
     * @param hkey
     */
    public void delCacheMapValue(final String key, final String hkey)
    {
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(key, hkey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys)
    {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern)
    {
        return redisTemplate.keys(pattern);
    }

    /**
     * 判断Key是否存在
     *
     * @param key Redis键
     * @return true=存在；false=不存在
     */
    public boolean hasKey(final String key)
    {
        return redisTemplate.hasKey(key);
    }

    /**
     * 列表左推入（LPUSH）
     * 功能：将多个值从左侧推入列表
     * 参数：
     *  - key 列表键
     *  - values 可变参数，推入的多个字符串值
     * 返回：
     *  - Long 推入后列表的长度
     */
    public Long lpush(final String key, final String... values)
    {
        return redisTemplate.opsForList().leftPushAll(key, Arrays.asList(values));
    }

    /**
     * 列表左推入并设置过期时间
     * 功能：从左侧推入多个值，并设置过期秒数
     * 参数：
     *  - key 列表键
     *  - seconds 过期时间（秒）
     *  - values 推入的多个值
     * 返回：
     *  - Long 推入后列表的长度
     */
    public Long lpush(final String key, final Integer seconds, final String... values)
    {
        Long len = redisTemplate.opsForList().leftPushAll(key, Arrays.asList(values));
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
        return len;
    }

    /**
     * 列表移除指定值（LREM）
     * 功能：从列表中移除指定数量的值
     * 参数：
     *  - key 列表键
     *  - count 移除个数（>0从头到尾，<0从尾到头，=0移除所有匹配）
     *  - value 要移除的值
     * 返回：
     *  - Long 实际移除的数量
     */
    public Long lrem(final String key, final long count, final String value)
    {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    /**
     * 集合添加成员（SADD）
     * 功能：向集合添加一个成员，并设置过期时间
     * 参数：
     *  - key 集合键
     *  - value 成员值
     *  - seconds 过期时间（秒）
     * 返回：
     *  - Long 新增的成员数量（0或1）
     */
    public Long sadd(final String key, final String value, final int seconds)
    {
        Long added = redisTemplate.opsForSet().add(key, value);
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
        return added;
    }

    /**
     * 数值自增（INCR）
     * 功能：将字符串值按数值自增1
     * 参数：
     *  - key 键
     * 返回：
     *  - Long 自增后的值
     */
    public Long increment(final String key)
    {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 数值自减（DECR）
     * 功能：将字符串值按数值自减1
     * 参数：
     *  - key 键
     * 返回：
     *  - Long 自减后的值
     */
    public Long decrement(final String key)
    {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 获取key的剩余过期时间
     * 功能：获取key的剩余过期时间（秒）
     * 参数：
     *  - key Redis键
     * 返回：
     *  - Long 剩余过期时间（秒），-1表示永不过期，-2表示key不存在
     */
    public Long getExpire(final String key)
    {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
}


