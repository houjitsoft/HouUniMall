package com.houjiang.thirdparty.config;

import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.filter.JwtAuthenticationTokenFilter;
import com.houjiang.thirdparty.entity.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Security安全配置
 * 集成commons模块的统一JWT认证
 * 
 * 【重要说明】Token验证机制：
 * 1. 所有请求必须携带Authorization头（格式：Bearer {token}）
 * 2. Token由BFF层（bff-web）通过FeignRequestInterceptor传递过来
 * 3. 本服务使用commons的JwtAuthenticationTokenFilter统一验证token
 * 4. JWT过滤器会：
 *    - 从请求头提取Authorization
 *    - 解析JWT token获取用户ID
 *    - 从Redis中查询用户信息（key: HOU_USER_TOKEN_USER + token）
 *    - 将用户信息设置到SecurityContextHolder
 * 5. Controller中通过SecurityContextUtils.getCurrentUser()获取当前用户
 * 
 * 【关键点】：
 * - 不要手动验证token，JWT过滤器已经处理
 * - 不要手动调用ParseTokenUtils，使用SecurityContextUtils
 * - 如果出现"用户未登录"错误，检查：
 *   1. BFF层是否正确传递Authorization头
 *   2. Redis中是否存在对应的token（可能已过期）
 *   3. JWT过滤器是否正确配置和注入
 * 
 * @author Amos
 * @date 2026-01-07
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RedisCache redisCache;
    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;

    /**
     * 企微开放 API：OA 等服务间调用，使用 Corp 凭证，不经过 JWT 过滤器（避免 Feign 无 Authorization 时报 11006）。
     */
    @Bean
    @Order(1)
    public SecurityFilterChain wecomOpenSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/wecom/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * 配置安全过滤链
     * 使用commons的JwtAuthenticationTokenFilter进行统一认证
     * 
     * 【重要】所有请求都通过JWT过滤器验证，SecurityFilterChain配置为permitAll
     * JWT过滤器会处理token验证，如果token无效会返回"用户未登录"错误
     */
    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（前后端分离架构不需要）
            .csrf(csrf -> csrf.disable())
            // 禁用Session（使用JWT无状态认证）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 配置认证规则
            // 【重要】所有请求都放行，由JWT过滤器统一处理token验证
            // JWT过滤器会检查token，如果无效会返回"用户未登录"错误
            .authorizeHttpRequests(authorize -> authorize
                // 消息接口由 JWT 过滤器区分用户数据面与内部发送端点
                .requestMatchers("/message/**").permitAll()
                // 短信接口放行
                .requestMatchers("/sms/**").permitAll()
                // OSS接口放行（Controller 在 /web/oss/*，须与白名单一致）
                .requestMatchers("/oss/**", "/web/oss/**").permitAll()
                // MinIO接口放行
                .requestMatchers("/minio/**").permitAll()
                // 翻译接口放行（历史 /trans 与现用 /baidu）
                .requestMatchers("/trans/**", "/baidu/**").permitAll()
                // 健康检查端点放行
                .requestMatchers("/actuator/**").permitAll()
                // 所有其他请求都放行，由JWT过滤器处理验证
                .anyRequest().permitAll()
            )
            // 添加commons的JWT认证过滤器
            // JWT过滤器会验证token，如果token无效会返回"用户未登录"错误
            .addFilterBefore(
                jwtAuthenticationTokenFilter(), 
                UsernamePasswordAuthenticationFilter.class
            );
        
        return http.build();
    }

    /**
     * 创建JWT认证过滤器Bean
     * 使用commons提供的统一过滤器
     * 
     * 【重要】统一使用LoginUser类型，与其他PAAS服务保持一致
     * 【验证规则】除白名单外，所有请求都验证token
     */
    @Bean
    public JwtAuthenticationTokenFilter<LoginUser> jwtAuthenticationTokenFilter() {
        // 统一使用commons的JWT过滤器，所有请求都通过JWT验证
        // 必须手动注入RedisCache和HandlerExceptionResolver依赖
        JwtAuthenticationTokenFilter<LoginUser> filter = new JwtAuthenticationTokenFilter<>(
            LoginUser.class,
            "/message/send/**", "/sms/**", "/oss/**", "/web/oss/**", "/minio/**", "/web/minio/**",
            "/trans/**", "/baidu/**", "/actuator/**",
            // OA 等服务间调用企微 API（Corp 凭证，勿要求用户 JWT）
            "/api/wecom/**",
            // 统一登录渠道凭据：登录页处于未登录态，auth 服务需在无 JWT 时读取。
            // 暴露面已收窄——只读、只覆盖登录类平台，且不返回批量数据（见 LoginChannelConfigController）
            "/api/login-channel/**"
        );
        // 手动注入依赖
        filter.setRedisCache(redisCache);
        filter.setResolver(handlerExceptionResolver);
        return filter;
    }
}
