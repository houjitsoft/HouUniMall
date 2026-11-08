package com.houjiang.commons.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * Redis实现分布式锁
 * @author Amos
 */
public class RedisLock {

	private static final Logger logger = LoggerFactory.getLogger(RedisLock.class);

	/**
	 * 获取jedis实例
 	 */
	private Jedis jedis;

	/**
	 * 锁 的key
	 */
	private String lockKey;

	/**
	 * 锁过期时间，防止线程在入锁之后 无限等待
	 */
	private int expireTimeMsg = 10 * 1000;

	/**
	 * 锁等待时间（或者 叫 尝试获得锁的时间），防止线程饥饿
	 */
	private int waitTimeMsg = 10 * 1000;

	/**
	 * 系统时间偏移量5秒，服务器间的系统时间差不可以超过5秒，避免由于时间差造成错误的解锁
	 * 5 * 1000 用毫秒表示
	 */
	private final static int offsetTime = 5 * 1000;

	/**
	 * 默认减去的时间
	 */
	private static final int DEFAULT_ACQUIRY_RESOLUTION_MILLIS = 100;

	/**
	 * 锁状态
	 */
	private volatile boolean lock = false;

	public RedisLock(Jedis jedis, String lockKey) {
		this.jedis = jedis;
		this.lockKey = lockKey + "_lock";
	}

	public RedisLock(Jedis jedis, String lockKey, int waitTimeMsg) {
		this(jedis, lockKey);
		this.waitTimeMsg = waitTimeMsg;
	}

	public RedisLock(Jedis jedis, String lockKey, int waitTimeMsg, int expireTimeMsg) {
		this(jedis, lockKey, waitTimeMsg);
		this.expireTimeMsg = expireTimeMsg;
	}

	/**
	 * 获取 锁 的key
	 * 
	 * @return
	 */
	public String getLockKey() {
		return lockKey;
	}

	/**
	 * 获取 key 对应的value
	 * 
	 * @param key
	 * @return
	 */
	private String get(String key) {
		return jedis.get(key);
	}

	/**
	 * 设置 key value，不存在 key，设置值 成功，返回1；存在key，设置值 失败，返回0
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	private long setNx(String key, String value) {
		return jedis.setnx(key, value);
	}

	/**
	 * 获取旧值，设置 新的 值
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	private String getSet(String key, String value) {
		return jedis.getSet(key, value);
	}

	/**
	 * 获取锁 实现思路: 主要是使用了redis 的setnx命令,缓存了锁 reids缓存的key是锁的key,所有的共享,
	 * value是锁的到期时间(注意:这里把过期时间放在value了,没有时间上设置其超时时间) 执行过程:
	 * 1.通过setnx尝试设置某个key的值,成功(当前没有这个锁)则返回,成功获得锁
	 * 2.锁已经存在则获取锁的到期时间,和当前时间比较,超时的话,则设置新的值
	 * 
	 * @return
	 * @throws InterruptedException
	 */

	public boolean lock() throws InterruptedException {
		// 10 * 1000
		int waitTime = waitTimeMsg;
		// 循环为了多次争夺锁
		while (waitTime >= 0) {
			// 过期时间
			long exquires = System.currentTimeMillis() + expireTimeMsg + 1;
			logger.info(Thread.currentThread().getName() + "尝试获取锁！");
			// 得到了 锁
			if (this.setNx(lockKey, String.valueOf(exquires)) == 1) {
				logger.info(Thread.currentThread().getName() + "获得了锁，锁 过期时间为：" + exquires);
				lock = true;
				return true;
			}
			// 存在原来的锁，就获取原来锁的过期时间
			String lastLockTime = this.get(lockKey);
			// 判断redis中的时间是否为空，获取出的 时间 过期了，则进行下面操作
			if (lastLockTime != null
					&& System.currentTimeMillis() - Long.valueOf(lastLockTime) > (expireTimeMsg + offsetTime)) {
				// 获取上一个锁的过期时间，并设置现在的锁的过期时间（只有一个线程才能获取上一个线程的设置时间，因为jedis.getSet是同步的）
				String oldValue = this.getSet(lockKey, String.valueOf(exquires));
				// 防止误删（覆盖，因为key是相同的）了他人的锁——这里达不到效果，这里值会被覆盖，但是因为相差了很少的时间，所以可以接受
				if (oldValue != null && oldValue.equals(lastLockTime)) {
					// [分布式的情况下]:如果这个时候，多个线程恰好都到了这里，但是只有一个线程的设置值和当前值相同，他才有权利获取锁
					logger.info("------" + Thread.currentThread().getName() + "获得了锁！------");
					lock = true;
					return true;
				}
			}
			// 循环一次减去一次
			waitTime = waitTime - DEFAULT_ACQUIRY_RESOLUTION_MILLIS;
			// 使用随机的等待时间可以一定程度上保证公平性
			Thread.sleep((long) (Math.random() * 100));
		}
		logger.error("--------" + Thread.currentThread().getName() + "获取锁失败！！");
		return false;
	}

	/**
	 * 释放锁
	 */
	public void unLock() {
		// 判断加锁了，才进行删除操作
		if (lock) {
			jedis.del(lockKey);
			logger.info(Thread.currentThread().getName() + "解锁成功！--------------");
			// 恢复默认值
			lock = false;
		}
	}

}