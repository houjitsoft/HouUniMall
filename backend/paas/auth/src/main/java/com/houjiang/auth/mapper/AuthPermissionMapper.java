package com.houjiang.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 登录认证阶段只读查询用户当前有效权限码。
 */
@Mapper
public interface AuthPermissionMapper {

    @Select("SELECT DISTINCT m.perms " +
            "FROM hou_permission_user_role ur " +
            "INNER JOIN hou_permission_role r ON r.id = ur.role_id AND r.delete_time IS NULL " +
            "INNER JOIN hou_permission_role_menu rm ON rm.role_id = ur.role_id " +
            "INNER JOIN hou_permission_menu m ON m.id = rm.menu_id " +
            "WHERE ur.user_id = #{userId} AND m.is_disable = 0 " +
            "AND m.perms IS NOT NULL AND m.perms <> '' " +
            "ORDER BY m.perms")
    List<String> selectPermissionCodes(@Param("userId") Long userId);

    /**
     * 超管标记（hou_user_info.is_admin，1=平台超管）。
     * 平台约定：超管的会话权限集含字面 "*"（生产 ① 实测形态：无 RBAC 角色、
     * permissions=["*"]）。依赖「通配放行」鉴权的模块（插件发布、mall 管理等）
     * 都按这个形态适配；缺了它，超管会被精确比对拒掉（403）。
     */
    @Select("SELECT is_admin FROM hou_user_info WHERE user_id = #{userId} AND is_delete = 0")
    Integer selectIsAdmin(@Param("userId") Long userId);
}
