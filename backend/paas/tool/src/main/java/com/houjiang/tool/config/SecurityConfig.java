package com.houjiang.tool.config;

import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.filter.JwtAuthenticationTokenFilter;
import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * tool 宿主安全配置：复用 commons 的统一 JWT 认证。
 * 所有请求（含 /plugin/** 插件路由）都需登录态；插件路由由 PluginDispatcher 强制校验，
 * 未登录返回 401。仅健康检查与 Swagger 文档放行。
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RedisCache redisCache;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public JwtAuthenticationTokenFilter<UserInfoVO> jwtAuthenticationTokenFilter() {
        JwtAuthenticationTokenFilter<UserInfoVO> filter = new JwtAuthenticationTokenFilter<>(
            UserInfoVO.class,
            // 健康检查端点白名单
            "/actuator/**",
            // Swagger 文档
            "/swagger-ui/**", "/v3/api-docs/**",
            // base（插件市场）派发安装用的内部端点：不走用户 JWT，端点内部用
            // X-Internal-Job-Token 自行鉴权（未配置令牌时 fail-closed）
            "/internal/plugin/**"
        );
        filter.setRedisCache(redisCache);
        filter.setResolver(handlerExceptionResolver);
        return filter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authorize -> authorize
                // /internal/plugin/** 是 base（插件市场）派发安装用的内部端点，不走用户 JWT；
                // 端点内部用 X-Internal-Job-Token 自行鉴权（未配置令牌时 fail-closed）。
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**", "/internal/plugin/**").permitAll()
                // 其余一律需认证（JWT 过滤器解析用户；插件 /plugin/** 亦走此链）
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                jwtAuthenticationTokenFilter(),
                UsernamePasswordAuthenticationFilter.class
            );
        return http.build();
    }
}
