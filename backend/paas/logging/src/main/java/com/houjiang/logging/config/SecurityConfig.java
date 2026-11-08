package com.houjiang.logging.config;

import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.filter.JwtAuthenticationTokenFilter;
import com.houjiang.logging.entity.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Spring Security 配置类
 *
 * <p>统一走 commons 的 JWT 过滤器；白名单仅 Swagger / actuator。</p>
 * @author Amos
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RedisCache redisCache;
    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public JwtAuthenticationTokenFilter<LoginUser> jwtAuthenticationTokenFilter() {
        JwtAuthenticationTokenFilter<LoginUser> filter = new JwtAuthenticationTokenFilter<>(
                LoginUser.class,
                "/actuator/**",
                "/swagger-ui/**",
                "/v3/api-docs/**"
        );
        filter.setRedisCache(redisCache);
        filter.setResolver(handlerExceptionResolver);
        return filter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
