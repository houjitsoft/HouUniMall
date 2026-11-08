package com.houjiang.payment.config;

import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.filter.JwtAuthenticationTokenFilter;
import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.RequiredArgsConstructor;
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
 * Security安全配置
 * 集成commons模块的统一JWT认证
 * 参考PM服务的标准架构模式
 * 
 * @author Amos
 * @date 2026-01-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final RedisCache redisCache;
    private final HandlerExceptionResolver handlerExceptionResolver;

    /**
     * 创建JWT认证过滤器Bean
     * 使用commons提供的统一过滤器
     */
    @Bean
    public JwtAuthenticationTokenFilter<UserInfoVO> jwtAuthenticationTokenFilter() {
        JwtAuthenticationTokenFilter<UserInfoVO> filter = new JwtAuthenticationTokenFilter<>(
            UserInfoVO.class,
            "/actuator/**",
            "/api/payment/callback/**",
            "/stripe/stripeNotifyUrl"
        );
        filter.setRedisCache(redisCache);
        filter.setResolver(handlerExceptionResolver);
        return filter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置安全过滤链
     * 使用commons的JwtAuthenticationTokenFilter进行统一认证
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/payment/callback/**").permitAll()
                .requestMatchers("/stripe/stripeNotifyUrl").permitAll()
                .requestMatchers("/plugin/ums-corp-order/notify").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                jwtAuthenticationTokenFilter(), 
                UsernamePasswordAuthenticationFilter.class
            );
        
        return http.build();
    }
}
