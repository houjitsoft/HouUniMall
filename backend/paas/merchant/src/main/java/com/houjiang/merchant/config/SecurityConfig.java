package com.houjiang.merchant.config;

import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.filter.JwtAuthenticationTokenFilter;
import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Merchant服务 - Security安全配置
 * 统一使用commons模块的JWT认证
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
 * @author Amos
 * @since 2026-04-28
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final RedisCache redisCache;
    private final @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver;
    private final PlatformFinanceServiceAuthenticationFilter platformFinanceServiceAuthenticationFilter;

    /**
     * 创建JWT认证过滤器Bean
     * 使用commons提供的统一过滤器
     * 
     * 【重要】统一使用UserInfoVO类型，与其他PAAS服务保持一致
     * 【验证规则】除白名单外，所有请求都验证token
     * 
     * @return JwtAuthenticationTokenFilter JWT认证过滤器实例
     */
    @Bean
    public JwtAuthenticationTokenFilter<UserInfoVO> jwtAuthenticationTokenFilter() {
        JwtAuthenticationTokenFilter<UserInfoVO> filter = new JwtAuthenticationTokenFilter<>(
            UserInfoVO.class,
            // 登录接口白名单
            "/merchant/login/login",
            // 退出接口白名单
            "/merchant/login/out",
            // 公共配置接口白名单
            "/merchant/config/getConfig",
            // 公众号回调白名单
            "/adminapi/channel/oa/callback/**",
            // 健康检查端点白名单
            "/actuator/**",
            // 服务间内部接口（Feign，无用户 JWT）
            "/merchant/internal/**"
        );
        // 手动注入依赖（必须）
        filter.setRedisCache(redisCache);
        filter.setResolver(handlerExceptionResolver);
        return filter;
    }

    /**
     * 密码编码器
     * 
     * @return PasswordEncoder BCrypt密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AdvertisingLifecycleInternalTokenFilter advertisingLifecycleInternalTokenFilter(
            @org.springframework.beans.factory.annotation.Value("${houcloud.plugin.internal-job-token:}")
            String internalToken) {
        return new AdvertisingLifecycleInternalTokenFilter(internalToken);
    }

    /**
     * 配置安全过滤链
     * 使用commons的JwtAuthenticationTokenFilter进行统一认证
     * 
     * @param http HttpSecurity配置对象
     * @return SecurityFilterChain 安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AdvertisingLifecycleInternalTokenFilter advertisingLifecycleInternalTokenFilter) throws Exception {
        http
            // 禁用CSRF保护，适用于API服务
            .csrf(csrf -> csrf.disable())
            // 禁用HTTP Basic认证
            .httpBasic(httpBasic -> httpBasic.disable())
            // 禁用表单登录
            .formLogin(form -> form.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 配置请求授权
            .authorizeHttpRequests(authz -> authz
                // 允许登录和退出接口
                .requestMatchers("/merchant/login/login", "/merchant/login/out").permitAll()
                // 允许公共配置接口
                .requestMatchers("/merchant/config/getConfig").permitAll()
                // 允许公众号回调接口
                .requestMatchers("/adminapi/channel/oa/callback/**").permitAll()
                // 允许Swagger文档
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 允许健康检查
                .requestMatchers("/actuator/**").permitAll()
                // 允许服务间内部接口
                .requestMatchers("/merchant/internal/**").permitAll()
                // 所有其他请求都需要通过JWT过滤器验证
                .anyRequest().authenticated()
            )
            // 添加commons的JWT认证过滤器
            .addFilterBefore(
                jwtAuthenticationTokenFilter(), 
                UsernamePasswordAuthenticationFilter.class
            )
            .addFilterAfter(
                platformFinanceServiceAuthenticationFilter,
                JwtAuthenticationTokenFilter.class
            )
            .addFilterBefore(
                advertisingLifecycleInternalTokenFilter,
                JwtAuthenticationTokenFilter.class
            );
        
        return http.build();
    }
}
