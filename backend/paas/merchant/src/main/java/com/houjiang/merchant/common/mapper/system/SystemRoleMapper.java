package com.houjiang.merchant.common.mapper.system;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.system.SystemRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统角色Mapper
 * @author Amos
 */
@Mapper
public interface SystemRoleMapper extends IBaseMapper<SystemRole> {


    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT * FROM hou_merchant_system_role role INNER JOIN hou_merchant_admin_role lar ON lar.role_id = role.id WHERE lar.admin_id = #{adminId} AND role.delete_time IS NULL")
    List<SystemRole> getByAdminId(@Param("adminId") Integer adminId);

}
