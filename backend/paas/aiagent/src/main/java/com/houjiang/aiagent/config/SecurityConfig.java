package com.houjiang.aiagent.config;

import com.houjiang.aiagent.handler.AccessDeniedHandlerImpl;
import com.houjiang.aiagent.handler.AuthenticationEntryPointImpl;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.filter.JwtAuthenticationTokenFilter;
import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * aiagent 安全配置。
 *
 * <p>🔴 独立服务必须自带 SecurityConfig（2026-10-27 踩坑）：aiagent 最初没写，
 * 结果整个服务没有任何 JWT 过滤器，所有请求都被默认规则拦成 401，
 * BFF 侧表现为所有 {@code /web/ai/**} 一律返回 22008「服务开小差了」。
 * 排查时别只看业务代码 —— 先确认服务有没有挂上 JWT 过滤器。
 *
 * <p>请求凭证由 BFF 的 Feign 链路透传，本服务只负责解析 JWT 并从 Redis 取会话。
 */
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final RedisCache redisCache;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;
    private final @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public JwtAuthenticationTokenFilter<UserInfoVO> jwtAuthenticationTokenFilter() {
        JwtAuthenticationTokenFilter<UserInfoVO> filter = new JwtAuthenticationTokenFilter<>(UserInfoVO.class,
            "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**");
        filter.setRedisCache(redisCache);
        filter.setResolver(handlerExceptionResolver);
        return filter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationTokenFilter<UserInfoVO> jwtFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // 🔴 SSE / SseEmitter 这类异步响应用的是 ASYNC dispatch 二次进入过滤器链，
                //    而 OncePerRequestFilter（JwtAuthenticationTokenFilter）默认跳过异步派发，
                //    导致第二次进来时 SecurityContext 为空 → AuthorizationDeniedException，
                //    且此时响应已提交，报「Unable to handle the Spring Security Exception
                //    because the response is already committed」，前端只看到「生成失败：closed」。
                //    鉴权在首次同步请求里已经做过，异步回填不再重复校验。
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ASYNC,
                        jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
