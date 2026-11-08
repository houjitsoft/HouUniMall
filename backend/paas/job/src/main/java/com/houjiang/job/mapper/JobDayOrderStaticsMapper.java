package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.JobDayOrderStaticsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @Entity com.houjiang.job.entity.JobDayOrderStaticsEntity
 * @author Amos
 */
@Mapper
public interface JobDayOrderStaticsMapper extends BaseMapper<JobDayOrderStaticsEntity> {

    /**
     * 已完成订单
     * @param countMap
     * @return
     */
    Map<String, String> countOrderBase(@Param("countMap") Map<String, String> countMap);

    /**
     * 完成的退款
     * @param countMap
     * @return
     */
    Map<String, String> countAfterSaleOrder(@Param("countMap")Map<String, String> countMap);

}




