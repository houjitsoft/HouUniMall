package com.houjiang.base.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.houjiang.base.entity.BaseSensitiveConfigEntity;
import com.houjiang.base.mapper.BaseSensitiveConfigMapper;
import com.houjiang.base.mapper.BaseSensitiveMapper;
import com.houjiang.base.service.impl.BaseSensitiveServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Amos
 */
@Component
public class MyApplicationRunner implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(MyApplicationRunner.class);

    @Autowired
    private BaseSensitiveConfigMapper baseSensitiveConfigMapper;

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private BaseSensitiveServiceImpl baseSensitiveService;
    
    @Autowired
    private Environment environment;
    
    @Override
    public void run(ApplicationArguments args) throws Exception{
        LOG.info("=== 开始加载redis配置");

        // 使用线程池执行器执行Redis配置加载逻辑，以避免阻塞应用启动过程
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数设置为1
        executor.setCorePoolSize(1);
        // 最大线程数设置为1
        executor.setMaxPoolSize(1);
        // 队列容量设置为0，强制使用线程池执行
        executor.setQueueCapacity(0);
        // 线程名称前缀
        executor.setThreadNamePrefix("RedisConfigLoader-");
        executor.initialize();

        Future<?> future = executor.submit(() -> {
            try {
                // 从环境变量中获取Redis配置
                String redisHost = environment.getProperty("spring.redis.host", "localhost");
                Integer redisPort = environment.getProperty("spring.redis.port", Integer.class, 6379);
                String redisPassword = environment.getProperty("spring.redis.password", "");
                String redisDatabase = environment.getProperty("spring.redis.database", "0");
                
                // 初始化Redis连接
                // Deleted: RedisUtil.initialPool2(redisHost, redisPort, redisPassword, redisDatabase);
                LOG.info("=== redis配置加载完成");
            } catch (Exception e) {
                // 记录加载失败的日志
                LOG.error("加载redis配置失败", e);
            }
        });
        try {
            // 等待异步任务完成
            future.get();
        } catch (Exception e) {
            LOG.error("异步加载redis配置过程中出现异常", e);
        } finally {
            // 关闭线程池
            executor.shutdown();

            // 等待线程池优雅关闭，超时后强制关闭
            try {
                if (!executor.getThreadPoolExecutor().awaitTermination(RedisUtil.EXRP_FIVE_MIN, TimeUnit.SECONDS)) {
                    // 强制关闭
                    executor.getThreadPoolExecutor().shutdownNow();
                }
            } catch (InterruptedException ie) {
                LOG.error("线程池关闭时被中断", ie);
                // 重新设置中断状态
                Thread.currentThread().interrupt();
            }
        }
        
        LOG.info("=== 开始加载敏感词配置 ===");
        BaseSensitiveConfigEntity baseSensitiveConfigEntity = baseSensitiveConfigMapper.selectOne(null);
        String str = JSONObject.toJSONString(baseSensitiveConfigEntity);
        // 读侧 SensitiveCheckAspect 用 RedisUtil(Jedis 字符串)读取，
        // 若用 redisCache 写入会给字符串多包一层引号导致 fastjson 解析失败，这里保持读写一致
        RedisUtil.set(HouConfig.HOU_SENSITIVE_CONFIG, str);
        baseSensitiveService.synSensitiveConfig();
    }
}
