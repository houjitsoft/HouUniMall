package com.houjiang.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.permission.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 * 
 * @author Amos
 * @since 2025-01-09
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    /**
     * 根据用户ID获取角色ID列表
     * 
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Select("SELECT ur.role_id FROM hou_permission_user_role ur " +
            "INNER JOIN hou_permission_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND (r.delete_time IS NULL OR r.delete_time = 0)")
    List<Integer> getRoleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID获取用户ID列表
     * 
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM hou_permission_user_role WHERE role_id = #{roleId}")
    List<Long> getUserIdsByRoleId(@Param("roleId") Integer roleId);
    
    /**
     * 根据角色ID获取用户数量
     * 
     * @param roleId 角色ID
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM hou_permission_user_role WHERE role_id = #{roleId}")
    Integer getUserCountByRoleId(@Param("roleId") Integer roleId);
}
