package com.houjiang.merchant.service.admin;

import java.util.List;

/**
 * 部门关联接口类
 */
public interface IAdminDeptService {

    /**
     * 关联部门id
     *
     * @author Amos
     */
    List<Integer> getDeptIdAttr(Integer adminId);

    /**
     * 批量添加部门
     * @param adminId
     * @param deptIds
     */
    void batchInsert(Integer adminId, List<Integer> deptIds);

    /**
     * 删除关联部门
     * @param adminId
     */
    void deleteByAdminId(Integer adminId);
}
