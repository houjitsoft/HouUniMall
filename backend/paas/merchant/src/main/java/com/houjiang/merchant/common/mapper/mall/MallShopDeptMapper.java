package com.houjiang.merchant.common.mapper.mall;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.admin.Dept;
import com.houjiang.merchant.common.entity.mall.MallShopDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门表Mapper
 * @author Amos
 */
@Mapper
public interface MallShopDeptMapper extends IBaseMapper<MallShopDept> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT * FROM hou_merchant_mall_shop_dept dept INNER JOIN hou_merchant_mall_shop_admin_dept lad ON lad.dept_id = dept.id WHERE lad.admin_id = #{adminId} AND dept.delete_time = 0")
    List<Dept> getByAdminId(@Param("adminId") Integer adminId);

}
