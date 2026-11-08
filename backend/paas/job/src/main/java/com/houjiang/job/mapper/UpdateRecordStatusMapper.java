package com.houjiang.job.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * @author Amos
 * @date 2023/5/17 14:44
 */
@Mapper
public interface UpdateRecordStatusMapper {

    /**
     * 更新所有超时订单相关的记录
     * @param paymentNumber
     * @return
     */
    Integer updateReceiverdRecord(String paymentNumber);

    /**
     * 更新所有超时未确认收货订单相关Trade
     * @param orderId
     * @return
     */
    Integer updateReceiverdTrade(String orderId);

    /**
     * 根据paymentOrder更新所有超时订单记录
     * @param orderId
     * @return
     */
    Integer updateTimeoutRecord(String orderId);

    /**
     * 更新所有超时未支付订单Trade
     * @param orderId
     * @return
     */
    Integer updateTimeoutTrade(String orderId);
}
