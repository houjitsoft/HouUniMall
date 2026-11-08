package com.houjiang.order.config;

import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.filter.JwtAuthenticationTokenFilter;
import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
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
     * 内部定时任务端点（job 服务的 Quartz 任务经 Feign 调用）。
     *
     * <p>实际路径形如 {@code /order/return/job/updateTimeOutReturnGoods}，中间带业务模块段。
     * 这条链路没有用户 Token，会先后被两处拦下：
     * <ol>
     *   <li>Spring Security 的 {@code anyRequest().authenticated()} —— 已在
     *       {@code authorizeHttpRequests} 里 permitAll；</li>
     *   <li>{@link JwtAuthenticationTokenFilter} —— 它<b>不读 SecurityContext</b>，
     *       只按自身 {@code whiteListPaths} 判定，白名单外无 token 直接抛 30802。
     *       因此必须把 job 路径一并写入其白名单，否则
     *       {@link OrderInternalJobTokenFilter} 即使校验通过也依然被拒。</li>
     * </ol>
     *
     * <p>🔴 这里逐条精确枚举全路径，<b>不写</b> {@code /order/**} 之类的宽前缀：
     * {@code JwtAuthenticationTokenFilter#isInWhiteList} 对不以 {@code /**} 结尾的条目
     * 会退化为 {@code requestURI.contains(entry)}，若写 {@code /order/} 就等于把 order
     * 的全部接口都跳过用户 JWT 校验，属于严重越权，禁止。
     * 下列条目与 {@code OrderServiceClient} 的 job 端点一一对应。
     *
     * <p>准入安全性由 {@link OrderInternalJobTokenFilter} 的
     * {@code X-Internal-Job-Token} 校验保证：令牌不正确时不注入身份，
     * 请求在后续链路仍按原语义被拒。
     */
    private static final String[] INTERNAL_JOB_PATHS = {
        "/order/base/job/queryNotEvaluatedOrder",
        "/order/goods/job/queryGoodsInfoByjob",
        "/order/goodsEvaluation/job/insertEvaluated",
        "/order/return/job/updateTimeOutReturnGoods",
        "/order/return/job/updateTimeOutReturn",
        "/order/return/job/updateTimeOutbuyReturn",
        "/order/return/job/updatebuyTimeOutReturn",
        "/order/return/job/updatebuyTimeOutReturnGoods",
        "/order/return/platform/job/updateTimeOut"
    };

    /**
     * 内部定时任务令牌过滤器：job 服务的 Quartz 任务经 Feign 打 {@code /order/**&#47;job/**}
     * 时携带 {@code X-Internal-Job-Token}，由本过滤器校验并注入 {@code ROLE_INTERNAL_JOB}。
     * 令牌未配置或校验失败时不注入身份，请求仍会被后续链路按原语义拒绝（401）。
     */
    @Bean
    public OrderInternalJobTokenFilter orderInternalJobTokenFilter(
            @Value("${houcloud.plugin.internal-job-token:}") String internalJobToken) {
        return new OrderInternalJobTokenFilter(internalJobToken);
    }

    /**
     * 创建JWT认证过滤器Bean
     * 使用commons提供的统一过滤器
     */
    @Bean
    public JwtAuthenticationTokenFilter<UserInfoVO> jwtAuthenticationTokenFilter() {
        // 白名单 = 固定公开路径 + 内部定时任务端点（逐条精确枚举，禁止宽前缀）。
        java.util.List<String> whiteList = new java.util.ArrayList<>(java.util.List.of(
            "/actuator/**",
            // 历史订单列表接口会从请求体 token 自行校验身份。
            "/order/base/queryOrders",
            "/order/base/queryOrdersByPC",
            // 商品详情页评论（C 端公开）
            "/order/goodsEvaluation/getComments",
            // BFF 使用服务间令牌读取登录页广告；插件路由自身仍校验 internal caller。
            "/plugin/mall-advertising/public/slots/**",
            "/plugin/mall-advertising/merchant/**",
            // base（插件市场）派发安装用的内部端点：不走用户 JWT，端点内部用
            // X-Internal-Job-Token 自行鉴权（未配置令牌时 fail-closed）。
            "/internal/plugin/**",
            // 满减只读桥：goods/order 内部 Feign 无用户 Token，仅查询生效活动
            "/activity/reductionPolicyBase/selectByGoodsId",
            "/activity/reductionPolicyBase/selectList",
            "/activity/reductionPolicyBase/selectContent"
        ));
        // 内部定时任务端点：无用户 Token，准入由 OrderInternalJobTokenFilter 校验
        // X-Internal-Job-Token 决定（令牌不正确时不注入身份，后续链路照常拒绝）。
        whiteList.addAll(java.util.Arrays.asList(INTERNAL_JOB_PATHS));

        JwtAuthenticationTokenFilter<UserInfoVO> filter = new JwtAuthenticationTokenFilter<>(
            UserInfoVO.class,
            whiteList.toArray(new String[0])
        );
        // 手动注入依赖
        filter.setRedisCache(redisCache);
        filter.setResolver(handlerExceptionResolver);
        return filter;
    }

    @Bean
    public AdvertisingInternalTokenFilter advertisingInternalTokenFilter(
            @Value("${houcloud.plugin.internal-job-token:}") String internalToken) {
        return new AdvertisingInternalTokenFilter(internalToken);
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
                                           AdvertisingInternalTokenFilter advertisingInternalTokenFilter,
                                           OrderInternalJobTokenFilter orderInternalJobTokenFilter)
            throws Exception {
        http
            // 禁用CSRF（前后端分离架构不需要）
            .csrf(csrf -> csrf.disable())
            // 禁用Session（使用JWT无状态认证）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 配置认证规则
            .authorizeHttpRequests(authorize -> authorize
                // Swagger文档放行
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 健康检查端点放行
                .requestMatchers("/actuator/**").permitAll()
                // 历史订单列表接口在 JWT 过滤器中直接放行，因此这里必须保持一致；
                // 两个接口都由业务方法从请求体 token 校验用户身份。
                .requestMatchers("/order/base/queryOrders", "/order/base/queryOrdersByPC").permitAll()
                // 商品详情页评论公开
                .requestMatchers("/order/goodsEvaluation/getComments").permitAll()
                // 仅跳过 Spring 的用户 JWT；内部令牌和插件 caller 白名单仍由两层后续校验。
                .requestMatchers("/plugin/mall-advertising/public/slots/**").permitAll()
                .requestMatchers("/plugin/mall-advertising/merchant/**").permitAll()
                // 内部定时任务端点：跳过 Spring 的用户 JWT，实际准入由
                // OrderInternalJobTokenFilter 校验 X-Internal-Job-Token 决定。
                .requestMatchers(INTERNAL_JOB_PATHS).permitAll()
                // 插件市场派发安装端点：跳过 Spring 的用户 JWT，准入由端点内的
                // X-Internal-Job-Token 校验保证（未配置令牌时 fail-closed）。
                .requestMatchers("/internal/plugin/**").permitAll()
                // 满减只读桥（服务间查询）
                .requestMatchers(
                        "/activity/reductionPolicyBase/selectByGoodsId",
                        "/activity/reductionPolicyBase/selectList",
                        "/activity/reductionPolicyBase/selectContent"
                ).permitAll()
                // 其他请求都需要认证（由JWT filter处理）
                .anyRequest().authenticated()
            )
            // 添加commons的JWT认证过滤器
            .addFilterBefore(
                jwtAuthenticationTokenFilter(), 
                UsernamePasswordAuthenticationFilter.class
            )
            .addFilterBefore(advertisingInternalTokenFilter, JwtAuthenticationTokenFilter.class)
            // 内部任务过滤器必须在 JWT 过滤器之前，先注入内部身份再交给 JWT 过滤器判定
            .addFilterBefore(orderInternalJobTokenFilter, JwtAuthenticationTokenFilter.class);
        
        return http.build();
    }
}
