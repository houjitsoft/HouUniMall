package com.houjiang.merchant.service.mall;

import java.util.List;

/**
 * 岗位关联接口类
 */
public interface IMallShopAdminJobsService {

    /**
     * 关联岗位id
     *
     * @author Amos
     */
    List<Integer> getJobIdAttr(Integer adminId);

    /**
     * 关联岗位
     *
     * @author Amos
     */
    List<String> getJobNameAttr(Integer adminId);

    /**
     * 批量添加岗位
     * @param adminId
     * @param jobsIds
     */
    void batchInsert(Integer adminId, List<Integer> jobsIds, Integer shopId);

    /**
     * 删除关联岗位
     * @param adminId
     */
    void deleteByAdminId(Integer adminId);

    /**
     * 岗位是否被使用
     * @param jobId
     * @return
     * true 没被使用
     * false 已被使用
     */
    Boolean shopAdminUse(Integer jobId);
}
