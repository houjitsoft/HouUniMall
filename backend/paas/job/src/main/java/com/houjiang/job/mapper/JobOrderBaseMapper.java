package com.houjiang.job.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * @author Amos
 * @date 2023/5/16 10:59
 */
@Mapper
public interface JobOrderBaseMapper {

    /**
     * 收货时间
     * @param orderId
     * @return
     */
    String  queryorderFinishedTime(String orderId);

    /**
     * 订单完成时间
     * @param orderId
     * @return
     */
    Integer updateOrderEvaluated(String orderId);
}
