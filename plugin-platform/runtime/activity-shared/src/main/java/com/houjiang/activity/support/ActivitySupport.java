package com.houjiang.activity.support;

import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.util.SecurityContextUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 活动插件共用的请求上下文工具（店铺归属校验、令牌读取）。
 *
 * <p>原 BaseController 把全部业务 service 一并 @Autowired 到基类，形成集中式门面，
 * 使任何插件拆分都必须复制整个门面。此类只保留与业务无关的上下文逻辑；
 * 业务 service 由各 controller 按需注入，插件之间不共享业务实现。
 */
public final class ActivitySupport {

    private ActivitySupport() { }

    /** 当前登录用户所属商城店铺，所有管理数据必须绑定到该店铺。 */
    public static Integer requireCurrentShopId() {
        UserInfoVO user = SecurityContextUtils.getCurrentUserOrNull();
        if (user == null || user.getShopId() == null || user.getShopId().isBlank()) {
            throw new IllegalStateException("当前店铺未确定，拒绝执行跨店铺活动操作");
        }
        try {
            int shopId = Integer.parseInt(user.getShopId().trim());
            if (shopId <= 0) throw new NumberFormatException();
            return shopId;
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("当前店铺ID无效");
        }
    }

    /** 从请求头中获取 token。 */
    public static String getTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                return authorization.substring(7);
            }
        }
        return null;
    }

    /** 验证 token 并获取用户信息。 */
    public static UserInfoVO validateTokenAndGetUser() {
        return SecurityContextUtils.getCurrentUser();
    }
}
