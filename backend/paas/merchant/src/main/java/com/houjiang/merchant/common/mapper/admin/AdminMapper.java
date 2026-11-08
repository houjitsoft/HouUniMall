package com.houjiang.merchant.common.mapper.admin;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.admin.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统管理员Mapper
 * @author Amos
 */
@Mapper
public interface AdminMapper extends IBaseMapper<Admin> {


    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT * FROM hou_merchant_admin admin INNER JOIN hou_merchant_admin_dept lad ON admin.id = lad.admin_id WHERE lad.dept_id = #{deptId} AND admin.delete_time IS NULL")
    List<Admin> getByDept(@Param("deptId") Integer deptId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT * FROM hou_merchant_admin admin INNER JOIN hou_merchant_admin_jobs lad ON admin.id = lad.admin_id WHERE lad.jobs_id = #{jobId} AND admin.delete_time IS NULL")
    List<Admin> getByJobs(@Param("jobId") Integer jobId);


    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT count(*) FROM hou_merchant_admin admin INNER JOIN hou_merchant_admin_role lad ON admin.id = lad.admin_id WHERE lad.role_id = #{roleId} AND admin.delete_time IS NULL")
    Integer getCountByRoleId(@Param("roleId") Integer roleId);


}
