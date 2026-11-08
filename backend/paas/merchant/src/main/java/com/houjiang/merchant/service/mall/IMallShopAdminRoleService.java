package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.entity.mall.MallShopAdminRole;

import java.util.List;

/**
 * 角色关联接口类
 */
public interface IMallShopAdminRoleService {

    /**
     * 关联角色id
     *
     * @author Amos
     */
    List<Integer> getRoleIdAttr(Integer adminId);
    /**
     * 关联岗位
     *
     * @author Amos
     */
    List<String> getRoleNameAttr(Integer adminId);


    /**
     * TODO 补充方法说明
     * @param roleId TODO
     * @return TODO
     */
    List<MallShopAdminRole> getAdminIdByRoleId(Integer roleId);

    /**
     * TODO 补充方法说明
     * @param roleId TODO
     * @return TODO
     */
    List<Integer> getAdminIdsByRoleId(Integer roleId);


    /**
     * 批量设置部门
     * @param adminId
     * @param deptIds
     */
    void batchInsert(Integer adminId, List<Integer> deptIds, Integer shopId);

    /**
     * 删除关联角色
     * @param adminId
     */
    void deleteByAdminId(Integer adminId);

    /**
     * 返回数量
     * @param roleId
     * @return
     */
    Long getCountByRoleId(Integer roleId);

    /**
     * 岗位是否被使用
     * @param roleId
     * @return
     * true 没被使用
     * false 已被使用
     */
    Boolean shopAdminUse(Integer roleId);
}
