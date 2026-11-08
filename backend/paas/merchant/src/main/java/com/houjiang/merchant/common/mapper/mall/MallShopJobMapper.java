package com.houjiang.merchant.common.mapper.mall;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.mall.MallShopJob;
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
public interface MallShopJobMapper extends IBaseMapper<MallShopJob> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT * FROM hou_merchant_mall_shop_job jobs INNER JOIN hou_merchant_mall_shop_admin_job laj ON laj.jobs_id = jobs.id WHERE laj.admin_id = #{adminId} AND jobs.delete_time = 0")
    List<Jobs> getByAdminId(@Param("adminId") Integer adminId);

}
