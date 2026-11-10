package com.houjiang.bffweb.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/** 兼容现有 Nacos redis.yaml 的 spring.redis.*，不导入 commons 的数据库配置。 */
@Configuration(proxyBeanMethods = false)
public class BugFeedbackRedisConfig {
    @Bean("bugFeedbackRedisConnectionFactory")
    public LettuceConnectionFactory bugFeedbackRedisConnectionFactory(Environment environment) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                property(environment, "host", "127.0.0.1"), Integer.parseInt(property(environment, "port", "6379")));
        config.setDatabase(Integer.parseInt(property(environment, "database", "0")));
        String password = property(environment, "password", "");
        if (!password.isEmpty()) config.setPassword(password);
        String username = property(environment, "username", "");
        if (!username.isEmpty()) config.setUsername(username);
        Duration timeout = environment.getProperty("spring.redis.timeout", Duration.class,
                environment.getProperty("spring.data.redis.timeout", Duration.class, Duration.ofSeconds(3)));
        var client = LettuceClientConfiguration.builder().commandTimeout(timeout).shutdownTimeout(Duration.ofSeconds(2));
        if (Boolean.parseBoolean(property(environment, "ssl.enabled", "false"))) client.useSsl();
        return new LettuceConnectionFactory(config, client.build());
    }

    @Bean("bugFeedbackRedisTemplate")
    public StringRedisTemplate bugFeedbackRedisTemplate(
            @Qualifier("bugFeedbackRedisConnectionFactory") LettuceConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    private String property(Environment environment, String name, String fallback) {
        return environment.getProperty("spring.redis." + name,
                environment.getProperty("spring.data.redis." + name, fallback));
    }
}
