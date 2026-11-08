package com.houjiang.mall.config;

import com.houjiang.plugin.engine.core.PluginDispatcher;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * 插件内部定时任务认证过滤器。
 *
 * <p>背景：geo / oa 插件由独立服务改为 embedded 内嵌进 mall 宿主后，Nacos 中
 * 不再存在 {@code geo} / {@code oa} 服务，job 服务的 Quartz 任务改经
 * {@code @FeignClient("mall")} 调 {@code /plugin/{code}/job/feign/**}。这类调用没有
 * 用户 Token，会被 {@link MallAdminTokenAuthenticationFilter} 以「未登录」拒绝，
 * 进而使 {@link com.houjiang.plugin.engine.core.PluginDispatcher} 的
 * {@code permissionEvaluator.isAuthenticated()} 判定失败（401）。
 *
 * <p>本过滤器只解决这一条内部链路：仅对 {@code /plugin/{code}/job/**} 且携带正确
 * {@link #HEADER_INTERNAL_JOB_TOKEN} 的请求注入内部 Authentication，使分发器放行。
 * 约束：
 * <ul>
 *   <li>只影响 {@code /plugin/{code}/job/**}，不改动 {@code /plugin/**} 其他路径的鉴权；</li>
 *   <li>令牌未配置（空白）时不做任何认证，退回宿主既有鉴权链路，避免「无凭据放行」；</li>
 *   <li>令牌错误时不在此处拒绝，交由 {@link MallAdminTokenAuthenticationFilter} 走原有
 *       401 逻辑，语义与改造前一致；</li>
 *   <li>插件未启用时的 423 / 路由不存在时的 404 由分发器原样返回，本过滤器不干预。</li>
 * </ul>
 *
 * <p>令牌配置项 {@code houcloud.plugin.internal-job-token}，生产值由 Nacos 提供，
 * 禁止把真实值提交到 git。
 *
 * @author Amos
 */
@Slf4j
@Component
public class PluginInternalJobTokenFilter extends OncePerRequestFilter {

    /** 内部任务令牌请求头名称，与 job 服务 Feign 配置保持一致。 */
    public static final String HEADER_INTERNAL_JOB_TOKEN = "X-Internal-Job-Token";

    /**
     * 请求属性名：标记本次请求已由内部任务令牌认证通过。
     * 供链路中后续过滤器（{@link MallAdminTokenAuthenticationFilter}）跳过用户 Token 校验。
     */
    public static final String ATTRIBUTE_INTERNAL_JOB_AUTHENTICATED =
            PluginInternalJobTokenFilter.class.getName() + ".AUTHENTICATED";

    /**
     * 放行两类内部端点：
     * <ul>
     *   <li>{@code /plugin/{code}/job/**} — 插件内部定时任务（job → mall）；</li>
     *   <li>{@code /internal/distribution/**} — 分销订单事件（order → mall）。</li>
     * </ul>
     * 两者都靠 {@code X-Internal-Job-Token} 自证身份，无用户态。
     */
    private static final List<String> INTERNAL_JOB_PATH_PATTERNS =
            List.of("/plugin/*/job/**", "/internal/distribution/**");

    private static final String ROLE_INTERNAL_JOB = "ROLE_INTERNAL_JOB";
    private static final String PRINCIPAL_INTERNAL_JOB = "internal-job";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final String expectedToken;

    public PluginInternalJobTokenFilter(
            @Value("${houcloud.plugin.internal-job-token:}") String expectedToken) {
        this.expectedToken = expectedToken == null ? "" : expectedToken.trim();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!isInternalJobRequest(request)) {
            chain.doFilter(request, response);
            return;
        }
        if (!StringUtils.hasText(expectedToken)) {
            log.warn("插件内部任务令牌未配置（houcloud.plugin.internal-job-token），跳过内部认证: uri={}",
                    request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }
        String provided = request.getHeader(HEADER_INTERNAL_JOB_TOKEN);
        if (StringUtils.hasText(provided) && tokenMatches(provided.trim(), expectedToken)) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    PRINCIPAL_INTERNAL_JOB, null, List.of(new SimpleGrantedAuthority(ROLE_INTERNAL_JOB)));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute(ATTRIBUTE_INTERNAL_JOB_AUTHENTICATED, Boolean.TRUE);
            // 调用方身份由服务端认定：只有本过滤器握有令牌校验结果，因此「internal」这个身份
            // 必须在这里写入 request attribute，而不是信任任何请求头（插件前端与宿主同处一个
            // JS 运行时，请求头人人可伪造）。写入后 PluginDispatcher 才会把本次请求判为
            // internal 调用方，按被调方 @HouCaller / @HouRoute(allowCallers) 的声明放行；
            // 未声明的插件路由在 enforce-caller-whitelist=true 时会被拒绝。
            PluginDispatcher.markCaller(request, PluginDispatcher.CALLER_INTERNAL);
            log.debug("插件内部任务令牌认证通过: uri={}", request.getRequestURI());
        } else {
            log.warn("插件内部任务令牌校验失败: uri={}, hasToken={}",
                    request.getRequestURI(), StringUtils.hasText(provided));
        }
        chain.doFilter(request, response);
    }

    private boolean isInternalJobRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String pattern : INTERNAL_JOB_PATH_PATTERNS) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    /** 常量时间比较，避免令牌比较的时序侧信道。 */
    private boolean tokenMatches(String provided, String expected) {
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }
}
