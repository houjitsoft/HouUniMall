package com.houjiang.auth.config;

import com.houjiang.auth.entity.LoginUser;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.filter.JwtAuthenticationTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.houjiang.commons.util.HouBCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName SecurityConfig
 * @Description
 * @Author Amos
 * @Date 2023-06-13 09:05:20
 * @Version 1.0
 * @author Amos
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final RedisCache redisCache;
    private final @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public JwtAuthenticationTokenFilter<LoginUser> jwtAuthenticationTokenFilter() {
        JwtAuthenticationTokenFilter<LoginUser> filter = new JwtAuthenticationTokenFilter<>(LoginUser.class,
            "/internal/session/identity",
            "/user/login", "/user/third-login", "/user/codelogin", "/register/",
            // 图片验证码在登录前生成，必须允许匿名访问；否则 permitAll 还未生效就会被 JWT 过滤器拦截。
            "/platform/userinfo/", "/shop/getShopVerify", "/shop/getSupplierVerify", "/getVerify",
            // 滑动拼图验证码（登录验证2），登录前匿名访问
            "/generate", "/verifyGenerateCaptcha",
            "/weixin/", "/weixinShopApp/", "/rider/login", "/rider/logout",
            // 微信登录相关接口
            "/auth/wechat/", "/auth/wechat/**",
            // 统一登录（UniLogin）渠道扫码接口，登录前匿名访问
            "/auth/unilogin/", "/auth/unilogin/**",
            "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**"
        );
        filter.setRedisCache(redisCache);
        filter.setResolver(handlerExceptionResolver);
        return filter;
    }

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new HouBCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 禁用 CSRF 保护（如果你确定不需要 CSRF 保护）
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests((requests) -> requests
                        // 允许所有用户访问主页
                        .requestMatchers( "/user/login", "/user/third-login","/user/codelogin", "/platform/userinfo/*", "/shop/getShopVerify", "/shop/getSupplierVerify", "/getVerify","/weixin/*","/weixinShopApp/*", "/register/sjcheckphoneandsend", "/user/loginOut", "/h5/userLogoff","/rider/login","/rider/logout", "/generate", "/verifyGenerateCaptcha").permitAll()
                        // 微信登录相关接口
                        .requestMatchers("/auth/wechat/**").permitAll()
                        // 统一登录（UniLogin）渠道扫码接口
                        .requestMatchers("/auth/unilogin/**").permitAll()
                        // 允许Swagger文档
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 允许健康检查
                        .requestMatchers("/actuator/**").permitAll()
                        // Identity endpoint verifies the supplied session itself.
                        .requestMatchers("/internal/session/identity").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        // 自定义登录页面
                        .loginPage("/login")
                        .permitAll()
                )
                // 允许所有人登出
                .logout((logout) -> logout.permitAll());
        //把token校验过滤器添加到过滤器链中
        http.addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(configurer -> configurer
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint)
        );


        return http.build();
    }

    /** CORS配置已移至bff-gateway统一处理 */

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService)
            throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        return auth.build();
    }

}



