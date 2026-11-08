package com.houjiang.order.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * order 服务内部定时任务（job 服务 Feign 调用）的令牌认证过滤器。
 *
 * <p>背景：{@code AfterSalesJob} 等 Quartz 任务经 {@code @FeignClient("order")} 打
 * {@code /order/**&#47;job/**}（如 {@code /order/return/job/updateTimeOutReturnGoods}）。
 * 这条链路没有用户 Token，会被 {@code JwtAuthenticationTokenFilter} 以
 * 「请求中没有token」拦下并抛 {@code 请先登录后再操作}，导致：
 * 售后超时自动处理全部失效；若调用方是 MQ 消费者，消息还会被反复重投形成毒消息。
 *
 * <p>本过滤器与 GEO 的 {@code GeoInternalJobTokenFilter} 同构：
 * 对命中内部任务路径 <b>且</b> 令牌校验通过的请求注入 {@code ROLE_INTERNAL_JOB} 身份。
 *
 * <p>🔴 与 GEO 的关键差异：GEO 的失败路径由 {@code ProductAccessInterceptor} +
 * 产品授权策略兜底（无身份 → 401），而 <b>order 没有该拦截器</b>。同时
 * {@code JwtAuthenticationTokenFilter} 已把 job 路径写入白名单并<b>直接放行</b>，
 * 因此本过滤器是这条链路上<b>唯一的准入闸门</b>：校验不通过必须由本过滤器
 * 显式拒绝（401），否则等于把 job 端点完全公开（2026-10-07 实测发现）。
 *
 * <p>令牌未配置时同样拒绝并 ERROR，避免配置缺失被静默当成"开放"。
 *
 * <p>令牌配置项 {@code houcloud.plugin.internal-job-token}，生产值由 Nacos 的
 * {@code order.yaml} 与 {@code job.yaml} 提供且必须相同，禁止把真实值提交到 git。
 *
 * @author Amos
 */
@Slf4j
public class OrderInternalJobTokenFilter extends OncePerRequestFilter {

    /** 内部任务令牌请求头，与 job 服务 {@code InternalJobFeignConfiguration} 保持一致。 */
    public static final String HEADER_INTERNAL_JOB_TOKEN = "X-Internal-Job-Token";

    /** 注入的内部任务角色。 */
    static final String ROLE_INTERNAL_JOB = "ROLE_INTERNAL_JOB";

    private static final String PRINCIPAL_INTERNAL_JOB = "internal-job";

    /**
     * 内部任务端点。order 的定时任务接口统一挂在 {@code /order/<domain>/job/**} 下，
     * 用通配符覆盖全部，避免每新增一个 job 端点就要改一次白名单。
     */
    private static final List<String> INTERNAL_JOB_PATH_PATTERNS = List.of(
        "/order/*/job/**",
        "/order/job/**"
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final String expectedToken;

    public OrderInternalJobTokenFilter(String expectedToken) {
        this.expectedToken = expectedToken == null ? "" : expectedToken.trim();
    }

    /** 是否命中内部任务端点。 */
    public static boolean isInternalJobRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String pattern : INTERNAL_JOB_PATH_PATTERNS) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (isInternalJobRequest(request)) {
            if (!StringUtils.hasText(expectedToken)) {
                log.error("ORDER 内部任务令牌未配置（houcloud.plugin.internal-job-token），拒绝内部任务请求: uri={}",
                        request.getRequestURI());
                reject(response, "内部任务令牌未配置");
                return;
            }
            String provided = request.getHeader(HEADER_INTERNAL_JOB_TOKEN);
            if (!StringUtils.hasText(provided) || !tokenMatches(provided.trim(), expectedToken)) {
                log.warn("ORDER 内部任务令牌校验失败，拒绝请求: uri={}, hasToken={}",
                        request.getRequestURI(), StringUtils.hasText(provided));
                reject(response, "无效的内部任务令牌");
                return;
            }
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                    PRINCIPAL_INTERNAL_JOB, null,
                    List.of(new SimpleGrantedAuthority(ROLE_INTERNAL_JOB))));
            log.debug("ORDER 内部任务令牌认证通过: uri={}", request.getRequestURI());
        }
        chain.doFilter(request, response);
    }

    /**
     * 拒绝内部任务请求。本过滤器是 job 链路的唯一准入闸门（JWT 过滤器已放行、
     * order 无 ProductAccessInterceptor 兜底），因此校验失败必须在此终止链路。
     *
     * <p>响应体沿用 commons 的「未登录」业务码语义，避免向调用方泄露内部端点的存在。
     */
    private void reject(HttpServletResponse response, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
            "{\"code\":30802,\"message\":\"" + reason + "\",\"data\":null}");
    }

    /** 常量时间比较，避免令牌比较的时序侧信道。 */
    private boolean tokenMatches(String provided, String expected) {
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }
}
