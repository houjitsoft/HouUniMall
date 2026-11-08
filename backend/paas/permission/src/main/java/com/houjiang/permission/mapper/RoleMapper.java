package com.houjiang.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.permission.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    
    /**
     * 根据角色ID获取用户数量
     * 
     * @param roleId 角色ID
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM hou_permission_user_role WHERE role_id = #{roleId}")
    Integer getCountByRoleId(@Param("roleId") Integer roleId);

    /**
     * 按用户 ID 联表查询角色名称（列表「角色组」展示用，避免分步查询不一致）
     */
    @Select("SELECT r.name FROM hou_permission_role r "
            + "INNER JOIN hou_permission_user_role ur ON ur.role_id = r.id "
            + "WHERE ur.user_id = #{userId} "
            + "AND (r.delete_time IS NULL OR r.delete_time = 0) "
            + "ORDER BY r.sort DESC, r.id ASC")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<String> selectRoleNamesByUserId(@Param("userId") Long userId);
}
