package com.houjiang.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.permission.entity.RoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色菜单关系Mapper接口
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
    
    /**
     * 根据角色ID列表获取菜单ID列表
     * @param roleIds 角色ID列表
     * @return 菜单ID列表
     */
    @Select("<script>SELECT menu_id FROM hou_permission_role_menu " +
            "WHERE role_id IN " +
            "<foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach></script>")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<Integer> getMenuIdsByRoleIds(@Param("roleIds") List<Integer> roleIds);
    
    /**
     * 批量插入角色菜单关系
     * @param roleMenus 角色菜单关系列表
     * @return 插入记录数
     */
    @Insert("<script>" +
            "INSERT INTO hou_permission_role_menu (role_id, menu_id) VALUES " +
            "<foreach collection='roleMenus' item='item' separator=','>" +
            "(#{item.roleId}, #{item.menuId})" +
            "</foreach>" +
            "</script>")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    int insertBatch(@Param("roleMenus") List<RoleMenu> roleMenus);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Delete("DELETE FROM hou_permission_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleIdFast(@Param("roleId") Integer roleId);

    /** 按 menu_id 列表批量插入（比实体列表少一层绑定） */
    @Insert("<script>" +
            "INSERT INTO hou_permission_role_menu (role_id, menu_id) VALUES " +
            "<foreach collection='menuIds' item='menuId' separator=','>" +
            "(#{roleId}, #{menuId})" +
            "</foreach>" +
            "</script>")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    int insertBatchIds(@Param("roleId") Integer roleId, @Param("menuIds") List<Integer> menuIds);

    /** 幂等补充单个角色菜单关系，不覆盖角色已有菜单。 */
    @Insert("INSERT INTO hou_permission_role_menu (role_id, menu_id) " +
            "SELECT #{roleId}, #{menuId} WHERE NOT EXISTS (" +
            "SELECT 1 FROM hou_permission_role_menu WHERE role_id = #{roleId} AND menu_id = #{menuId})")
    int insertIfMissing(@Param("roleId") Integer roleId, @Param("menuId") Integer menuId);
}
