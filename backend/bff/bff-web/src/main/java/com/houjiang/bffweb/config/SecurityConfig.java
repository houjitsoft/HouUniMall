package com.houjiang.bffweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * BFF Web Security 配置
/**
 * 
 * 【重要说明】Token验证机制：
 * 1. BFF层（bff-web）不进行JWT验证，所有请求都放行
 * 2. Token通过FeignRequestInterceptor传递给下游PAAS服务
 * 3. 下游PAAS服务（如oa、pm等）统一使用commons的JwtAuthenticationTokenFilter进行验证
 * 4. 如果出现"用户未登录"错误，问题不在BFF层，而在：
 *    - 前端未携带token
 *    - FeignRequestInterceptor未正确传递token
 *    - PAAS服务的JWT过滤器配置问题
 *    - Redis中token已过期
 * 
 * @description 配置 BFF Web 服务的 Spring Security 规则
 * @note bff-web作为网关层，不进行JWT验证，所有请求都放行
 *       认证由下游PAAS服务（如auth服务）处理
 * @author Amos
 * @date 2024-01-01
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // 注意：不在应用层输出 CORS 头。
            // 反向代理（nginx）已统一下发 Access-Control-Allow-Origin: * 并直接应答 OPTIONS 预检，
            // 若 Spring 再回显 Origin 会出现重复的 ACAO 头，浏览器会拒绝读取响应（2026-08-31 实测）。
            // 前端均以请求头携带 token，不使用 cookie 凭证，* 通配安全。
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(requests -> requests
                // Swagger文档放行
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 健康检查端点放行
                .requestMatchers("/actuator/**").permitAll()
                // 登录相关接口放行（无需认证）
                .requestMatchers("/web/auth/login", "/web/auth/codelogin", "/web/auth/register/**", "/web/auth/smscode").permitAll()
                .requestMatchers("/web/user/login", "/web/user/codelogin", "/web/user/smscode", "/web/user/logout").permitAll()
                // 微信登录相关接口放行（无需认证）
                .requestMatchers("/web/auth/wechat/**").permitAll()
                // 统一登录（UniLogin）渠道扫码接口放行：登录页本身处于未登录态
                .requestMatchers("/web/auth/unilogin/**").permitAll()
                // bff-web作为网关层，不进行认证验证，其他请求都放行
                // Token会通过FeignRequestInterceptor传递给下游PAAS服务
                // 下游服务（如auth、userms等）会进行JWT验证
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}