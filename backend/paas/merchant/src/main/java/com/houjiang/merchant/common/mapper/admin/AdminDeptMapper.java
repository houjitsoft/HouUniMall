package com.houjiang.merchant.common.mapper.admin;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.admin.AdminDept;
import com.houjiang.merchant.common.entity.admin.AdminRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色关联表Mapper
 * @author Amos
 */
@Mapper
public interface AdminDeptMapper extends IBaseMapper<AdminDept> {

}
