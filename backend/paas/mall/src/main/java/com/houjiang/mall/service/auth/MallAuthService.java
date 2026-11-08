package com.houjiang.mall.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.commons.util.JwtUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.dto.auth.MallAuthAdminVo;
import com.houjiang.mall.dto.auth.MallAuthLoginVo;
import com.houjiang.mall.dto.auth.MallAuthMenuVo;
import com.houjiang.mall.dto.auth.MallAuthUserInfoVo;
import com.houjiang.mall.entity.auth.MallSystemMenu;
import com.houjiang.mall.mapper.auth.MallAdminMapper;
import com.houjiang.mall.mapper.auth.MallSystemMenuMapper;
import com.houjiang.mall.util.MallAdminTokenHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Mall 管理员认证服务
 * 独立于 permission 服务，使用 hou_mall_admin 和 hou_mall_system_* 表
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MallAuthService {

    private final MallAdminMapper mallAdminMapper;
    private final MallSystemMenuMapper mallSystemMenuMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MallCloudTokenSyncService mallCloudTokenSyncService;
    private static final String MALL_ADMIN_TOKEN_KEY = "MALL_ADMIN_TOKEN:";
    private static final long TOKEN_EXPIRE_HOURS = 24;

    /** 前端路由使用的菜单类型（对齐 like-admin：M=目录, C=菜单） */
    private static final List<String> ROUTE_MENU_TYPES = Arrays.asList("M", "C");

    public ResponseResult login(String account, String password) {
        try {
            if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
                return ResponseResult.fail(400, "账号和密码不能为空");
            }

            QueryWrapper<com.houjiang.mall.entity.auth.MallAdmin> wrapper = new QueryWrapper<>();
            wrapper.eq("account", account);
            wrapper.and(w -> w.eq("delete_time", 0).or().isNull("delete_time"));
            com.houjiang.mall.entity.auth.MallAdmin admin = mallAdminMapper.selectOne(wrapper);

            if (admin == null) {
                log.warn("Mall管理员登录失败，账号不存在: {}", account);
                return ResponseResult.fail(401, "账号或密码错误");
            }

            String md5Password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
            if (!md5Password.equals(admin.getPassword())) {
                log.warn("Mall管理员登录失败，密码错误: {}", account);
                return ResponseResult.fail(401, "账号或密码错误");
            }

            if (admin.getDisable() != null && admin.getDisable() == 1) {
                log.warn("Mall管理员登录失败，账号已禁用: {}", account);
                return ResponseResult.fail(403, "账号已被禁用");
            }

            String token = JwtUtil.createJWT(admin.getId().toString());

            Map<String, Object> adminInfo = buildAdminInfo(admin);
            redisTemplate.opsForValue().set(
                    MALL_ADMIN_TOKEN_KEY + token,
                    adminInfo,
                    TOKEN_EXPIRE_HOURS,
                    TimeUnit.HOURS
            );
            mallCloudTokenSyncService.bindOnLogin(token, admin);
            admin.setLoginTime((int) (System.currentTimeMillis() / 1000));
            mallAdminMapper.updateById(admin);

            log.info("Mall管理员登录成功: {}", account);

            MallAuthLoginVo loginVo = new MallAuthLoginVo();
            loginVo.setToken(token);
            loginVo.setAdminInfo(adminInfo);
            loginVo.setUser(adminInfo);
            return ResponseResult.success(loginVo);
        } catch (Exception e) {
            log.error("Mall管理员登录异常", e);
            return ResponseResult.fail(500, "登录失败: " + e.getMessage());
        }
    }

  /**
     * 获取当前管理员信息（含按钮权限，对齐 like-admin mySelf）
     */
    public ResponseResult getUserInfo(String token) {
        try {
            Map<String, Object> adminInfo = getAdminInfoFromToken(token);
            if (adminInfo == null) {
                return ResponseResult.fail(401, "登录已过期");
            }
            mallCloudTokenSyncService.refreshTtl(token);
            Integer adminId = (Integer) adminInfo.get("id");
            Integer root = (Integer) adminInfo.get("root");

            MallAuthUserInfoVo vo = new MallAuthUserInfoVo();
            vo.setUser(adminInfo);
            vo.setPermissions(resolvePermissions(adminId, root));
            return ResponseResult.success(vo);
        } catch (Exception e) {
            log.error("获取管理员信息异常", e);
            return ResponseResult.fail(500, "获取信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取管理员菜单树（对齐 like-admin selectMenuByRoleId 返回格式）
     */
    public ResponseResult getMenus(String token) {
        try {
            Map<String, Object> adminInfo = getAdminInfoFromToken(token);
            if (adminInfo == null) {
                return ResponseResult.fail(401, "登录已过期");
            }

            Integer adminId = (Integer) adminInfo.get("id");
            Integer root = (Integer) adminInfo.get("root");

            List<MallSystemMenu> menuEntities = loadRouteMenus(adminId, root);
            List<MallAuthMenuVo> menuList = menuEntities.stream()
                    .map(this::toMenuVo)
                    .collect(Collectors.toList());

            return ResponseResult.success(buildMenuTree(menuList));
        } catch (Exception e) {
            log.error("获取菜单异常", e);
            return ResponseResult.fail(500, "获取菜单失败: " + e.getMessage());
        }
    }

    public ResponseResult logout(String token) {
        try {
            String normalized = MallAdminTokenHelper.normalize(token);
            redisTemplate.delete(MALL_ADMIN_TOKEN_KEY + normalized);
            mallCloudTokenSyncService.revokeOnLogout(token);
            log.info("Mall管理员退出登录");
            return ResponseResult.success("退出成功");
        } catch (Exception e) {
            log.error("退出登录异常", e);
            return ResponseResult.fail(500, "退出失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getAdminInfoFromToken(String token) {
        Object adminInfoObj = redisTemplate.opsForValue().get(MALL_ADMIN_TOKEN_KEY + MallAdminTokenHelper.normalize(token));
        if (adminInfoObj == null) {
            return null;
        }
        return (Map<String, Object>) adminInfoObj;
    }

    private Map<String, Object> buildAdminInfo(com.houjiang.mall.entity.auth.MallAdmin admin) {
        MallAuthAdminVo vo = new MallAuthAdminVo();
        vo.setId(admin.getId());
        vo.setAccount(admin.getAccount());
        vo.setName(admin.getName());
        vo.setAvatar(admin.getAvatar());
        vo.setRoot(admin.getRoot());
        vo.setMultipointLogin(admin.getMultipointLogin());
        vo.setDisable(admin.getDisable());

        Map<String, Object> adminInfo = new LinkedHashMap<>();
        adminInfo.put("id", vo.getId());
        adminInfo.put("root", vo.getRoot());
        adminInfo.put("account", vo.getAccount());
        adminInfo.put("name", vo.getName());
        adminInfo.put("avatar", vo.getAvatar());
        adminInfo.put("multipoint_login", vo.getMultipointLogin());
        adminInfo.put("disable", vo.getDisable());
        return adminInfo;
    }

    private List<String> resolvePermissions(Integer adminId, Integer root) {
        if (root != null && root == 1) {
            return Collections.singletonList("*");
        }
        List<String> perms = mallSystemMenuMapper.selectPermsByAdminId(adminId);
        if (perms == null || perms.isEmpty()) {
            return Collections.singletonList("");
        }
        return perms.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<MallSystemMenu> loadRouteMenus(Integer adminId, Integer root) {
        QueryWrapper<MallSystemMenu> wrapper = new QueryWrapper<>();
        wrapper.in("type", ROUTE_MENU_TYPES);
        wrapper.eq("is_disable", 0);
        wrapper.orderByDesc("sort");
        wrapper.orderByAsc("id");

        if (root != null && root == 1) {
            return mallSystemMenuMapper.selectList(wrapper);
        }
        return mallSystemMenuMapper.selectRouteMenusByAdminId(adminId);
    }

    private MallAuthMenuVo toMenuVo(MallSystemMenu menu) {
        MallAuthMenuVo vo = new MallAuthMenuVo();
        BeanUtils.copyProperties(menu, vo);
        vo.setPaths(normalizePath(menu.getPaths()));
        // 兼容历史错误数据 D/B -> M/A
        vo.setType(normalizeMenuType(menu.getType()));
        return vo;
    }

    private String normalizeMenuType(String type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case "D":
                return "M";
            case "B":
                return "A";
            default:
                return type;
        }
    }

    /** 去掉 paths 前导 /，与 like-admin 及前端 createRouteRecord 一致 */
    private String normalizePath(String paths) {
        if (!StringUtils.hasText(paths)) {
            return paths;
        }
        return paths.startsWith("/") ? paths.substring(1) : paths;
    }

    private List<MallAuthMenuVo> buildMenuTree(List<MallAuthMenuVo> menuList) {
        Map<Integer, MallAuthMenuVo> nodeMap = new LinkedHashMap<>();
        for (MallAuthMenuVo menu : menuList) {
            nodeMap.put(menu.getId(), menu);
        }

        List<MallAuthMenuVo> tree = new ArrayList<>();
        for (MallAuthMenuVo menu : menuList) {
            Integer pid = menu.getPid();
            if (pid == null || pid == 0) {
                tree.add(menu);
                continue;
            }
            MallAuthMenuVo parent = nodeMap.get(pid);
            if (parent == null) {
                tree.add(menu);
                continue;
            }
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(menu);
        }
        // 移除空 children，减少前端路由解析干扰
        for (MallAuthMenuVo menu : menuList) {
            if (menu.getChildren() != null && menu.getChildren().isEmpty()) {
                menu.setChildren(null);
            }
        }
        return tree;
    }
}
