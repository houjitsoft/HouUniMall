package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.entity.UserDeptEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 部门管理Mapper接口
 * 
 * @author Amos
 * @since 2025-01-22
 */
@Mapper
public interface UserDeptMapper extends BaseMapper<UserDeptEntity> {

    /**
     * 根据组织ID查询部门列表
     * 
     * @param orgId 组织ID
     * @return 部门列表
     */
    List<UserDeptEntity> selectDeptsByOrgId(@Param("orgId") Long orgId);

    /**
     * 根据组织ID查询部门树结构
     * 包含部门详细信息，通过关联hou_base_department表获取完整信息
     * 
     * @param orgId 组织ID
     * @return 部门树结构
     */
    List<Map<String, Object>> selectDeptTreeByOrgId(@Param("orgId") Long orgId);

    /**
     * 根据部门ID查询部门详细信息
     * 关联hou_base_department表获取完整信息
     * 
     * @param deptId 部门ID
     * @return 部门详细信息
     */
    Map<String, Object> selectDeptDetailById(@Param("deptId") Long deptId);

    /**
     * 从用户部门关联表查询组织ID列表
     *
     * @param userId 用户ID
     * @return org_id 字符串列表（可能为逗号分隔）
     */
    List<String> selectOrgIdsByUserId(@Param("userId") String userId);
}
