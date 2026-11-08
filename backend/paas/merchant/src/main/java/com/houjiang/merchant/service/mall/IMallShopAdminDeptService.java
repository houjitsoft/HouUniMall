package com.houjiang.merchant.service.mall;

import java.util.List;

/**
 * 部门关联接口类
 */
public interface IMallShopAdminDeptService {

    /**
     * 关联部门id
     *
     * @author Amos
     */
    List<Integer> getDeptIdAttr(Integer adminId);

    /**
     * 关联部门id
     *
     * @author Amos
     */
    List<String> getDeptNameAttr(Integer adminId);

    /**
     * 批量添加部门
     * @param adminId
     * @param deptIds
     */
    void batchInsert(Integer adminId, List<Integer> deptIds, Integer shopId);

    /**
     * 删除关联部门
     * @param adminId
     */
    void deleteByAdminId(Integer adminId);

    /**
     * 部门是否被使用
     * @param jobId
     * @return
     * true 没被使用
     * false 已被使用
     */
    Boolean deptAdminUse(Integer deptId);
}
