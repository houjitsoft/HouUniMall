package com.houjiang.permission.config;

import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.filter.JwtAuthenticationTokenFilter;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.permission.security.PermissionSecurityExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 权限服务安全配置
 * 统一使用 commons JWT 过滤器验证访问身份。
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] WHITE_LIST = {
        "/actuator/health/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/error"
    };

    private final RedisCache redisCache;
    private final PermissionSecurityExceptionHandler securityExceptionHandler;

    @Bean
    public JwtAuthenticationTokenFilter<UserInfoVO> jwtAuthenticationTokenFilter() {
        JwtAuthenticationTokenFilter<UserInfoVO> filter =
                new JwtAuthenticationTokenFilter<>(UserInfoVO.class, WHITE_LIST);
        filter.setRedisCache(redisCache);
        filter.setResolver(securityExceptionHandler);
        return filter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(WHITE_LIST).permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(securityExceptionHandler)
                .accessDeniedHandler(securityExceptionHandler)
            )
            .addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
