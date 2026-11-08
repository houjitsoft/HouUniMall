package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.entity.UserOrganizationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName UserOrganizationMapper
 * @Description 用户组织关联Mapper
 * @Author Amos
 * @Date 2024-12-19
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface UserOrganizationMapper extends BaseMapper<UserOrganizationEntity> {
}
