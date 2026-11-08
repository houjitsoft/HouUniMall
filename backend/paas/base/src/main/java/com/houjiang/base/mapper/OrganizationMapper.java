package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.base.dto.OrganizationDTO;
import com.houjiang.base.entity.OrganizationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName OrganizationMapper
 * @Description 组织信息Mapper
 * @Author Amos
 * @Date 2024-12-19
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrganizationMapper extends BaseMapper<OrganizationEntity> {

    /**
     * 分页查询组织信息
     *
     * @param page 分页对象
     * @param dto  查询条件
     * @return 分页结果
     */
    IPage<OrganizationEntity> selectOrganizationPage(Page<OrganizationEntity> page, @Param("dto") OrganizationDTO dto);

    /**
     * 查询组织树形结构
     *
     * @param dto 查询条件
     * @return 组织列表
     */
    List<OrganizationEntity> selectOrganizationTree(@Param("dto") OrganizationDTO dto);

    /**
     * 根据父组织ID查询子组织
     *
     * @param parentOrgId 父组织ID
     * @return 子组织列表
     */
    List<OrganizationEntity> selectChildrenByParentId(@Param("parentOrgId") String parentOrgId);

    /**
     * 检查组织编码是否存在
     *
     * @param orgCode 组织编码
     * @param orgId   组织ID（排除自己）
     * @return 数量
     */
    int checkOrgCodeExists(@Param("orgCode") String orgCode, @Param("orgId") String orgId);

    /**
     * 检查组织名称是否存在
     *
     * @param orgName 组织名称
     * @param orgId   组织ID（排除自己）
     * @return 数量
     */
    int checkOrgNameExists(@Param("orgName") String orgName, @Param("orgId") String orgId);
}
