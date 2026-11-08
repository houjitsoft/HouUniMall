package com.houjiang.merchant.service.admin;

import com.houjiang.merchant.common.entity.admin.AdminRole;

import java.util.List;

/**
 * 角色关联接口类
 */
public interface IAdminRoleService {

    /**
     * 关联角色id
     *
     * @author Amos
     */
    List<Integer> getRoleIdAttr(Integer adminId);


    /**
     * TODO 补充方法说明
     * @param roleId TODO
     * @return TODO
     */
    List<AdminRole> getAdminIdByRoleId(Integer roleId);


    /**
     * 批量设置部门
     * @param adminId
     * @param deptIds
     */
    void batchInsert(Integer adminId, List<Integer> deptIds);

    /**
     * 删除关联角色
     * @param adminId
     */
    void deleteByAdminId(Integer adminId);
}
