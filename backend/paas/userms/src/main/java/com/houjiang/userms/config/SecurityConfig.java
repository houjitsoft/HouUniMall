package com.houjiang.userms.config;

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
        // 创建filter实例，传入登录用户类型和白名单路径
        JwtAuthenticationTokenFilter<UserInfoVO> filter = new JwtAuthenticationTokenFilter<>(
            UserInfoVO.class,
            // 健康检查端点白名单
            "/actuator/**",
            // 由专用内部令牌过滤器校验，不能用普通会员 JWT 代替。
            OAuthInternalTokenFilter.PATH,
            // 用户注册接口（公开接口）
            "/user/ms/register",
            // 忘记密码（GEO/前台无 token）
            "/user/ms/forgot-password",
            // 忘记密码兼容旧路径
            "/user/ms/forgotPassWord",
            // 手机号查重（注册流程无token）
            "/user/ms/checkUserMobileExist",
            // 手机号查重（注册流程无token）
            "/user/ms/checkPhoneNumberExist",
            // 根据手机号查询用户信息（用于登录流程）
            "/user/ms/infoByMobile",
            // 根据参数查询用户信息（用于登录流程）
            "/user/ms/querUserInfoByParams",
            // 根据参数查询商家信息（用于登录流程）
            "/user/businessInfo/infoByMap",
            // 查询用户积分记录（用于登录流程）
            "/user/pointsLog/infoByMap",
            // 插入积分记录（用于登录流程）
            "/user/pointsLog/insertByMap",
            // 更新会员积分（用于登录流程）
            "/user/ms/updateMemberIntegral",
            // 更新登录时间和次数（用于登录流程）
            "/user/ms/updateLoginTimeCount",
            // 更新用户日活跃度（用于登录流程）
            "/user/liveStatistics/insertUserDayLive",
            // 登录成功后 auth 补全会话 orgList（此时尚无用户 JWT）
            "/user/ms/organization/getUserOrganizations",
            // base（插件市场）派发安装用的内部端点：不走用户 JWT，端点内部用
            // X-Internal-Job-Token 自行鉴权（未配置令牌时 fail-closed）
            "/internal/plugin/**"
        );
        // 手动注入依赖
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
    public SecurityFilterChain filterChain(HttpSecurity http,
            @org.springframework.beans.factory.annotation.Value("${unilogin.internal-token:}") String oauthToken) throws Exception {
        http
            // 禁用CSRF（前后端分离架构不需要）
            .csrf(csrf -> csrf.disable())
            // 禁用Session（使用JWT无状态认证）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 配置认证规则
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(OAuthInternalTokenFilter.PATH).hasRole("INTERNAL_OAUTH")
                // Swagger文档放行
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 健康检查端点放行
                .requestMatchers("/actuator/**").permitAll()
                // 插件市场派发安装端点：跳过 Spring 的用户 JWT，准入由端点内的
                // X-Internal-Job-Token 校验保证（未配置令牌时 fail-closed）
                .requestMatchers("/internal/plugin/**").permitAll()
                // 用户注册/忘记密码接口放行（公开接口，无 token）
                .requestMatchers("/user/ms/register", "/user/ms/forgot-password", "/user/ms/forgotPassWord").permitAll()
                // 手机号查重接口放行（注册流程无 token 场景使用）
                .requestMatchers("/user/ms/checkUserMobileExist", "/user/ms/checkPhoneNumberExist").permitAll()
                // 登录相关接口放行（用于服务间调用）
                .requestMatchers("/user/ms/infoByMobile", "/user/ms/querUserInfoByParams", "/user/businessInfo/infoByMap", 
                    "/user/pointsLog/infoByMap", "/user/pointsLog/insertByMap", 
                    "/user/ms/updateMemberIntegral", "/user/ms/updateLoginTimeCount", 
                    "/user/liveStatistics/insertUserDayLive",
                    // auth 登录补全会话组织：调用时还没有 Bearer，必须放行
                    "/user/ms/organization/getUserOrganizations").permitAll()
                // 其他请求都需要认证（由JWT filter处理）
                .anyRequest().authenticated()
            )
            // 添加commons的JWT认证过滤器
            .addFilterBefore(
                jwtAuthenticationTokenFilter(), 
                UsernamePasswordAuthenticationFilter.class
            )
            .addFilterBefore(new OAuthInternalTokenFilter(oauthToken), JwtAuthenticationTokenFilter.class);
        
        return http.build();
    }
}
