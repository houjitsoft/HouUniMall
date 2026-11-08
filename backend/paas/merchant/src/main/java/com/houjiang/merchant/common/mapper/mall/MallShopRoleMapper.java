package com.houjiang.merchant.common.mapper.mall;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.mall.MallShopRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统角色Mapper
 * @author Amos
 */
@Mapper
public interface MallShopRoleMapper extends IBaseMapper<MallShopRole> {


    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT * FROM hou_merchant_mall_shop_role role INNER JOIN hou_merchant_mall_shop_admin_role lar ON lar.role_id = role.id WHERE lar.admin_id = #{adminId} AND role.delete_time = 0")
    List<MallShopRole> getByAdminId(@Param("adminId") Integer adminId);

}
