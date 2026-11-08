package com.houjiang.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Base服务安全配置
 * 内部服务调用，允许所有请求
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain pluginCenterFilterChain(HttpSecurity http, RedisTemplate<String, Object> redis) throws Exception {
        return http.securityMatcher("/base/plugin-center/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PluginCenterSessionFilter::isCredentialEndpoint).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new PluginCenterSessionFilter(redis), UsernamePasswordAuthenticationFilter.class)
                .httpBasic(basic -> basic.disable()).formLogin(form -> form.disable()).build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // 允许Swagger文档
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 允许健康检查
                .requestMatchers("/actuator/**").permitAll()
                // 允许所有请求，因为这是内部服务调用
                .anyRequest().permitAll()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable());
        
        return http.build();
    }
}
