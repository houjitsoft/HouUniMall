package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.JobOrderNotCompletedEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.job.entity.JobOrderNotCompletedEntity
 * @author Amos
 */
@Mapper
public interface JobOrderNotCompletedMapper extends BaseMapper<JobOrderNotCompletedEntity> {

    /**
     * 未完成订单
     * @param finishTime
     * @return
     */
    List<Map<String,Object>> queryNotCompletedparam(String finishTime);
}




