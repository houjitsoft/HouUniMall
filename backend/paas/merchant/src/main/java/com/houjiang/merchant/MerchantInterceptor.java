package com.houjiang.merchant;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.annotation.MerchantPermission;
import com.houjiang.merchant.common.entity.mall.MallShopAdmin;
import com.houjiang.merchant.common.entity.mall.MallShopMenu;
import com.houjiang.merchant.common.enums.ErrorEnum;
import com.houjiang.merchant.common.exception.LoginException;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMenuMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.YmlUtils;
import com.houjiang.merchant.service.mall.IMallShopAdminMenuService;
import com.houjiang.merchant.service.mall.IMallShopAdminRoleService;
import com.houjiang.merchant.service.mall.IMallShopService;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 商户模块拦截器
 * 认证由SecurityConfig + JwtAuthenticationTokenFilter统一处理
 * 此拦截器负责：
 * 1. 从SecurityContext获取已认证的用户信息并写入MerchantThreadLocal
 * 2. 演示环境拦截
 * 3. 权限校验（基于数据库菜单配置）
 * 
 * @author Amos
 * @since 2026-04-28
 */
@Component
public class MerchantInterceptor implements HandlerInterceptor {

    private static final String MERCHANT_PREFIX = "/merchant/";
    private static final String ADMINAPI_PREFIX = "/adminapi/";
    private static final Set<String> PUBLIC_ROUTES = Set.of(
            "/merchant/login/login",
            "/merchant/login/out",
            "/merchant/config/getConfig",
            "/adminapi/channel/oa/callback");
    private static final Set<String> AUTHENTICATED_BOOTSTRAP_ROUTES = Set.of(
            "/merchant/index/menu_list",
            "/merchant/index/mySelf");
    private static final Set<String> AUTHENTICATED_INFRASTRUCTURE_ROUTES = Set.of(
            "/merchant/upload/image",
            "/merchant/upload/file",
            "/merchant/upload/video",
            "/merchant/area/lists",
            "/merchant/area/lists_all",
            "/merchant/area/tencent_search",
            "/merchant/file/lists",
            "/merchant/file/rename",
            "/merchant/file/move",
            "/merchant/file/delete",
            "/merchant/file/listCate",
            "/merchant/file/addCate",
            "/merchant/file/editCate",
            "/merchant/file/delCate");
    private static final Map<String, String> PERMISSION_MODULE_ALIASES = Map.of(
            "user", "user.user");

    @Resource
    MallShopAdminMapper mallShopAdminMapper;
    @Resource
    MallShopMenuMapper mallShopMenuMapper;

    @Resource
    IMallShopAdminMenuService iMallShopAdminMenuService;
    @Resource
    IMallShopAdminRoleService iMallShopAdminRoleService;
    @Resource
    IMallShopService iMallShopService;

    /**
     * 前置处理器
     * JWT认证已由SecurityConfig处理，此处只负责填充ThreadLocal和权限检查
     *
     * @param request 请求
     * @param response 响应
     * @param handler 处理
     * @return boolean 是否继续处理
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return HandlerInterceptor.super.preHandle(request, response, handler);
        }

        boolean proceed = false;
        try {
            response.setContentType("application/json;charset=utf-8");
            String canonicalRoute = resolveCanonicalRoute(handlerMethod);
            if (canonicalRoute == null) {
                throw noPermission();
            }
            this.fillThreadLocal(canonicalRoute, permissionTokensForHandler(handlerMethod));

            // 演示环境拦截
            String env = YmlUtils.get("merchant.production");
            if (StringUtils.isNotNull(env) && env.equals("true")) {
                if (request.getMethod().equals("POST")
                        && !PUBLIC_ROUTES.contains(canonicalRoute)) {
                    String message = "演示环境不支持修改数据，请下载源码本地部署体验";
                    AjaxResult<Object> result = AjaxResult.failed(ErrorEnum.NO_PERMISSION.getCode(), message);
                    response.getWriter().print(JSON.toJSONString(result));
                    return false;
                }
            }

            proceed = HandlerInterceptor.super.preHandle(request, response, handler);
            return proceed;
        } catch (LoginException e) {
            AjaxResult<Object> result = AjaxResult.failed(e.getCode(), e.getMsg());
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        } finally {
            // 返回 false 的拦截器不会收到 afterCompletion，必须在当前请求立即清理。
            if (!proceed) {
                MerchantThreadLocal.remove();
            }
        }
    }

    /**
     * 后置处理器
     *
     * @param request 请求
     * @param response 响应
     * @param handler 处理
     * @param ex 异常
     * @throws Exception 异常
     */
    @Override
    public void afterCompletion(@NotNull HttpServletRequest request,
                                @NotNull HttpServletResponse response,
                                @NotNull Object handler, Exception ex) throws Exception {
        MerchantThreadLocal.remove();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    /**
     * 从SecurityContext获取用户信息并填充到ThreadLocal
     * JWT过滤器已完成认证，这里只从SecurityContext获取用户ID然后查询商户管理员信息
     *
     * @param canonicalRoute HandlerMethod解析出的规范路由
     */
    private void fillThreadLocal(
            String canonicalRoute, List<String> declaredPermissionTokens) {
        if (PUBLIC_ROUTES.contains(canonicalRoute)) {
            return;
        }

        // 尝试从SecurityContext获取当前用户
        UserInfoVO currentUser = SecurityContextUtils.getCurrentUserOrNull();
        if (currentUser == null || currentUser.getUserId() == null) {
            // 非白名单接口，用户未登录应抛出明确错误
            Integer errCode = ErrorEnum.TOKEN_INVALID.getCode();
            String errMsg = "用户未登录或token已过期，请重新登录";
            throw new LoginException(errCode, errMsg);
        }

        String userIdStr = currentUser.getUserId();
        Integer userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            throw new LoginException(
                    ErrorEnum.TOKEN_INVALID.getCode(), ErrorEnum.TOKEN_INVALID.getMsg());
        }

        // 用户校验
        MallShopAdmin adminUser = mallShopAdminMapper.selectOne(
                new QueryWrapper<MallShopAdmin>()
                    .eq("id", userId).eq("delete_time", 0)
                    .last("limit 1"));

        // 删除校验
        if (StringUtils.isNull(adminUser)) {
            Integer errCode = ErrorEnum.TOKEN_INVALID.getCode();
            String errMsg = ErrorEnum.TOKEN_INVALID.getMsg();
            throw new LoginException(errCode, errMsg);
        }

        // 禁用校验
        if (adminUser.getStatus().equals(0)) {
            Integer errCode = ErrorEnum.LOGIN_DISABLE_ERROR.getCode();
            String errMsg = ErrorEnum.LOGIN_DISABLE_ERROR.getMsg();
            throw new LoginException(errCode, errMsg);
        }

        // 写入线程
        MerchantThreadLocal.put("adminId", userId);
        MerchantThreadLocal.put("username", adminUser.getName());
        String roleIds = StringUtils.join(iMallShopAdminRoleService.getRoleIdAttr(userId), ",");
        MerchantThreadLocal.put("roleIds", roleIds);
        MerchantThreadLocal.put("shopId", adminUser.getShopId());
        MerchantThreadLocal.put("shopName", iMallShopService.getNameById(adminUser.getShopId()));
        MerchantThreadLocal.put("isSuper", adminUser.getIsSuper());

        // 登录后初始化必须先完成身份、状态和店铺上下文校验，再跳过菜单token检查。
        if (AUTHENTICATED_BOOTSTRAP_ROUTES.contains(canonicalRoute)) {
            return;
        }
        // 跨页面基础能力无独立菜单access，但仍必须先完成商家身份和上下文校验。
        if (AUTHENTICATED_INFRASTRUCTURE_ROUTES.contains(canonicalRoute)) {
            return;
        }

        // 所有受保护路由都必须先由启用菜单token覆盖；超管的“*”仅跳过角色分配检查。
        this.checkAuth(canonicalRoute, declaredPermissionTokens);
    }

    /**
     * 权限验证
     * 基于数据库菜单配置进行权限校验
     *
     * @param canonicalRoute HandlerMethod解析出的规范路由
     */
    void checkAuth(String canonicalRoute) {
        checkAuth(canonicalRoute, List.of());
    }

    private void checkAuth(
            String canonicalRoute, List<String> declaredPermissionTokens) {
        String prefix;
        if (canonicalRoute.startsWith(ADMINAPI_PREFIX)) {
            prefix = ADMINAPI_PREFIX;
        } else if (canonicalRoute.startsWith(MERCHANT_PREFIX)) {
            prefix = MERCHANT_PREFIX;
        } else {
            throw noPermission();
        }
        String access = canonicalRoute.substring(prefix.length());
        int actionSeparator = access.lastIndexOf('/');
        if (actionSeparator <= 0 || actionSeparator == access.length() - 1) {
            throw noPermission();
        }
        String permissionAccess = permissionAccessForCanonical(access);
        QueryWrapper<MallShopMenu> menuQuery = new QueryWrapper<MallShopMenu>()
                .eq("status", 1)
                .eq("delete_time", 0)
                .ne("access", "")
                .select("access");
        if (declaredPermissionTokens.isEmpty()) {
            String permissionModule = permissionAccess.substring(
                    0, permissionAccess.lastIndexOf('/'));
            menuQuery.likeRight("access", permissionModule + "/");
        } else {
            menuQuery.in("access", declaredPermissionTokens);
        }

        List<MallShopMenu> candidates = mallShopMenuMapper.selectList(menuQuery);
        List<String> matchingTokens = new ArrayList<>();
        if (candidates != null) {
            for (MallShopMenu candidate : candidates) {
                if (candidate == null || candidate.getAccess() == null) {
                    continue;
                }
                boolean matches = declaredPermissionTokens.isEmpty()
                        ? menuTokenCoversCanonicalAccess(
                                candidate.getAccess(), permissionAccess)
                        : declaredPermissionTokens.contains(candidate.getAccess());
                if (matches) {
                    matchingTokens.add(candidate.getAccess());
                }
            }
        }
        // 没有启用菜单token覆盖该action，说明权限尚未登记，两个命名空间都默认拒绝。
        if (matchingTokens.isEmpty()) {
            throw noPermission();
        }

        Integer adminId = MerchantThreadLocal.getAdminId();
        List<String> accessList = iMallShopAdminMenuService.getAdminAccessList(adminId);
        if (accessList != null) {
            if (accessList.contains("*")) {
                return;
            }
            for (String matchingToken : matchingTokens) {
                if (accessList.contains(matchingToken)) {
                    return;
                }
            }
        }
        throw noPermission();
    }

    List<String> permissionTokensForHandler(HandlerMethod handlerMethod) {
        MerchantPermission permission = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), MerchantPermission.class);
        if (permission == null) {
            permission = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(), MerchantPermission.class);
        }
        if (permission == null) {
            return List.of();
        }
        return Arrays.stream(permission.anyOf())
                .filter(token -> token != null && !token.isBlank())
                .distinct()
                .toList();
    }

    /**
     * 将Controller access精确转换为种子菜单使用的权限access，未登记模块保持原值。
     */
    static String permissionAccessForCanonical(String canonicalAccess) {
        int actionSeparator = canonicalAccess.lastIndexOf('/');
        if (actionSeparator <= 0 || actionSeparator == canonicalAccess.length() - 1) {
            return canonicalAccess;
        }
        String canonicalModule = canonicalAccess.substring(0, actionSeparator);
        String permissionModule = PERMISSION_MODULE_ALIASES.getOrDefault(
                canonicalModule, canonicalModule);
        return permissionModule + canonicalAccess.substring(actionSeparator);
    }

    /**
     * 判断数据库菜单token是否覆盖单个Controller canonical access。
     * 菜单action可用冒号声明组合权限，但模块和action均严格区分大小写。
     */
    static boolean menuTokenCoversCanonicalAccess(
            String menuToken, String canonicalAccess) {
        if (menuToken == null || menuToken.isBlank()
                || canonicalAccess == null || canonicalAccess.isBlank()) {
            return false;
        }

        int menuSeparator = menuToken.lastIndexOf('/');
        int accessSeparator = canonicalAccess.lastIndexOf('/');
        if (menuSeparator <= 0 || menuSeparator == menuToken.length() - 1
                || accessSeparator <= 0 || accessSeparator == canonicalAccess.length() - 1) {
            return false;
        }

        String menuModule = menuToken.substring(0, menuSeparator);
        String accessModule = canonicalAccess.substring(0, accessSeparator);
        String menuActions = menuToken.substring(menuSeparator + 1);
        String accessAction = canonicalAccess.substring(accessSeparator + 1);
        if (!menuModule.equals(accessModule)
                || accessAction.isBlank() || accessAction.contains(":")) {
            return false;
        }

        String[] actions = menuActions.split(":", -1);
        for (String action : actions) {
            if (action.isBlank()) {
                return false;
            }
        }
        for (String action : actions) {
            if (action.equals(accessAction)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从Controller声明的映射生成稳定路由，不依赖contextPath、矩阵参数或请求别名。
     */
    String resolveCanonicalRoute(HandlerMethod handlerMethod) {
        return resolveCanonicalRoute(handlerMethod.getBeanType(), handlerMethod.getMethod());
    }

    String resolveCanonicalRoute(Class<?> handlerType, Method handlerMethod) {
        RequestMapping classMapping = AnnotatedElementUtils.findMergedAnnotation(
                handlerType, RequestMapping.class);
        RequestMapping methodMapping = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod, RequestMapping.class);
        if (classMapping == null || methodMapping == null) {
            return null;
        }

        List<String> candidates = new ArrayList<>();
        for (String classPath : mappingPaths(classMapping)) {
            for (String methodPath : mappingPaths(methodMapping)) {
                String candidate = joinMappingPaths(classPath, methodPath);
                if (candidate.startsWith(MERCHANT_PREFIX)
                        || candidate.startsWith(ADMINAPI_PREFIX)
                        || candidate.equals("/merchant")
                        || candidate.equals("/adminapi")) {
                    candidates.add(candidate);
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }

        // Access规范为 module/action。多别名时优先该规范，顺序相同则保留声明顺序。
        String best = candidates.getFirst();
        int bestScore = canonicalRouteScore(best);
        for (int i = 1; i < candidates.size(); i++) {
            String candidate = candidates.get(i);
            int score = canonicalRouteScore(candidate);
            if (score < bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return best;
    }

    private String[] mappingPaths(RequestMapping mapping) {
        String[] paths = mapping.path();
        if (paths.length == 0) {
            paths = mapping.value();
        }
        return paths.length == 0 ? new String[]{""} : paths;
    }

    private String joinMappingPaths(String classPath, String methodPath) {
        String[] parts = (classPath + "/" + methodPath).split("/");
        StringBuilder route = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank()) {
                route.append('/').append(part);
            }
        }
        return route.isEmpty() ? "/" : route.toString();
    }

    private int canonicalRouteScore(String route) {
        String prefix = route.startsWith(ADMINAPI_PREFIX)
                ? ADMINAPI_PREFIX : MERCHANT_PREFIX;
        String access = route.substring(Math.min(prefix.length(), route.length()));
        long separators = access.chars().filter(value -> value == '/').count();
        return separators == 1 ? 0 : 1;
    }

    private LoginException noPermission() {
        return new LoginException(
                ErrorEnum.NO_PERMISSION.getCode(), "无权限访问该功能");
    }
}
