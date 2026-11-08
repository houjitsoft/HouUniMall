package com.houjiang.merchant.common.mapper.system;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.system.SystemMenu;
import com.houjiang.merchant.common.entity.system.SystemRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统菜单Mapper
 * @author Amos
 */
@Mapper
public interface SystemRoleMenuMapper extends IBaseMapper<SystemRoleMenu> {


    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT DISTINCT menuR.menu_id FROM hou_merchant_system_role_menu menuR INNER JOIN hou_merchant_system_role role ON menuR.role_id = role.id INNER JOIN hou_merchant_admin_role lar ON lar.role_id = role.id WHERE lar.admin_id = #{adminId} AND role.delete_time IS NULL")
    List<Integer> getMenuIds(@Param("adminId") Integer adminId);



}
