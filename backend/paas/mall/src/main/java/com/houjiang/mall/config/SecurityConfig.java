package com.houjiang.mall.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Mall 商城服务安全配置：{@link MallAdminTokenAuthenticationFilter} 替代 commons 云 JWT 过滤器。
 *
 * <p>链路顺序：{@link PluginInternalJobTokenFilter} → {@link MallAdminTokenAuthenticationFilter}
 * → Spring Security 其余过滤器。前者只负责 job 服务无用户 Token 调用
 * {@code /plugin/{code}/job/**} 的内部链路，后者仍是用户请求的权威鉴权。
 *
 * @author Amos
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MallAdminTokenAuthenticationFilter mallAdminTokenAuthenticationFilter;
    private final PluginInternalJobTokenFilter pluginInternalJobTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/mall/front/**").permitAll()
                .requestMatchers("/mall/auth/**").permitAll()
                // T11：插件前端产物为公开只读资源（浏览器 import() 无法携带 token），仅 GET 已映射
                .requestMatchers("/plugin-assets/**").permitAll()
                // Stage 3：库存生命周期兼容入口（无用户态 Feign；写方在 goods）
                .requestMatchers("/mall/storage/stock/lifecycle/**").permitAll()
                // 分销订单事件（order → mall）：无用户态，靠 X-Internal-Job-Token 自证
                .requestMatchers("/internal/distribution/**").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/mall/home/data",
                        "/mall/home/banner/list",
                        "/mall/home/notice/list",
                        "/mall/home/recommend/list",
                        "/mall/home/floor/enabled",
                        "/mall/config/get_mall_style").permitAll()
                .anyRequest().authenticated()
            )
            // 内部任务令牌过滤器必须先于用户鉴权过滤器：命中 /plugin/{code}/job/** 且令牌正确时
            // 先注入内部 Authentication，随后由 MallAdminTokenAuthenticationFilter 跳过用户 Token 校验。
            //
            // 注意：addFilterBefore 的第二个参数（锚点）必须是 Spring Security 已在
            // FilterOrderRegistration 中登记顺序的过滤器。MallAdminTokenAuthenticationFilter 是
            // 自定义过滤器、初始没有登记顺序，直接把它当锚点会在 HttpSecurity.build() 时抛
            // 「The Filter class ... does not have a registered order」导致 mall 启动失败。
            // 正确写法：先把用户鉴权过滤器挂到 UsernamePasswordAuthenticationFilter 之前
            // （该调用会为它登记顺序），再以其为锚点把内部任务令牌过滤器插到它之前，
            // 从而稳定得到 PluginInternalJobTokenFilter → MallAdminTokenAuthenticationFilter 的顺序。
            .addFilterBefore(mallAdminTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(pluginInternalJobTokenFilter, MallAdminTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
