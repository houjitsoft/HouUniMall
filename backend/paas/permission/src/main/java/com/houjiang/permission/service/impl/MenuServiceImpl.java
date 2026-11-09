package com.houjiang.permission.service.impl;

import com.houjiang.permission.controller.PermissionController.MenuCreateRequest;
import com.houjiang.permission.controller.PermissionController.MenuUpdateRequest;
import com.houjiang.permission.entity.PermissionMenu;
import com.houjiang.permission.feign.PluginCategoryClient;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.permission.mapper.PermissionMenuMapper;
import com.houjiang.permission.mapper.UserRoleMapper;
import com.houjiang.permission.plugin.MenuContributionGateway;
import com.houjiang.permission.service.MenuService;
import com.houjiang.permission.service.RoleMenuService;
import com.houjiang.permission.support.MenuAncestorSupport;
import com.houjiang.permission.vo.MenuVO;
import com.houjiang.commons.config.RedisCache;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 * 
 * @author Amos
 * @since 2025-01-09
 */
@Slf4j
@Service
public class MenuServiceImpl implements MenuService {
    
    @Autowired
    private PermissionMenuMapper permissionMenuMapper;

    @Autowired
    private PluginCategoryClient pluginCategoryClient;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private RoleMenuService roleMenuService;
    
    @Autowired(required = false)
    private RedisCache redisCache;

    @Autowired
    private MenuContributionGateway menuContributionGateway;
    
    /** Jackson序列化器，用于Redis缓存的可靠序列化/反序列化（避免FastJSON AutoType问题） */
    private static final ObjectMapper jacksonMapper = new ObjectMapper();
    private static final TypeReference<List<MenuVO>> MENU_LIST_TYPE = new TypeReference<List<MenuVO>>() {};

    /** 菜单统一排序：sort DESC，sort 相同则 id DESC（与 /menu/list、子菜单树口径一致） */
    private static final Comparator<MenuVO> MENU_ORDER =
            Comparator.comparingInt((MenuVO m) -> m.getSort() == null ? 0 : m.getSort()).reversed()
                    .thenComparing(Comparator.comparingInt((MenuVO m) -> m.getId() == null ? 0 : m.getId()).reversed());

    @Override
    public List<MenuVO> getAllMenus() {
        log.info("获取所有菜单（用于菜单管理，返回树形结构）");
        List<MenuVO> menus = permissionMenuMapper.getAllMenus();
        log.info("从数据库查询到的菜单数量: {}", menus != null ? menus.size() : 0);
        // 菜单管理页面需要显示所有菜单项的树形结构
        return buildMenuAllTree(menus);
    }

    @Override
    public List<MenuVO> getMenuTreeForRole() {
        log.info("获取菜单权限树（用于角色权限管理，包含按钮级别）");
        List<MenuVO> menus = permissionMenuMapper.getAllMenus();
        log.info("从数据库查询到的菜单数量: {}", menus != null ? menus.size() : 0);

        // 不过滤按钮权限，返回完整的树形结构
        return buildMenuTree(menus);
    }
    
    @Override
    public List<MenuVO> getMenusByRole(Integer roleId) {
        log.info("根据角色获取菜单: roleId={}", roleId);
        List<MenuVO> menus = permissionMenuMapper.getMenusByRole(roleId);
        return buildMenuTree(menus);
    }
    
    @Override
    public List<MenuVO> buildMenuAllTree(List<MenuVO> menus) {
        // 按父级ID分组
        Map<Integer, List<MenuVO>> menuMap = menus.stream()
                .collect(Collectors.groupingBy(MenuVO::getPid));

        // 构建树形结构
        List<MenuVO> rootMenus = menuMap.getOrDefault(0, new ArrayList<>());

        for (MenuVO menu : rootMenus) {
            buildChildren(menu, menuMap);
        }

        return rootMenus;
    }
    

    @Override
    public List<MenuVO> buildMenuTree(List<MenuVO> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }

        // 按 pid 分组：LinkedHashMap 保持 pid 首次出现的顺序（此前为 HashMap，与注释不符）
        Map<Integer, List<MenuVO>> menuMap = new LinkedHashMap<>(menus.size());
        for (MenuVO menu : menus) {
            menuMap.computeIfAbsent(menu.getPid(), k -> new ArrayList<>()).add(menu);
        }

        // 分组内显式二级排序（sort DESC, id DESC）：不再依赖各 SQL 的 ORDER BY 口径，
        // 否则 /user/menus（原 id ASC）与 /menu/list（id DESC）在 sort 相同时顺序相反
        menuMap.values().forEach(list -> list.sort(MENU_ORDER));

        // 构建树形结构
        List<MenuVO> rootMenus = menuMap.getOrDefault(0, new ArrayList<>());
        for (MenuVO menu : rootMenus) {
            buildChildren(menu, menuMap);
        }

        return rootMenus;
    }
    
    /**
     * 递归构建子菜单
     */
    private void buildChildren(MenuVO parent, Map<Integer, List<MenuVO>> menuMap) {
        List<MenuVO> children = menuMap.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            parent.setChildren(children);
            for (MenuVO child : children) {
                buildChildren(child, menuMap);
            }
        }
    }
    
    @Override
    public MenuVO getMenuDetail(Integer id) {
        log.info("获取菜单详情: id={}", id);
        PermissionMenu menu = permissionMenuMapper.selectById(id);
        Assert.notNull(menu, "菜单不存在");
        return convertToMenuVODirect(menu);
    }
    
    @Override
    public void addMenu(MenuCreateRequest request) {
        log.info("新增菜单: {}", request);
        // 添加调试日志
        log.info("parentId值: {}", request.getParentId());
        
        PermissionMenu menu = new PermissionMenu();
        Integer parentId = request.getParentId() != null ? request.getParentId() : 0;
        menu.setPid(parentId);
        // 添加调试日志
        log.info("设置pid为: {}", parentId);
        menu.setType(request.getType());
        menu.setName(request.getName());
        menu.setIcon(request.getIcon());
        menu.setSort(request.getSort() != null ? request.getSort() : 0);
        menu.setPerms(request.getPerms());
        menu.setPaths(request.getPaths());
        menu.setComponent(request.getComponent());
        menu.setSelected(request.getSelected());
        menu.setParams(request.getParams());
        menu.setIsCache(request.getIsCache() != null ? request.getIsCache() : 0);
        menu.setIsShow(request.getIsShow() != null ? request.getIsShow() : 1);
        menu.setIsDisable(request.getIsDisable() != null ? request.getIsDisable() : 0);
        menu.setCreateTime((int) (System.currentTimeMillis() / 1000));
        menu.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        
        inheritParentOwnership(menu);
        if (isPluginRoot(menu)) {
            if (!Integer.valueOf(0).equals(menu.getPid())) throw new IllegalArgumentException("插件入口必须是一级菜单");
            menu.setDisplayPos("plugin");
            menu.setPluginCode(pluginCode(menu));
        }
        // 添加调试日志
        log.info("准备插入菜单对象: pid={}, name={}", menu.getPid(), menu.getName());
        permissionMenuMapper.insert(menu);
        // 添加调试日志
        log.info("新增菜单成功: id={}, pid={}", menu.getId(), menu.getPid());
        
        MenuAncestorSupport.invalidatePidCache();
        // 清除所有用户的菜单缓存
        clearAllUserMenusCache();
    }
    
    @Override
    public void editMenu(MenuUpdateRequest request) {
        log.info("编辑菜单: {}", request);
        
        PermissionMenu menu = permissionMenuMapper.selectById(request.getId());
        Assert.notNull(menu, "菜单不存在");
        int parentId = request.getParentId() == null ? 0 : request.getParentId();
        if (CORE_ROOT_SORT.containsKey(menu.getId())) {
            if (parentId != 0 || !CORE_ROOT_SORT.get(menu.getId()).equals(request.getSort())) {
                throw new IllegalArgumentException("核心入口的父级和顺序不可修改");
            }
            if (!java.util.Objects.equals(menu.getPaths(), request.getPaths())
                    || !java.util.Objects.equals(menu.getPerms(), request.getPerms())) {
                throw new IllegalArgumentException("核心入口的路由和权限标识不可修改");
            }
        }
        boolean pluginRoot = isPluginRoot(menu)
                || (request.getPaths() != null && request.getPaths().matches("/plugin/[^/]+/?"));
        if (pluginRoot && parentId != 0) throw new IllegalArgumentException("插件入口必须是一级菜单");

        if (!Integer.valueOf(0).equals(menu.getPid())) {
            PermissionMenu currentRoot = rootOf(menu);
            if (parentId == 0) throw new IllegalArgumentException("子菜单不能提升为新的应用入口");
            PermissionMenu nextParent = permissionMenuMapper.selectById(parentId);
            Assert.notNull(nextParent, "父级菜单不存在");
            if (!java.util.Objects.equals(currentRoot.getId(), rootOf(nextParent).getId())) {
                throw new IllegalArgumentException("子菜单不能移到其他业务模块");
            }
            assertNoParentCycle(menu.getId(), nextParent);
        }
        String originalPlugin = pluginCode(menu);
        String requestedPlugin = pluginCodeFromPath(request.getPaths());
        if (!originalPlugin.isBlank() && !requestedPlugin.isBlank() && !originalPlugin.equals(requestedPlugin)) {
            throw new IllegalArgumentException("菜单路由不能指向其他插件");
        }
        menu.setType(request.getType());
        menu.setName(request.getName());
        menu.setIcon(request.getIcon());
        menu.setSort(request.getSort() != null ? request.getSort() : 0);
        menu.setPaths(request.getPaths());
        menu.setPerms(request.getPerms());
        menu.setComponent(request.getComponent());
        menu.setPid(request.getParentId() != null ? request.getParentId() : 0);
        menu.setSelected(request.getSelected());
        menu.setParams(request.getParams());
        menu.setIsCache(request.getIsCache() != null ? request.getIsCache() : 0);
        menu.setIsShow(request.getIsShow() != null ? request.getIsShow() : 1);
        menu.setIsDisable(request.getIsDisable() != null ? request.getIsDisable() : 0);
        menu.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        
        inheritParentOwnership(menu);
        if (pluginRoot && !"hidden".equals(menu.getDisplayPos())) menu.setDisplayPos("plugin");
        permissionMenuMapper.updateById(menu);
        log.info("编辑菜单成功: id={}", menu.getId());
        
        MenuAncestorSupport.invalidatePidCache();
        // 清除所有用户的菜单缓存
        clearAllUserMenusCache();
    }
    
    @Override
    public void deleteMenu(Integer id) {
        log.info("删除菜单: id={}", id);
        
        PermissionMenu menu = permissionMenuMapper.selectById(id);
        Assert.notNull(menu, "菜单不存在");
        
        if (CORE_ROOT_SORT.containsKey(id)) throw new IllegalArgumentException("固定核心入口不可删除");

        // 检查是否有子菜单
        List<PermissionMenu> children = permissionMenuMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PermissionMenu>()
                .eq("pid", id)
        );
        Assert.isTrue(children.isEmpty(), "请先删除子菜单再操作");
        
        permissionMenuMapper.deleteById(id);
        log.info("删除菜单成功: id={}", id);
        
        MenuAncestorSupport.invalidatePidCache();
        // 清除所有用户的菜单缓存
        clearAllUserMenusCache();
    }
    
    /**
     * 获取用户导航菜单（不包含A级按钮权限）
     * 此方法专门为前端导航菜单服务，只返回M级（目录）和C级（菜单）
     * A级按钮权限应该通过单独的权限接口管理，不在导航菜单中显示
     * @param userId 用户ID
     * @return 用户导航菜单列表（不包含A级按钮）
     */
    @Override
    public List<MenuVO> getUserMenus(Long userId) {
        try {
            String cacheKey = "user:menus:" + userId;
            
            // 1. 优先从Redis读取（用Jackson反序列化，避免FastJSON AutoType问题）
            if (redisCache != null) {
                try {
                    Object cached = redisCache.getCacheObject(cacheKey);
                    if (cached != null) {
                        List<MenuVO> menus = deserializeMenuCache(cached);
                        if (menus != null && !menus.isEmpty()) {
                            log.debug("用户菜单命中缓存，userId={}, count={}", userId, menus.size());
                            return withPluginMenus(menus);
                        }
                    }
                } catch (Exception e) {
                    log.warn("菜单缓存读取失败，走库查，userId={}, error={}", userId, e.getMessage());
                }
            }

            // 2. 单次SQL查询用户菜单（user_role → role_menu → menu），已按sort DESC, id排序
            List<PermissionMenu> menus = permissionMenuMapper.selectUserMenusByUserId(userId);
            if (menus == null || menus.isEmpty()) {
                // 🔴 超管兜底：超管（is_admin=1）有意不绑 RBAC 角色——一旦绑角色，auth 侧权限集
                // 就不再命中 ["*"] fallback（见 auth 的 UserDetailsServiceImpl），超管会被
                // 精确比对鉴权 403。因此超管按角色查菜单恒空，若不兜底，最高管理员登录后
                // 左侧菜单全空（2026-10-28 实测）。语义与鉴权层对齐：角色菜单优先，
                // 超管全量仅作空集兜底（超管真绑了角色时以角色菜单为准）。
                if (Integer.valueOf(1).equals(permissionMenuMapper.selectIsAdmin(userId))) {
                    menus = permissionMenuMapper.selectActiveNavMenusForAdmin();
                    log.info("超管无角色菜单，兜底全部可见导航菜单: userId={}, count={}",
                            userId, menus == null ? 0 : menus.size());
                } else {
                    log.debug("用户无菜单数据，userId={}", userId);
                    return new ArrayList<>();
                }
            }
            // 补全父级目录（如仅勾选 OA 子菜单未含 id=7 时，侧栏需补 OA办公 一级菜单）
            menus = MenuAncestorSupport.expandMenusWithAncestors(permissionMenuMapper, menus);

            // 3. 直接字段赋值替代BeanUtils反射拷贝
            List<MenuVO> menuVOs = new ArrayList<>(menus.size());
            for (PermissionMenu m : menus) {
                menuVOs.add(convertToMenuVODirect(m));
            }
            List<MenuVO> menuTree = buildMenuTree(menuVOs);

            // 4. 存入Redis（用Jackson序列化为JSON字符串，确保反序列化可靠）
            if (redisCache != null && !menuTree.isEmpty()) {
                try {
                    String jsonStr = jacksonMapper.writeValueAsString(menuTree);
                    redisCache.setCacheObject(cacheKey, jsonStr, 60, TimeUnit.MINUTES);
                    log.debug("用户菜单已缓存(json)，userId={}, ttl=60min, size={}B", userId, jsonStr.length());
                } catch (Exception e) {
                    log.warn("菜单写入缓存失败，userId={}", userId, e);
                }
            }
            return withPluginMenus(menuTree);
        } catch (Exception e) {
            log.error("获取用户菜单失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 合并二开插件经 {@link MenuContributionGateway} 贡献的菜单入口。
     *
     * <p>只在返回结果上追加（不写回 Redis 缓存），因此插件贡献项不落缓存、无「缓存过期才生效」问题，
     * 且重复调用幂等（去重见 {@code MenuContributionGateway}）。无插件时为一次空快照读，零开销。
     */
    private List<MenuVO> withPluginMenus(List<MenuVO> official) {
        return menuContributionGateway.contributeAndMerge(null, official);
    }
    
    /**
     * 从Redis缓存反序列化菜单树
     * 兼容两种格式：JSON字符串（新格式）和直接对象（旧格式/FastJSON）
     * @param cached Redis返回的缓存对象
     * @return 菜单树列表，反序列化失败返回null
     */
    @SuppressWarnings("unchecked")
    private List<MenuVO> deserializeMenuCache(Object cached) {
        try {
            if (cached instanceof String) {
                // 新格式：Jackson序列化的JSON字符串
                String json = (String) cached;
                if (!json.isEmpty()) {
                    return jacksonMapper.readValue(json, MENU_LIST_TYPE);
                }
            } else if (cached instanceof List) {
                // 旧格式兼容：FastJSON返回的List<JSONObject>，通过Jackson转换
                return jacksonMapper.convertValue(cached, MENU_LIST_TYPE);
            }
        } catch (Exception e) {
            log.warn("菜单缓存反序列化失败，将重新查库: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取子菜单树（不包含A级按钮权限）
     * 用于微前端子服务获取完整的导航菜单树，不包含A级按钮权限
     * @param userId 用户ID
     * @param parentMenuId 一级菜单ID
     * @return 子导航菜单树（不包含A级按钮）
     */
    @Override
    public List<MenuVO> getSubMenuTree(Long userId, Integer parentMenuId) {
        try {
            log.info("开始获取子菜单树，userId={}, parentMenuId={}", userId, parentMenuId);
            
            // 获取用户角色
            List<Integer> roleIds = userRoleMapper.getRoleIdsByUserId(userId);
            log.info("用户角色ID列表: {}", roleIds);
            if (roleIds == null || roleIds.isEmpty()) {
                log.warn("用户没有分配角色，userId={}", userId);
                return new ArrayList<>();
            }
            
            // 获取角色菜单权限
            List<Integer> menuIds = roleMenuService.getMenuIdsByRoleIds(roleIds);
            log.info("角色对应的菜单ID列表: {}", menuIds);
            if (menuIds == null || menuIds.isEmpty()) {
                log.warn("角色没有分配菜单权限，roleIds={}", roleIds);
                return new ArrayList<>();
            }
            
            // 🔥 关键修改：在数据库查询层面就排除A级菜单
            List<PermissionMenu> allUserMenus = permissionMenuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PermissionMenu>()
                    // 只获取用户有权限的菜单
                    .in("id", menuIds)
                    // 🔥 数据库层面排除A级菜单
                    .ne("type", "A")
                    // 获取非隐藏的菜单
                    .ne("is_show", 0)
                    // 只获取未禁用的菜单
                    .eq("is_disable", 0)
            );
            log.info("查询到的用户导航菜单数量（已排除A级）: {}", allUserMenus.size());
            
            if (allUserMenus.isEmpty()) {
                log.warn("用户没有菜单权限，userId={}, parentMenuId={}", userId, parentMenuId);
                return new ArrayList<>();
            }
            
            // 直接转换为VO，数据库已经过滤了A级菜单
            List<MenuVO> menuVOs = allUserMenus.stream()
                .map(this::convertToMenuVO)
                .collect(Collectors.toList());
            log.info("转换后的导航菜单VO数量: {}", menuVOs.size());
            
            // 直接构建树形结构，返回指定父菜单下的子菜单树
            List<MenuVO> menuTree = buildSubMenuTreeDirectly(menuVOs, parentMenuId);
            log.info("构建子菜单树完成，最终菜单数量: {}", menuTree.size());
            
            return menuTree;
        } catch (Exception e) {
            log.error("获取子菜单树失败: userId={}, parentMenuId={}", userId, parentMenuId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 优化版：一次性查询所有相关菜单，然后在内存中构建树形结构
     * @param parentMenuId 父菜单ID
     * @param userMenuIds 用户有权限的菜单ID列表
     * @return 所有子菜单列表
     */
    private List<PermissionMenu> getAllSubMenusRecursively(Integer parentMenuId, List<Integer> userMenuIds) {
            // 🔥 关键修改：在数据库查询层面就排除A级菜单
            List<PermissionMenu> allUserMenus = permissionMenuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PermissionMenu>()
                    // 只获取用户有权限的菜单
                    .in("id", userMenuIds)
                    // 🔥 数据库层面排除A级菜单
                    .ne("type", "A")
                    // 获取非隐藏的菜单
                    .ne("is_show", 0)
                    // 只获取未禁用的菜单
                    .eq("is_disable", 0)
            );
        
        // 在内存中构建树形结构，获取指定父菜单下的所有子菜单
        return buildSubMenusInMemory(allUserMenus, parentMenuId);
    }
    
    /**
     * 在内存中构建子菜单树
     * @param allMenus 所有菜单
     * @param parentMenuId 父菜单ID
     * @return 子菜单列表
     */
    private List<PermissionMenu> buildSubMenusInMemory(List<PermissionMenu> allMenus, Integer parentMenuId) {
        List<PermissionMenu> result = new ArrayList<>();
        
        // 按父级ID分组
        Map<Integer, List<PermissionMenu>> menuMap = allMenus.stream()
            .collect(Collectors.groupingBy(PermissionMenu::getPid));
        
        // 递归构建子菜单
        buildSubMenusRecursively(menuMap, parentMenuId, result);
        
        return result;
    }
    
    /**
     * 递归构建子菜单（内存操作，无数据库查询）
     */
    private void buildSubMenusRecursively(Map<Integer, List<PermissionMenu>> menuMap, 
                                         Integer parentMenuId, List<PermissionMenu> result) {
        List<PermissionMenu> children = menuMap.get(parentMenuId);
        if (children != null) {
            for (PermissionMenu child : children) {
                result.add(child);
                // 递归添加子菜单的子菜单
                buildSubMenusRecursively(menuMap, child.getId(), result);
            }
        }
    }
    
    /**
     * 优化版：直接构建子菜单树（专门用于子菜单，不限制pid=0）
     * @param menus 菜单列表
     * @param parentMenuId 父菜单ID
     * @return 子菜单树
     */
    private List<MenuVO> buildSubMenuTreeDirectly(List<MenuVO> menus, Integer parentMenuId) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }

        // 按父级ID分组，并对每个分组内的菜单按sort字段排序
        Map<Integer, List<MenuVO>> menuMap = menus.stream()
            .collect(Collectors.groupingBy(
                MenuVO::getPid,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> list.stream()
                        .sorted((a, b) -> {
                            // 按sort字段降序排序，如果sort相同则按id降序排序
                            int sortCompare = Integer.compare(b.getSort() != null ? b.getSort() : 0, 
                                                           a.getSort() != null ? a.getSort() : 0);
                            if (sortCompare == 0) {
                                return Integer.compare(b.getId() != null ? b.getId() : 0, 
                                                     a.getId() != null ? a.getId() : 0);
                            }
                            return sortCompare;
                        })
                        .collect(Collectors.toList())
                )
            ));

        // 构建树形结构，从指定的父菜单ID开始
        List<MenuVO> rootMenus = menuMap.getOrDefault(parentMenuId, new ArrayList<>());

        for (MenuVO menu : rootMenus) {
            buildChildren(menu, menuMap);
        }

        return rootMenus;
    }
    
    /**
     * 构建子菜单树（专门用于子菜单，不限制pid=0）
     * @param menus 菜单列表
     * @param parentMenuId 父菜单ID
     * @return 子菜单树
     */
    private List<MenuVO> buildSubMenuTree(List<MenuVO> menus, Integer parentMenuId) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }

        // 按父级ID分组，并对每个分组内的菜单按sort字段排序
        Map<Integer, List<MenuVO>> menuMap = menus.stream()
            .collect(Collectors.groupingBy(
                MenuVO::getPid,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> list.stream()
                        .sorted((a, b) -> {
                            // 按sort字段降序排序，如果sort相同则按id降序排序
                            int sortCompare = Integer.compare(b.getSort() != null ? b.getSort() : 0, 
                                                           a.getSort() != null ? a.getSort() : 0);
                            if (sortCompare == 0) {
                                return Integer.compare(b.getId() != null ? b.getId() : 0, 
                                                     a.getId() != null ? a.getId() : 0);
                            }
                            return sortCompare;
                        })
                        .collect(Collectors.toList())
                )
            ));

        // 构建树形结构，从指定的父菜单ID开始
        List<MenuVO> rootMenus = menuMap.getOrDefault(parentMenuId, new ArrayList<>());

        for (MenuVO menu : rootMenus) {
            buildChildren(menu, menuMap);
        }

        return rootMenus;
    }

    /**
     * 转换PermissionMenu为MenuVO（反射方式，兼容用）
     */
    private MenuVO convertToMenuVO(PermissionMenu menu) {
        return convertToMenuVODirect(menu);
    }
    
    /**
     * 直接字段赋值转换PermissionMenu为MenuVO（无反射开销，高性能）
     * @param m 数据库实体
     * @return 菜单VO
     */
    private MenuVO convertToMenuVODirect(PermissionMenu m) {
        MenuVO vo = new MenuVO();
        vo.setId(m.getId());
        vo.setPid(m.getPid());
        vo.setType(m.getType());
        vo.setName(m.getName());
        vo.setIcon(m.getIcon());
        vo.setSort(m.getSort());
        vo.setPerms(m.getPerms());
        vo.setPaths(m.getPaths());
        vo.setComponent(m.getComponent());
        vo.setSelected(m.getSelected());
        vo.setParams(m.getParams());
        vo.setIsCache(m.getIsCache());
        vo.setIsShow(m.getIsShow());
        vo.setIsDisable(m.getIsDisable());
        vo.setDisplayPos(m.getDisplayPos());
        vo.setPluginCode(m.getPluginCode());
        vo.setCateId(m.getCateId());
        vo.setCreateTime(m.getCreateTime());
        vo.setUpdateTime(m.getUpdateTime());
        return vo;
    }
    
    /**
     * 清除所有用户的菜单缓存
     * 当菜单数据发生变化时调用（新增、编辑、删除菜单）
     */
    @Override
    public void clearUserMenusCache(Long userId) {
        if (redisCache == null || userId == null) {
            return;
        }
        try {
            redisCache.deleteObject("user:menus:" + userId);
            redisCache.deleteObject("user:permissions:" + userId);
            log.info("已清除用户菜单缓存 userId={}", userId);
        } catch (Exception e) {
            log.warn("清除用户菜单缓存失败 userId={}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void clearAllUserMenusCache() {
        if (redisCache != null) {
            try {
                java.util.Collection<String> menuKeys = redisCache.keys("user:menus:*");
                if (menuKeys != null && !menuKeys.isEmpty()) {
                    redisCache.deleteObject(menuKeys);
                    log.info("已清除 {} 个用户菜单缓存", menuKeys.size());
                }
                java.util.Collection<String> permKeys = redisCache.keys("user:permissions:*");
                if (permKeys != null && !permKeys.isEmpty()) {
                    redisCache.deleteObject(permKeys);
                    log.info("已清除 {} 个用户权限缓存", permKeys.size());
                }
            } catch (Exception e) {
                log.warn("清除用户菜单/权限缓存失败: {}", e.getMessage());
            }
        }
    }

    private static final Set<String> VALID_DISPLAY_POS = Set.of("left", "plugin", "hidden");
    private static final Map<Integer, Integer> CORE_ROOT_SORT = Map.of(
            19, 10000, 1, 9000, 1009, 8500, 15, 8000, 4, 7000, 18, 6000);

    @Override
    public void updateMenuDisplay(Integer id, String displayPos, Integer cateId) {
        if (id == null) throw new IllegalArgumentException("菜单ID不能为空");
        PermissionMenu menu = permissionMenuMapper.selectById(id);
        if (menu == null) throw new IllegalArgumentException("菜单不存在: " + id);
        String pos = displayPos == null ? "left" : displayPos.trim().toLowerCase(Locale.ROOT);
        if (!VALID_DISPLAY_POS.contains(pos)) throw new IllegalArgumentException("菜单显示位置不合法");
        if (CORE_ROOT_SORT.containsKey(id) && !"left".equals(pos)) {
            throw new IllegalArgumentException("六个核心入口必须固定在左侧");
        }
        boolean pluginRoot = isPluginRoot(menu);
        // 核心入口（含支付中心 id=15）必须允许 left；路径已回归 /payment
        if (pluginRoot && "left".equals(pos) && !CORE_ROOT_SORT.containsKey(id)) throw new IllegalArgumentException("插件入口不能移到左侧");
        if ((pluginRoot || "plugin".equals(pos)) && !Integer.valueOf(0).equals(menu.getPid())) {
            throw new IllegalArgumentException("插件入口必须是一级菜单，不能挂在核心菜单下");
        }
        if (cateId != null && cateId < 0) throw new IllegalArgumentException("分类ID不能为负数");
        int category = "plugin".equals(pos) ? (cateId == null ? 0 : cateId)
                : pluginRoot && menu.getCateId() != null ? menu.getCateId() : 0;
        if (pluginRoot) {
            String code = pluginCode(menu);
            if (code.isBlank()) throw new IllegalArgumentException("插件入口缺少插件编码");
            requireCategorySuccess(pluginCategoryClient.updateCategory(Map.of("pluginCode", code, "cateId", category)));
        } else if ("plugin".equals(pos) && category > 0) {
            boolean exists = getAddonCateList().stream().anyMatch(row -> String.valueOf(category).equals(String.valueOf(row.get("id"))));
            if (!exists) throw new IllegalArgumentException("插件分类不存在: " + category);
        }
        menu.setDisplayPos(pos);
        menu.setCateId(category);
        menu.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        permissionMenuMapper.updateById(menu);
        clearAllUserMenusCache();
    }

    /** 路由 /plugin/code 以及已登记的一级插件入口（包括 /lowcode）。 */
    private static boolean isPluginRoot(PermissionMenu menu) {
        String path = menu.getPaths() == null ? "" : menu.getPaths();
        return "plugin".equals(menu.getDisplayPos()) || path.matches("/plugin/[^/]+/?")
                || "/lowcode".equals(path)
                || (Integer.valueOf(0).equals(menu.getPid()) && menu.getPluginCode() != null && !menu.getPluginCode().isBlank());
    }

    private static String pluginCode(PermissionMenu menu) {
        if (menu.getPluginCode() != null && !menu.getPluginCode().isBlank()) return menu.getPluginCode().trim();
        String path = menu.getPaths() == null ? "" : menu.getPaths();
        if (path.startsWith("/plugin/")) return path.substring(8).split("/", 2)[0];
        return "/lowcode".equals(path) ? "lowcode" : "";
    }

    /** 子菜单的插件归属与分类只继承本模块根，不允许借路由跨应用。 */
    private void inheritParentOwnership(PermissionMenu menu) {
        if (menu.getPid() == null || menu.getPid() == 0) return;
        PermissionMenu parent = permissionMenuMapper.selectById(menu.getPid());
        Assert.notNull(parent, "父级菜单不存在");
        PermissionMenu root = rootOf(parent);
        String owner = pluginCode(root);
        String routeOwner = pluginCodeFromPath(menu.getPaths());
        if (!routeOwner.isBlank() && !routeOwner.equals(owner)) {
            throw new IllegalArgumentException("子菜单路由必须属于父级应用");
        }
        menu.setPluginCode(owner);
        menu.setCateId(owner.isBlank() || root.getCateId() == null ? 0 : root.getCateId());
        if (!"hidden".equals(menu.getDisplayPos())) menu.setDisplayPos("left");
    }

    private PermissionMenu rootOf(PermissionMenu menu) {
        Set<Integer> seen = new HashSet<>();
        while (menu.getPid() != null && menu.getPid() != 0) {
            if (!seen.add(menu.getId())) throw new IllegalArgumentException("菜单父级存在循环");
            menu = permissionMenuMapper.selectById(menu.getPid());
            Assert.notNull(menu, "父级菜单不存在");
        }
        return menu;
    }

    private void assertNoParentCycle(Integer childId, PermissionMenu parent) {
        Set<Integer> seen = new HashSet<>();
        while (parent != null) {
            if (java.util.Objects.equals(childId, parent.getId()) || !seen.add(parent.getId())) {
                throw new IllegalArgumentException("菜单不能放到自身或后代下面");
            }
            if (parent.getPid() == null || parent.getPid() == 0) return;
            parent = permissionMenuMapper.selectById(parent.getPid());
        }
    }

    private static String pluginCodeFromPath(String path) {
        if (path != null && path.startsWith("/plugin/")) return path.substring(8).split("/", 2)[0];
        return "/lowcode".equals(path) ? "lowcode" : "";
    }

    private static void requireCategorySuccess(ResponseResult<?> response) {
        if (response == null || response.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            throw new IllegalStateException("插件分类服务不可用或拒绝了请求，请稍后重试");
        }
    }

    /** 旧调用兼容入口；分类数据仍只从 base 获取。 */
    @Override
    public List<Map<String, Object>> getAddonCateList() {
        ResponseResult<List<Map<String, Object>>> result = pluginCategoryClient.categories();
        requireCategorySuccess(result);
        return result.getData() == null ? new ArrayList<>() : result.getData();
    }
}
