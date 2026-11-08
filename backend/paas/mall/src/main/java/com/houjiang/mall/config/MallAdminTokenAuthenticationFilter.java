package com.houjiang.mall.config;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.mall.service.auth.MallAdminTokenService;
import com.houjiang.mall.service.auth.MallCloudTokenSyncService;
import com.houjiang.mall.service.auth.MallCloudPrincipalOrganizationResolver;
import com.houjiang.mall.util.MallAdminTokenHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商城服务鉴权（双 token，按优先级）：
 * <ol>
 *   <li>{@code MALL_ADMIN_TOKEN:} — bbc.houjit.com/admin 平台后台登录</li>
 *   <li>{@code hou-user-token-user_} — unimall.houjit.com 内嵌商城管理（云后台 token）</li>
 * </ol>
 * @author Amos
 */
@Slf4j
@Component
public class MallAdminTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> WHITE_LIST = List.of(
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/mall/front/**",
            "/mall/auth/**",
            // T11：插件前端产物公开只读（准入由插件登记 + 安装态两道门控把关）
            "/plugin-assets/**",
            // M2：制品回源端点。它不靠会话鉴权，靠 URL 上的下载令牌自证身份——
            // 令牌在 /mall/artifact/download-url 签发时已完成授权判定，并绑定
            // 「制品 + 站点 + 10 分钟 + 5 次命中」。插件引擎是后台程序，
            // 拿不到商城登录 Token，只能走令牌，因此这里必须放行。
            "/mall/artifact/raw",
            // Stage 3：订单库存生命周期兼容入口（写方在 goods；无用户态回调需放行）
            "/mall/storage/stock/lifecycle/**",
            // 分销订单事件（order → mall）：无用户态，靠 X-Internal-Job-Token 自证，
            // 由 PluginInternalJobTokenFilter 完成认证后本过滤器放行
            "/internal/distribution/**"
    );

    /** 买家前台只读首页数据，不要求登录 */
    private static final List<String> HOME_PUBLIC_GET = List.of(
            "/mall/home/data",
            "/mall/home/banner/list",
            "/mall/home/notice/list",
            "/mall/home/recommend/list",
            "/mall/home/floor/enabled",
            "/mall/config/get_mall_style"
    );

    private final MallAdminTokenService mallAdminTokenService;
    private final MallCloudTokenSyncService mallCloudTokenSyncService;
    private final MallCloudPrincipalOrganizationResolver cloudPrincipalOrganizationResolver;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HandlerExceptionResolver resolver;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public MallAdminTokenAuthenticationFilter(
            MallAdminTokenService mallAdminTokenService,
            MallCloudTokenSyncService mallCloudTokenSyncService,
            MallCloudPrincipalOrganizationResolver cloudPrincipalOrganizationResolver,
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.mallAdminTokenService = mallAdminTokenService;
        this.mallCloudTokenSyncService = mallCloudTokenSyncService;
        this.cloudPrincipalOrganizationResolver = cloudPrincipalOrganizationResolver;
        this.redisTemplate = redisTemplate;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (isWhiteList(uri) || isHomePublicGet(request.getMethod(), uri)) {
            chain.doFilter(request, response);
            return;
        }

        // 内部定时任务链路（job → /plugin/{code}/job/**）已由 PluginInternalJobTokenFilter
        // 完成认证并写入 SecurityContext，此处跳过用户 Token 校验，沿用其注入的 Authentication。
        if (Boolean.TRUE.equals(request.getAttribute(
                PluginInternalJobTokenFilter.ATTRIBUTE_INTERNAL_JOB_AUTHENTICATED))) {
            log.debug("插件内部任务请求跳过用户 Token 校验: uri={}", uri);
            chain.doFilter(request, response);
            return;
        }

        String rawAuth = request.getHeader("Authorization");
        if (!StringUtils.hasText(rawAuth)) {
            rawAuth = request.getHeader("authorization");
        }
        if (!mallAdminTokenService.isAuthenticated(rawAuth)) {
            log.warn("商城鉴权失败: uri={}, hasAuth={}", uri, StringUtils.hasText(rawAuth));
            reject(request, response);
            return;
        }

        Map<String, Object> adminInfo = mallAdminTokenService.getAdminInfo(rawAuth);
        if (adminInfo != null) {
            mallCloudTokenSyncService.ensureBound(rawAuth, adminInfo);
        }
        Object cloudUser = adminInfo == null ? loadCloudUser(rawAuth) : null;
        Object principal = adminInfo != null ? adminInfo : resolveCloudPrincipal(cloudUser);
        if (principal instanceof UserInfoVO cloudPrincipal) {
            principal = cloudPrincipalOrganizationResolver.resolve(cloudPrincipal);
        }
        if (principal == null) {
            log.warn("Cloud 用户登录态缺少可解析的用户信息: uri={}", uri);
            reject(request, response);
            return;
        }
        String role = adminInfo != null ? "ROLE_MALL_ADMIN" : "ROLE_CLOUD_USER";
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        Set<String> permissions = adminInfo == null
                ? getCloudPermissions(cloudUser)
                : (uri.startsWith("/mall/finance/")
                        ? mallAdminTokenService.getFinancePermissions(rawAuth)
                        : Set.of());
        permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private Object loadCloudUser(String rawAuth) {
        String token = MallAdminTokenHelper.normalize(rawAuth);
        return redisTemplate.opsForValue().get(HouConfig.HOU_USER_TOKEN_USER + token);
    }

    public static boolean hasCloudFinanceAccess(Object cloudUser) {
        return !getCloudFinancePermissions(cloudUser).isEmpty();
    }

    public static Set<String> getCloudFinancePermissions(Object cloudUser) {
        return getCloudPermissions(cloudUser).stream()
                .filter(MallAdminTokenService::isFinancePermission)
                .collect(Collectors.toUnmodifiableSet());
    }

    /** Cloud 登录态只把服务端签发的权限码转换为 Spring Security authority。 */
    public static Set<String> getCloudPermissions(Object cloudUser) {
        UserInfoDTO dto = parseCloudUser(cloudUser);
        if (dto == null || dto.getPermissions() == null) {
            return Set.of();
        }
        return dto.getPermissions().stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Cloud Redis 中保存的是 UserInfoDTO；业务插件统一只接受其中的 UserInfoVO 作为 principal。
     */
    public static UserInfoVO resolveCloudPrincipal(Object cloudUser) {
        if (cloudUser instanceof UserInfoVO user) {
            return user;
        }
        UserInfoDTO dto = parseCloudUser(cloudUser);
        return dto == null ? null : dto.getUser();
    }

    private static UserInfoDTO parseCloudUser(Object cloudUser) {
        if (cloudUser == null) {
            return null;
        }
        try {
            UserInfoDTO dto;
            if (cloudUser instanceof UserInfoDTO userInfoDTO) {
                dto = userInfoDTO;
            } else if (cloudUser instanceof String json) {
                dto = JSON.parseObject(json, UserInfoDTO.class);
            } else {
                dto = JSON.parseObject(JSON.toJSONString(cloudUser), UserInfoDTO.class);
            }
            if (dto == null || dto.getPermissions() == null) {
                return dto;
            }
            return dto;
        } catch (RuntimeException e) {
            log.warn("Cloud 用户登录态解析失败: type={}", cloudUser.getClass().getName());
            return null;
        }
    }

    private boolean isHomePublicGet(String method, String uri) {
        if (method == null || !"GET".equalsIgnoreCase(method)) {
            return false;
        }
        for (String pattern : HOME_PUBLIC_GET) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWhiteList(String uri) {
        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    private void reject(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        resolver.resolveException(
                request,
                response,
                null,
                new DefinedException(
                        BizCodeEnume.USER_IS_NOT_LOGGED_IN.getCode(),
                        "未登录或登录已过期。商城独立后台请用 bbc/admin 登录；Cloud 内嵌请用 cloud 账号登录"));
    }
}
