package com.houjiang.merchant.common.mapper.admin;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.admin.AdminJobs;
import com.houjiang.merchant.common.entity.admin.AdminRole;
import com.houjiang.merchant.common.entity.system.SystemRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色关联表Mapper
 * @author Amos
 */
@Mapper
public interface AdminJobsMapper extends IBaseMapper<AdminJobs> {



}
