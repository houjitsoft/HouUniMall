package com.houjiang.commons.filter;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * JWT认证过滤器 - 通用版本
 * 
 * 【重要说明】Token验证机制：
 * 1. 本过滤器是PAAS层统一使用的JWT验证组件（位于commons模块）
 * 2. Token由BFF层（bff-web）通过FeignRequestInterceptor传递过来
 * 3. 验证流程：
 *    a. 从请求头提取Authorization（格式：Bearer {token}）
 *    b. 解析JWT token获取用户ID
 *    c. 从Redis查询用户信息（key: HOU_USER_TOKEN_USER + token）
 *    d. 将用户信息设置到SecurityContextHolder
 * 4. Controller中通过SecurityContextUtils.getCurrentUser()获取当前用户
 * 
 * 【关键点】：
 * - 所有PAAS服务必须配置此过滤器（在SecurityConfig中）
 * - 必须注入RedisCache和HandlerExceptionResolver
 * - 如果Redis中没有找到token，返回"用户未登录"错误（code: 11006）
 * - 不要手动验证token，本过滤器已经处理
 * 
 * 【配置要求】：
 * 在PAAS服务的SecurityConfig中：
 * ```java
 * @Bean
 * public JwtAuthenticationTokenFilter<LoginUser> jwtAuthenticationTokenFilter() {
 *     JwtAuthenticationTokenFilter<LoginUser> filter = new JwtAuthenticationTokenFilter<>(
 // 白名单路径
/**
 *         LoginUser.class, "/user/login", "/actuator/**"
 *     );
 *     filter.setRedisCache(redisCache);
 *     filter.setResolver(handlerExceptionResolver);
 *     return filter;
 * }
 * ```
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Slf4j
public class JwtAuthenticationTokenFilter<T> extends OncePerRequestFilter {

    private RedisCache redisCache;

    private HandlerExceptionResolver resolver;
    
    /**
     * 登录用户类型（由子类指定）
     */
    private final Class<T> loginUserClass;
    
    /**
     * 白名单URL路径
     */
    private final List<String> whiteListPaths;

    // ================== Redis 故障兜底：本地缓存 + 重试 ==================
    /**
     * token -> userValue 本地缓存（进程级），用于 Redis 抖动期间兜底。
     * 用 ConcurrentHashMap 手写 TTL，避免引入 Caffeine/commons-pool2 等
     * 可能触发 Spring Boot 3.2.4 nested jar loader bug 的额外依赖。
     */
    private static final java.util.concurrent.ConcurrentHashMap<String, LocalCacheEntry> TOKEN_LOCAL_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();
    /**
     * 本地兜底缓存 fresh 期（60 秒）——正常命中期，覆盖 Lettuce autoReconnect 典型窗口。
     * fresh 期内 Redis 异常时使用 L1 数据是完全安全的。
     */
    private static final long LOCAL_CACHE_FRESH_MS = 60_000L;
    /**
     * 本地兜底缓存 stale 期额外延长（10 分钟）——SWR（Stale-While-Revalidate）模式兑底。
     * 仅当 Redis 持续不可用（>60s）时使用，保障已登录用户在 Redis 抖动 10 分钟内无感。
     * 已登出的 token 理论上可能在 stale 窗口内仍能访问，但其 JWT 本身有有效期，风险可控。
     */
    private static final long LOCAL_CACHE_STALE_MS = 600_000L;
    /** 本地缓存上限，防止内存无限增长；超限时清理完全过期项 */
    private static final int LOCAL_CACHE_MAX_SIZE = 5000;
    /**
     * Redis 首次失败后重试前的退避时间（毫秒）。
     * 实测 Lettuce autoReconnect 在 100ms 内可完成，退避 200ms 足以等待重连。
     * 过长的退避会叠加到用户响应时间（当前 3s commandTimeout × 2 + 200ms ≈ 6.2s 最差），
     * 取 200ms 在"给重连机会"与"快速失败"之间取平衡。
     */
    private static final long REDIS_RETRY_DELAY_MS = 200L;

    private static final class LocalCacheEntry {
        final String value;
        final long freshUntilMs;
        final long staleUntilMs;
        LocalCacheEntry(String value) {
            this.value = value;
            long now = System.currentTimeMillis();
            this.freshUntilMs = now + LOCAL_CACHE_FRESH_MS;
            this.staleUntilMs = now + LOCAL_CACHE_FRESH_MS + LOCAL_CACHE_STALE_MS;
        }
        boolean isFresh() { return System.currentTimeMillis() <= freshUntilMs; }
        boolean isStale() {
            long now = System.currentTimeMillis();
            return now > freshUntilMs && now <= staleUntilMs;
        }
        boolean isExpired() { return System.currentTimeMillis() > staleUntilMs; }
    }

    /**
     * 构造函数
     * @param loginUserClass 登录用户实体类
     * @param whiteListPaths 白名单路径
     */
    public JwtAuthenticationTokenFilter(Class<T> loginUserClass, String... whiteListPaths) {
        this.loginUserClass = loginUserClass;
        this.whiteListPaths = Arrays.asList(whiteListPaths);
    }

    /**
     * 设置RedisCache（用于手动注入依赖）
     * @param redisCache Redis缓存组件
     */
    public void setRedisCache(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    /**
     * 设置HandlerExceptionResolver（用于手动注入依赖）
     * @param resolver 异常处理器
     */
    public void setResolver(HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * 自动注入RedisCache（Spring容器管理时使用）
     */
    @Autowired(required = false)
    public void autowireRedisCache(RedisCache redisCache) {
        if (this.redisCache == null) {
            this.redisCache = redisCache;
        }
    }

    /**
     * 自动注入HandlerExceptionResolver（Spring容器管理时使用）
     */
    @Autowired(required = false)
    @Qualifier("handlerExceptionResolver")
    public void autowireResolver(HandlerExceptionResolver resolver) {
        if (this.resolver == null) {
            this.resolver = resolver;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws IOException, ServletException {
        
        String requestURI = request.getRequestURI();
        log.info("JWT过滤器处理请求: {}, redisCache: {}, resolver: {}", requestURI, redisCache != null, resolver != null);
        
        // 检查是否在白名单中
        boolean inWhiteList = isInWhiteList(requestURI);
        log.info("JWT过滤器 - 白名单检查: 路径={}, 白名单路径={}, 匹配结果={}", 
            requestURI, whiteListPaths, inWhiteList);
        if (inWhiteList) {
            log.info("请求在白名单中，直接放行: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 检查依赖是否已注入（提前检查，避免后续问题）
        if (redisCache == null) {
            log.error("RedisCache未注入，无法验证token，请求URI: {}", requestURI);
            if (resolver != null) {
                resolver.resolveException(request, response, null, 
                    new DefinedException(BizCodeEnume.SYSTEM_ERROR.getCode(), "系统配置错误：RedisCache未初始化"));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json;charset=UTF-8");
                try {
                    response.getWriter().write("{\"code\":500,\"message\":\"系统配置错误：RedisCache未初始化\"}");
                } catch (IOException e) {
                    log.error("无法写入错误响应", e);
                }
            }
            return;
        }
        
        if (resolver == null) {
            log.error("HandlerExceptionResolver未注入，无法处理异常，请求URI: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":500,\"message\":\"系统配置错误：异常处理器未初始化\"}");
            } catch (IOException e) {
                log.error("无法写入错误响应", e);
            }
            return;
        }
        
        // 获取token：优先 Authorization，其次 Token/token 头（旧商城子应用），再次 URL 参数 token
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            String headerToken = request.getHeader("Token");
            if (!StringUtils.hasText(headerToken)) {
                headerToken = request.getHeader("token");
            }
            if (StringUtils.hasText(headerToken)) {
                token = headerToken.startsWith("Bearer ") ? headerToken : ("Bearer " + headerToken);
                log.debug("JWT过滤器 - 从 Token/token 请求头读取认证信息，路径: {}", requestURI);
            }
        }
        if (!StringUtils.hasText(token)) {
            String queryToken = request.getParameter("token");
            if (StringUtils.hasText(queryToken)) {
                token = queryToken.startsWith("Bearer ") ? queryToken : ("Bearer " + queryToken);
                log.debug("JWT过滤器 - 从 URL 参数 token 读取认证信息，路径: {}", requestURI);
            }
        }
        log.info("JWT过滤器 - 请求URI: {}, Authorization头: {}, 长度: {}", 
            requestURI, 
            token != null && token.length() > 20 ? token.substring(0, 20) + "..." : token, 
            token != null ? token.length() : 0);
        
        if (!StringUtils.hasText(token)) {
            log.warn("JWT过滤器 - 请求中没有token，路径: {}, 所有请求头: {}", requestURI, 
                java.util.Collections.list(request.getHeaderNames()));
            // 没有token时，直接返回未登录错误（统一使用commons的JWT验证）
            resolver.resolveException(request, response, null, 
                new DefinedException(BizCodeEnume.USER_IS_NOT_LOGGED_IN.getCode(), BizCodeEnume.USER_IS_NOT_LOGGED_IN.getMsg()));
            return;
        }
        
        // 检查token格式，如果是Bearer格式，提取token
        String originalToken = token;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.debug("JWT过滤器 - 提取Bearer后的token: {}...", token.length() > 20 ? token.substring(0, 20) : token);
        } else if (token.startsWith("Bearer Bearer ")) {
            // 处理双重Bearer的情况
            token = token.substring(14);
            log.warn("JWT过滤器 - 检测到双重Bearer前缀，已修复: {}...", token.length() > 20 ? token.substring(0, 20) : token);
        }
        
        // 检查token是否为undefined或null
        if ("undefined".equals(token) || "null".equals(token) || !StringUtils.hasText(token)) {
            log.warn("JWT过滤器 - Token为空或无效，原始token: {}, 提取后token: {}", originalToken, token);
            // Token为空或无效时，直接返回未登录错误（统一使用commons的JWT验证）
            resolver.resolveException(request, response, null, 
                new DefinedException(BizCodeEnume.USER_IS_NOT_LOGGED_IN.getCode(), BizCodeEnume.USER_IS_NOT_LOGGED_IN.getMsg()));
            return;
        }
        
        // 解析token验证格式
        String userid;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userid = claims.getSubject();
            log.info("Token解析成功，用户ID: {}", userid);
        } catch (Exception e) {
            log.error("Token解析失败: {}", e.getMessage());
            resolver.resolveException(request, response, null, 
                new DefinedException(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg()));
            return;
        }
        
        // 从redis中获取用户信息 - 优先使用HOU_USER_TOKEN_USER，兼容旧的HOU_USER_TOKEN_USER
        // 统一使用commons的RedisCache进行Redis操作
        String redisKey = HouConfig.HOU_USER_TOKEN_USER + token;

        // Redis 三层兜底：本地缓存(L1) → Redis(R1) → 本地缓存兜底 / 重试(R2) → 失败
        String value = null;
        boolean redisFirstFailed = false;
        Exception firstRedisEx = null;

        // ---- R1: 首次 Redis 查询 ----
        try {
            value = fetchTokenValueFromRedis(redisKey);
        } catch (Exception redisEx) {
            redisFirstFailed = true;
            firstRedisEx = redisEx;
            log.warn("JWT过滤器 - Redis首次查询异常，将走本地缓存兜底/重试: {}", redisEx.getMessage());
        }

        // ---- Redis 异常分支：SWR 策略（L1 优先 → 退避 → 重试 → L1 极限 → 失败）----
        // 优先返回 L1 任意可用（fresh/stale）数据，避免叠加第二次 3s 超时等待
        if (redisFirstFailed) {
            long swrStart = System.currentTimeMillis();
            LocalCacheEntry cached = TOKEN_LOCAL_CACHE.get(token);
            if (cached != null && !cached.isExpired()) {
                // L1 命中（fresh 或 stale 都可用）：直接返回，不再重试 Redis，节省 3s+
                value = cached.value;
                if (cached.isFresh()) {
                    log.info("JWT过滤器 - L1 fresh SWR命中(Redis异常期间，已跳过R2重试), token前20: {}, fresh剩余: {}ms",
                            token.length() > 20 ? token.substring(0, 20) : token,
                            cached.freshUntilMs - System.currentTimeMillis());
                } else {
                    log.warn("JWT过滤器 - L1 stale SWR兜底(Redis不可用，已跳过R2重试), token前20: {}, stale剩余: {}ms",
                            token.length() > 20 ? token.substring(0, 20) : token,
                            cached.staleUntilMs - System.currentTimeMillis());
                }
            } else {
                // L1 miss：退避 200ms 后重试 1 次 Redis（给 Lettuce reconnect 机会）
                try {
                    Thread.sleep(REDIS_RETRY_DELAY_MS);
                    value = fetchTokenValueFromRedis(redisKey);
                    log.info("JWT过滤器 - Redis重试成功, token前20: {}, 总耗时: {}ms",
                            token.length() > 20 ? token.substring(0, 20) : token,
                            System.currentTimeMillis() - swrStart);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("JWT过滤器 - Redis重试被中断: {}", ie.getMessage());
                    resolver.resolveException(request, response, null,
                            new DefinedException(BizCodeEnume.SYSTEM_ERROR.getCode(), "认证服务暂时不可用，请稍后重试"));
                    return;
                } catch (Exception retryEx) {
                    // 重试也失败：最后再查一次 L1（并发请求可能在重试期间写入了 L1）
                    LocalCacheEntry cached2 = TOKEN_LOCAL_CACHE.get(token);
                    if (cached2 != null && !cached2.isExpired()) {
                        value = cached2.value;
                        log.warn("JWT过滤器 - Redis重试失败后使用 L1 极限兜底, token前20: {}, isFresh: {}, 总耗时: {}ms",
                                token.length() > 20 ? token.substring(0, 20) : token,
                                cached2.isFresh(),
                                System.currentTimeMillis() - swrStart);
                    } else {
                        log.error("JWT过滤器 - Redis重试仍失败且无 L1 可兜底，总耗时: {}ms, 原始异常: {}, 重试异常: {}",
                                System.currentTimeMillis() - swrStart,
                                firstRedisEx != null ? firstRedisEx.getMessage() : "null",
                                retryEx.getMessage());
                        resolver.resolveException(request, response, null,
                                new DefinedException(BizCodeEnume.SYSTEM_ERROR.getCode(), "认证服务暂时不可用，请稍后重试"));
                        return;
                    }
                }
            }
        }

        // ---- Redis 成功且命中（非空）→ 写入本地缓存，抖动窗口期内兜底其他请求 ----
        if (!redisFirstFailed && value != null && !value.isEmpty()) {
            if (TOKEN_LOCAL_CACHE.size() >= LOCAL_CACHE_MAX_SIZE) {
                TOKEN_LOCAL_CACHE.entrySet().removeIf(e -> e.getValue().isExpired());
            }
            TOKEN_LOCAL_CACHE.put(token, new LocalCacheEntry(value));
        }
        
        // 如果Redis中没有找到用户信息，直接返回未登录错误
        if (value == null || value.isEmpty()) {
            log.error("JWT过滤器 - 用户未登录或登录已过期，Redis中未找到token对应的用户信息！" +
                "\n请求URI: {}" +
                "\nToken前20字符: {}" +
                "\n用户ID: {}" +
                "\nRedisKey: {}" +
                "\n建议：请检查登录时是否正确存储token到Redis，或token是否已过期",
                requestURI,
                token.length() > 20 ? token.substring(0, 20) : token, 
                userid,
                redisKey);
            resolver.resolveException(request, response, null, 
                new DefinedException(BizCodeEnume.USER_IS_NOT_LOGGED_IN.getCode(), BizCodeEnume.USER_IS_NOT_LOGGED_IN.getMsg()));
            return;
        }
        
        // 刷新token过期时间
        try {
            redisCache.expire(redisKey, HouConfig.TOKEN_OUTTIME, java.util.concurrent.TimeUnit.SECONDS);
            log.debug("JWT过滤器 - 已刷新token过期时间，key: {}, 过期时间: {}秒", redisKey, HouConfig.TOKEN_OUTTIME);
        } catch (Exception e) {
            log.warn("JWT过滤器 - 刷新token过期时间失败（不影响认证）: {}", e.getMessage());
        }
        
        // 解析用户信息 - 需要先解析为UserInfoDTO，再提取User
        log.info("JWT过滤器 - 开始解析用户信息，value前100字符: {}", 
            value != null && value.length() > 100 ? value.substring(0, 100) : value);
        
        com.houjiang.commons.dto.userms.UserInfoDTO userInfoDTO = null;
        try {
            userInfoDTO = JSON.parseObject(value, com.houjiang.commons.dto.userms.UserInfoDTO.class);
            log.info("JWT过滤器 - UserInfoDTO解析结果: {}, user字段: {}", 
                userInfoDTO != null, 
                userInfoDTO != null && userInfoDTO.getUser() != null ? "存在" : "不存在");
        } catch (Exception e) {
            log.error("JWT过滤器 - 解析UserInfoDTO失败: {}, value: {}", e.getMessage(), 
                value != null && value.length() > 200 ? value.substring(0, 200) : value, e);
        }
        
        T loginUser = null;
        
        if (userInfoDTO != null && userInfoDTO.getUser() != null) {
            log.info("JWT过滤器 - UserInfoDTO解析成功，尝试创建LoginUser，loginUserClass: {}", loginUserClass.getName());
            // 如果loginUserClass是UserInfoVO类型，直接使用
            if (loginUserClass.isInstance(userInfoDTO.getUser())) {
                loginUser = (T) userInfoDTO.getUser();
                log.info("JWT过滤器 - 直接使用UserInfoVO作为登录用户");
            } else {
                // 尝试构造LoginUser对象（OA服务使用）
                try {
                    // 使用反射构造LoginUser，同时恢复登录缓存中的真实权限列表
                    java.lang.reflect.Constructor<T> constructor = loginUserClass.getConstructor(
                        com.houjiang.commons.vo.user.UserInfoVO.class, 
                        java.util.List.class
                    );
                    List<String> permissions = userInfoDTO.getPermissions() == null
                        ? List.of()
                        : userInfoDTO.getPermissions().stream()
                            .filter(StringUtils::hasText)
                            .map(String::trim)
                            .distinct()
                            .toList();
                    loginUser = constructor.newInstance(userInfoDTO.getUser(), permissions);
                    log.info("JWT过滤器 - 通过构造函数创建LoginUser成功，类型: {}", loginUser.getClass().getName());
                } catch (Exception e) {
                    // 如果构造失败，尝试直接解析为loginUserClass（兼容旧格式）
                    log.warn("JWT过滤器 - 无法通过构造函数创建LoginUser，尝试直接解析: {}", e.getMessage());
                    try {
                        loginUser = JSON.parseObject(value, loginUserClass);
                        log.info("JWT过滤器 - 直接解析为loginUserClass成功，类型: {}", 
                            loginUser != null ? loginUser.getClass().getName() : "null");
                    } catch (Exception e2) {
                        log.error("JWT过滤器 - 直接解析为loginUserClass失败: {}", e2.getMessage(), e2);
                    }
                }
            }
        } else {
            // 如果解析失败，尝试直接解析为loginUserClass（兼容旧格式）
            log.warn("JWT过滤器 - UserInfoDTO为空或user字段为空，尝试直接解析为loginUserClass");
            try {
                loginUser = JSON.parseObject(value, loginUserClass);
                log.info("JWT过滤器 - 直接解析为loginUserClass成功，类型: {}", 
                    loginUser != null ? loginUser.getClass().getName() : "null");
            } catch (Exception e) {
                log.error("JWT过滤器 - 直接解析为loginUserClass失败: {}, value前200字符: {}", 
                    e.getMessage(), 
                    value != null && value.length() > 200 ? value.substring(0, 200) : value, e);
            }
        }
        
        if (Objects.isNull(loginUser)) {
            log.error("JWT过滤器 - 用户未登录或登录已过期，无法解析用户信息！" +
                "Token: {}..., 用户ID: {}, RedisKey: {}, value前200字符: {}, " +
                "userInfoDTO: {}, loginUserClass: {}", 
                token.length() > 20 ? token.substring(0, 20) : token,
                userid,
                redisKey,
                value != null && value.length() > 200 ? value.substring(0, 200) : value,
                userInfoDTO != null,
                loginUserClass.getName());
            resolver.resolveException(request, response, null, 
                new DefinedException(BizCodeEnume.USER_IS_NOT_LOGGED_IN.getCode(), BizCodeEnume.USER_IS_NOT_LOGGED_IN.getMsg()));
            return;
        }
        
        // 存入 SecurityContextHolder。
        // UserInfoVO 路径（erp/finance/pm 等）不实现 UserDetails，原先 authorities=null，
        // ProductAccessInterceptor 会把已登录且已授权菜单的请求误判成 403
        // 「Product access denied or unavailable」。权限以 Redis 会话里的 permissions 为准。
        Collection<? extends GrantedAuthority> authorities = resolveAuthorities(loginUser, userInfoDTO);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        
        log.info("JWT过滤器 - 认证信息已设置到SecurityContext，用户ID: {}, Principal类型: {}, LoginUser类型: {}", 
            userid, 
            loginUser != null ? loginUser.getClass().getName() : "null",
            loginUser != null && loginUser.getClass().getName().contains("LoginUser") ? "LoginUser" : "其他类型");
        
        // 验证SecurityContext是否正确设置
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            log.error("JWT过滤器 - 警告：SecurityContext设置失败！authentication: {}, principal: {}", 
                auth != null, auth != null && auth.getPrincipal() != null);
        } else {
            log.debug("JWT过滤器 - SecurityContext验证成功，Principal: {}", auth.getPrincipal().getClass().getName());
        }
        
        // 放行。若原始请求只有 token/Token/query，而没有 Authorization，
        // 下游 ProductAccessPolicy 只会转发 Authorization 去问 base；
        // 这里补齐包装头，避免「JWT 已登录、产品闸却 503 Product access denied」。
        filterChain.doFilter(withAuthorizationHeader(request, token), response);
    }

    /**
     * 当请求未带可用 Authorization 时，用已解析出的 JWT 补一个 Bearer 头，供下游拦截器/策略读取。
     */
    private static HttpServletRequest withAuthorizationHeader(HttpServletRequest request, String rawToken) {
        String existing = request.getHeader("Authorization");
        if (StringUtils.hasText(existing)
                && !"null".equalsIgnoreCase(existing.trim())
                && !"undefined".equalsIgnoreCase(existing.trim())) {
            return request;
        }
        if (!StringUtils.hasText(rawToken)) {
            return request;
        }
        final String bearer = rawToken.startsWith("Bearer ") ? rawToken : ("Bearer " + rawToken);
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if ("Authorization".equalsIgnoreCase(name)) {
                    return bearer;
                }
                return super.getHeader(name);
            }

            @Override
            public java.util.Enumeration<String> getHeaders(String name) {
                if ("Authorization".equalsIgnoreCase(name)) {
                    return java.util.Collections.enumeration(java.util.List.of(bearer));
                }
                return super.getHeaders(name);
            }

            @Override
            public java.util.Enumeration<String> getHeaderNames() {
                java.util.List<String> names = new java.util.ArrayList<>(
                        java.util.Collections.list(super.getHeaderNames()));
                boolean hasAuth = names.stream().anyMatch(n -> "Authorization".equalsIgnoreCase(n));
                if (!hasAuth) {
                    names.add("Authorization");
                }
                return java.util.Collections.enumeration(names);
            }
        };
    }

    /**
     * 解析写入 SecurityContext 的权限集合。
     * UserDetails 实现（如 OA LoginUser）走自身 authorities；UserInfoVO 等非 UserDetails
     * 主体则回退到会话 UserInfoDTO.permissions，避免独立微服务 ProductAccess 鉴权空权限 403。
     */
    private Collection<? extends GrantedAuthority> resolveAuthorities(
            T loginUser, com.houjiang.commons.dto.userms.UserInfoDTO userInfoDTO) {
        java.util.LinkedHashMap<String, GrantedAuthority> merged = new java.util.LinkedHashMap<>();
        if (loginUser instanceof UserDetails userDetails) {
            Collection<? extends GrantedAuthority> fromUser = userDetails.getAuthorities();
            if (fromUser != null && !fromUser.isEmpty()) {
                appendAuthorities(merged, fromUser, null);
            }
        }
        if (merged.isEmpty() && userInfoDTO != null && userInfoDTO.getPermissions() != null) {
            appendAuthorities(merged, null, userInfoDTO.getPermissions());
        }
        // 权限叠加：插件安装 / 角色改授权之后，登录时固化的会话快照不会自己长出新的权限点，
        // 老会话会一直 403（插件接口提示「无权访问插件接口」）。这里再读一次权限服务维护的
        // 最新快照 user:permissions:<userId>（60 分钟 TTL，菜单变更时由插件菜单同步清空），
        // 与会话快照取并集：身份仍以会话为准，权限以权限服务的最新记录为准。
        for (String permission : fetchLatestPermissions(userInfoDTO)) {
            merged.putIfAbsent(permission, new SimpleGrantedAuthority(permission));
        }
        return List.copyOf(merged.values());
    }

    /** 把 UserDetails 的权限或会话 permissions 合并进目标集合，按权限名去重。 */
    private static void appendAuthorities(java.util.LinkedHashMap<String, GrantedAuthority> target,
                                          Collection<? extends GrantedAuthority> authorities,
                                          List<String> permissions) {
        if (authorities != null) {
            for (GrantedAuthority authority : authorities) {
                if (authority != null && StringUtils.hasText(authority.getAuthority())) {
                    String name = authority.getAuthority().trim();
                    target.putIfAbsent(name, authority);
                }
            }
        }
        if (permissions != null) {
            for (String permission : permissions) {
                if (StringUtils.hasText(permission)) {
                    String name = permission.trim();
                    target.putIfAbsent(name, new SimpleGrantedAuthority(name));
                }
            }
        }
    }

    /**
     * 读取权限服务维护的最新权限快照 {@code user:permissions:<userId>}。
     *
     * <p>权限缓存缺失（TTL 到期未重建）、格式不符或 Redis 不可用时一律降级返回空集合：
     * 登录态本身已经校验通过，不能因为权限缓存读不到就把用户挡在门外。
     */
    private List<String> fetchLatestPermissions(com.houjiang.commons.dto.userms.UserInfoDTO userInfoDTO) {
        if (redisCache == null || userInfoDTO == null || userInfoDTO.getUser() == null
                || !StringUtils.hasText(userInfoDTO.getUser().getUserId())) {
            return List.of();
        }
        String cacheKey = "user:permissions:" + userInfoDTO.getUser().getUserId().trim();
        try {
            Object cached = redisCache.getCacheObject(cacheKey);
            if (cached == null) {
                return List.of();
            }
            // 权限服务写入的是 Jackson 序列化的 JSON 数组，经 Redis 序列化后可能再被字符串包一层。
            Object parsed = cached;
            for (int depth = 0; depth < 3 && parsed instanceof String text; depth++) {
                if (!StringUtils.hasText(text)) {
                    return List.of();
                }
                parsed = JSON.parse(text);
            }
            if (!(parsed instanceof List<?> list) || list.isEmpty()) {
                return List.of();
            }
            List<String> permissions = new ArrayList<>(list.size());
            for (Object item : list) {
                if (item != null && StringUtils.hasText(String.valueOf(item))) {
                    permissions.add(String.valueOf(item).trim());
                }
            }
            return permissions;
        } catch (RuntimeException e) {
            log.debug("JWT过滤器 - 读取最新权限快照失败，沿用会话权限: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 从 Redis 查询 token 对应的用户信息字符串。
     * 【性能优化】移除 hasKey + getExpire 预检（减少 2 次 Redis 往返），
     * 直接 getCacheObject，null 视为不存在。单次 Redis 调用最差 3s（原先最差 9s）。
     * 查询 HOU_USER_TOKEN_USER + token；旧前缀由发布迁移脚本一次性原子改名。
     * 任何 Redis 异常直接上抛给调用方，由上层做重试 / 本地缓存兜底决策。
     *
     * @param redisKey 新 key（HOU_USER_TOKEN_USER + token）
     * @return token 对应的 value（用户 JSON），不存在返回 null
     */
    private String fetchTokenValueFromRedis(String redisKey) {
        long t0 = System.currentTimeMillis();
        Object raw = redisCache.getCacheObject(redisKey);
        String value = toTokenJsonString(raw);
        long cost1 = System.currentTimeMillis() - t0;
        if (value != null && !value.isEmpty()) {
            log.info("JWT过滤器 - 从Redis新key获取用户信息成功，key: {}, value长度: {}, 耗时: {}ms",
                    redisKey, value.length(), cost1);
            return value;
        }

        log.warn("JWT过滤器 - Redis key未命中, key: {}, 耗时: {}ms", redisKey,
                System.currentTimeMillis() - t0);
        return null;
    }

    /** Redis 值可能是 String 或 FastJson 反序列化后的 Map，统一转为 JSON 字符串供后续解析。 */
    private static String toTokenJsonString(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof String s) {
            return s.isEmpty() ? null : s;
        }
        return JSON.toJSONString(raw);
    }

    /**
     *检查URL是否在白名单中
/**
     * 支持精确匹配和通配符匹配（**表示多级路径）
     */
    private boolean isInWhiteList(String requestURI) {
        if (whiteListPaths == null || whiteListPaths.isEmpty()) {
            return false;
        }
        
        for (String whitePath : whiteListPaths) {
            // 处理通配符路径（如 /actuator/**）
            if (whitePath.endsWith("/**")) {
                String prefix = whitePath.substring(0, whitePath.length() - 3);
                if (requestURI.startsWith(prefix)) {
                    log.debug("白名单匹配（通配符）: {} 匹配 {}", requestURI, whitePath);
                    return true;
                }
            }
            // 精确匹配
            else if (requestURI.equals(whitePath)) {
                log.debug("白名单匹配（精确）: {} 匹配 {}", requestURI, whitePath);
                return true;
            }
            // 包含匹配（向后兼容）
            else if (requestURI.contains(whitePath)) {
                log.debug("白名单匹配（包含）: {} 包含 {}", requestURI, whitePath);
                return true;
            }
        }
        
        return false;
    }
}
