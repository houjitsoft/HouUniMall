package com.houjiang.merchant.service.admin;

import java.util.List;

/**
 * 岗位关联接口类
 */
public interface IAdminJobsService {

    /**
     * 关联岗位id
     *
     * @author Amos
     */
    List<Integer> getJobIdAttr(Integer adminId);

    /**
     * 批量添加岗位
     * @param adminId
     * @param jobsIds
     */
    void batchInsert(Integer adminId, List<Integer> jobsIds);

    /**
     * 删除关联岗位
     * @param adminId
     */
    void deleteByAdminId(Integer adminId);
}
