package com.houjiang.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.permission.entity.PermissionMenu;
import com.houjiang.permission.vo.MenuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限菜单Mapper接口
 * 
 * @author Amos
 * @since 2025-01-09
 */
@Mapper
public interface PermissionMenuMapper extends BaseMapper<PermissionMenu> {
    
    /**
     * 获取所有菜单（用于菜单管理）
     */
    @Select("SELECT id, pid, type, name, icon, sort, perms, paths, component, " +
            "selected, params, is_cache, is_show, is_disable, create_time, update_time, " +
            "display_pos, cate_id, plugin_code " +
            "FROM hou_permission_menu " +
            "WHERE is_disable = 0 " +
            "ORDER BY sort DESC, id DESC")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<MenuVO> getAllMenus();

    /**
     * 超管判断（与 auth 服务 AuthPermissionMapper.selectIsAdmin 同源 SQL）：
     * 超管（is_admin=1）有意不绑 RBAC 角色，按角色查菜单恒空，需单独兜底。
     */
    @Select("SELECT is_admin FROM hou_user_info WHERE user_id = #{userId} AND is_delete = 0")
    Integer selectIsAdmin(@Param("userId") Long userId);

    /**
     * 超管侧栏兜底：全部可见导航菜单。
     * 口径与 selectUserMenusByUserId 完全一致（排除 A 级按钮、隐藏、禁用），
     * 仅去掉角色 JOIN —— display_pos 的左侧/插件位/hidden 分流由前端 menuDisplay.js 过滤。
     */
    @Select("SELECT DISTINCT m.id, m.pid, m.type, m.name, m.icon, m.sort, m.perms, m.paths, m.component, " +
            "m.selected, m.params, m.is_cache, m.is_show, m.is_disable, m.create_time, m.update_time, " +
            "m.display_pos, m.cate_id, m.plugin_code " +
            "FROM hou_permission_menu m " +
            "WHERE m.type <> 'A' AND m.is_show <> 0 AND m.is_disable = 0 " +
            "ORDER BY m.sort DESC, m.id DESC")
    List<PermissionMenu> selectActiveNavMenusForAdmin();

    /**
     * 仅 id/pid/禁用标记，供角色保存时内存补全祖先（避免逐条 selectById）
     */
    @Select("SELECT id, pid, is_disable FROM hou_permission_menu")
    List<PermissionMenu> selectAllIdPid();
    
    /**
     * 根据用户角色获取菜单
     */
    @Select("SELECT m.id, m.pid, m.type, m.name, m.icon, m.sort, m.perms, m.paths, m.component, " +
            "m.selected, m.params, m.is_cache, m.is_show, m.is_disable, m.create_time, m.update_time, " +
            "m.display_pos, m.cate_id, m.plugin_code " +
            "FROM hou_permission_menu m " +
            "INNER JOIN hou_permission_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN hou_permission_role r ON rm.role_id = r.id " +
            "WHERE r.id = #{roleId} AND (r.delete_time IS NULL OR r.delete_time = 0) " +
            "AND m.is_disable = 0 AND m.is_show = 1 " +
            "ORDER BY m.sort DESC, m.id DESC")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<MenuVO> getMenusByRole(@Param("roleId") Integer roleId);
    
    /**
     * 根据父级ID获取子菜单
     */
    @Select("SELECT id, pid, type, name, icon, sort, perms, paths, component, " +
            "selected, params, is_cache, is_show, is_disable, create_time, update_time, " +
            "display_pos, cate_id, plugin_code " +
            "FROM hou_permission_menu " +
            "WHERE pid = #{pid} AND is_disable = 0 " +
            "ORDER BY sort DESC, id DESC")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<MenuVO> getMenusByParentId(@Param("pid") Integer pid);
    
    /**
     * 检查用户权限
     */
    @Select("<script>SELECT COUNT(1) FROM hou_permission_menu m " +
            "INNER JOIN hou_permission_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN hou_permission_user_role ur ON rm.role_id = ur.role_id " +
            "INNER JOIN hou_permission_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND m.perms = #{permission} " +
            "AND m.is_disable = 0 AND (r.delete_time IS NULL OR r.delete_time = 0) " +
            "AND m.id IN " +
            "<foreach collection='menuIds' item='menuId' open='(' separator=',' close=')'>" +
            "#{menuId}" +
            "</foreach></script>")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    int checkPermission(@Param("userId") Long userId, @Param("permission") String permission, @Param("menuIds") List<Integer> menuIds);
    
    /**
     * 根据用户ID一次查询其可用的导航菜单（排除A级按钮、隐藏、禁用）
     * 用于 /permission/user/menus 接口加速，避免多次 DB 往返
     * 使用STRAIGHT_JOIN强制连接顺序: ur(1行) → rm(索引) → m(PRIMARY KEY)，避免菜单表全表扫描
     */
    @Select("SELECT STRAIGHT_JOIN DISTINCT m.id, m.pid, m.type, m.name, m.icon, m.sort, m.perms, m.paths, m.component, " +
            "m.selected, m.params, m.is_cache, m.is_show, m.is_disable, m.create_time, m.update_time, " +
            "m.display_pos, m.cate_id, m.plugin_code " +
            "FROM hou_permission_user_role ur " +
            "INNER JOIN hou_permission_role_menu rm ON ur.role_id = rm.role_id " +
            "INNER JOIN hou_permission_role r ON ur.role_id = r.id " +
            "INNER JOIN hou_permission_menu m ON rm.menu_id = m.id " +
            "WHERE ur.user_id = #{userId} AND (r.delete_time IS NULL OR r.delete_time = 0) " +
            "AND m.type <> 'A' AND m.is_show <> 0 AND m.is_disable = 0 " +
            "ORDER BY m.sort DESC, m.id DESC")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<PermissionMenu> selectUserMenusByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID一次查询其权限标识列表（用于 /permission/user/permissions 加速）
     * 使用STRAIGHT_JOIN强制连接顺序: ur(1行) → rm(索引) → m(PRIMARY KEY)，避免菜单表全表扫描
     */
    @Select("SELECT STRAIGHT_JOIN DISTINCT m.perms " +
            "FROM hou_permission_user_role ur " +
            "INNER JOIN hou_permission_role_menu rm ON ur.role_id = rm.role_id " +
            "INNER JOIN hou_permission_role r ON ur.role_id = r.id " +
            "INNER JOIN hou_permission_menu m ON rm.menu_id = m.id " +
            "WHERE ur.user_id = #{userId} AND (r.delete_time IS NULL OR r.delete_time = 0) " +
            "AND m.is_disable = 0 AND m.perms IS NOT NULL AND m.perms <> ''")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<String> selectUserPermissionPermsByUserId(@Param("userId") Long userId);

    /**
     * 根据菜单ID列表获取权限标识列表
     */
    @Select("<script>SELECT perms FROM hou_permission_menu " +
            "WHERE id IN " +
            "<foreach collection='menuIds' item='menuId' open='(' separator=',' close=')'>" +
            "#{menuId}" +
            "</foreach> " +
            "AND is_disable = 0 AND perms IS NOT NULL AND perms != ''</script>")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<String> getPermissionsByMenuIds(@Param("menuIds") List<Integer> menuIds);
}
