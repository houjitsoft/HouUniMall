package com.houjiang.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.permission.entity.UserRoleRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关系Mapper接口
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Mapper
public interface UserRoleRelationMapper extends BaseMapper<UserRoleRelation> {
    
    /**
     * 根据用户ID获取角色ID列表
     * 
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Select("SELECT role_id FROM hou_permission_user_role WHERE user_id = #{userId}")
    List<Integer> getRoleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID获取用户ID列表
     * 
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM hou_permission_user_role WHERE role_id = #{roleId}")
    List<Long> getUserIdsByRoleId(@Param("roleId") Integer roleId);
}
