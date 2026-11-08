package com.houjiang.job.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * @author Amos
 * @date 2023/5/17 11:36
 */
@Mapper
public interface UpdateSalesPromStateMapper {


    /**
     * 更新代金券模板
     * @return
     */
    Integer updateRedpkTempState();


    /**
     * 用户已领的代金券
     * @return
     */
    Integer updateRedpkBaseState();


    /**
     * 更新红包模板
     * @return
     */
    Integer updateVouchrTempState();


    /**
     * 用户已领的红包
     * @return
     */
    Integer updateVouchrBaseState();
}
