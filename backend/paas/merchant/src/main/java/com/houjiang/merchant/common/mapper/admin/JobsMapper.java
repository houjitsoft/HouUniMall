package com.houjiang.merchant.common.mapper.admin;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.admin.AdminJobs;
import com.houjiang.merchant.common.entity.admin.Dept;
import com.houjiang.merchant.common.entity.system.Jobs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 岗位表Mapper
 * @author Amos
 */
@Mapper
public interface JobsMapper extends IBaseMapper<Jobs> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT * FROM hou_merchant_jobs jobs INNER JOIN hou_merchant_admin_jobs laj ON laj.jobs_id = jobs.id WHERE laj.admin_id = #{adminId} AND jobs.delete_time IS NULL")
    List<Jobs> getByAdminId(@Param("adminId") Integer adminId);

}
