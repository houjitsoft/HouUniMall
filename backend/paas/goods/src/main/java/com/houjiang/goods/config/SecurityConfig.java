package com.houjiang.goods.config;

import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.filter.JwtAuthenticationTokenFilter;
import com.houjiang.goods.entity.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Security安全配置
 * 集成commons模块的统一JWT认证
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
 * 【关键点】：
 * - 不要手动验证token，JWT过滤器已经处理
 * - 不要手动调用ParseTokenUtils，使用SecurityContextUtils
 * - 如果出现"用户未登录"错误，检查：
 *   1. BFF层是否正确传递Authorization头
 *   2. Redis中是否存在对应的token（可能已过期）
 *   3. JWT过滤器是否正确配置和注入
 * 
 * @author Amos
 * @date 2026-01-01
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RedisCache redisCache;
    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;

    /**
     * 配置安全过滤链
     * 使用commons的JwtAuthenticationTokenFilter进行统一认证
     * 
     * 【重要】所有请求都通过JWT过滤器验证，SecurityFilterChain配置为permitAll
     * JWT过滤器会处理token验证，如果token无效会返回"用户未登录"错误
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（前后端分离架构不需要）
            .csrf(csrf -> csrf.disable())
            // 禁用Session（使用JWT无状态认证）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 配置认证规则
            // 【重要】所有请求都放行，由JWT过滤器统一处理token验证
            // JWT过滤器会检查token，如果无效会返回"用户未登录"错误
            .authorizeHttpRequests(authorize -> authorize
                // 健康检查端点放行
                .requestMatchers("/actuator/**").permitAll()
                // 所有其他请求都放行，由JWT过滤器处理验证
                .anyRequest().permitAll()
            )
            // 添加commons的JWT认证过滤器
            // JWT过滤器会验证token，如果token无效会返回"用户未登录"错误
            .addFilterBefore(
                jwtAuthenticationTokenFilter(), 
                UsernamePasswordAuthenticationFilter.class
            );
        
        return http.build();
    }

    /**
     * 创建JWT认证过滤器Bean
     * 使用commons提供的统一过滤器
     * 
     * 【重要】统一使用LoginUser类型，与其他PAAS服务保持一致
     * 【验证规则】除健康检查外，所有请求都验证token
     */
    @Bean
    public JwtAuthenticationTokenFilter<LoginUser> jwtAuthenticationTokenFilter() {
        // 统一使用commons的JWT过滤器，所有请求都通过JWT验证
        // 必须手动注入RedisCache和HandlerExceptionResolver依赖
        JwtAuthenticationTokenFilter<LoginUser> filter = new JwtAuthenticationTokenFilter<>(
            LoginUser.class,
            "/actuator/**",
            // Mall 服务内部 Feign 调用，鉴权在 mall 层（MALL_ADMIN_TOKEN）
            "/mall/goods/**",
            // Stage 3：订单/兼容 mall 的可售库存生命周期 Feign（鉴权在调用方；支付回调等无用户态）
            "/goods/storage/stock/lifecycle/**",
            // 商品详情页可售区域查询（C 端公开）
            "/goods/base/transport/area/query",
            // C 端商品列表 / 搜索结果页：游客也要能浏览（进白名单后不做 JWT 校验）。
            // 会员价不丢：前端把 token 同时放进了请求体（request.js 的 token 字段），
            // searchGoodsData 读 param.getToken() 解析 PLUS 折扣，不依赖 SecurityContext。
            "/es/searchGoodsData",
            // C 端商品列表的分类/品牌/属性筛选项（分类条与侧滑筛选面板）
            "/goods/cat/query/filter",
            // 入驻申请页「主营类目」类目树：申请阶段用户还没有 shopId，会话是 joinOnly，
            // 拿不到商家登录态会被拦成 30802。这里返回的是平台全量类目树（queryAllCat
            // 只用 Redis 缓存 + selectAllCat2，不用 shopId、不读店铺私有数据），
            // 与上面已放行的 /goods/cat/query/filter 同源同敏感级，故一并放行只读查询。
            "/goods/cat/cat/query",
            // base（插件市场）派发安装用的内部端点：不走用户 JWT，端点内部用
            // X-Internal-Job-Token 自行鉴权（未配置令牌时 fail-closed）
            "/internal/plugin/**"
        );
        // 手动注入依赖
        filter.setRedisCache(redisCache);
        filter.setResolver(handlerExceptionResolver);
        return filter;
    }
}
