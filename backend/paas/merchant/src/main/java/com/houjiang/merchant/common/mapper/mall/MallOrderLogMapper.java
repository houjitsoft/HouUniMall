package com.houjiang.merchant.common.mapper.mall;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.mall.MallOrderLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单日志Mapper
 * @author Amos
 */
@Mapper
public interface MallOrderLogMapper extends IBaseMapper<MallOrderLog> {

    /**
     * 写入订单日志
     *
     * @author Amos
     * @param orderId Integer 订单id
     * @param type Integer 类型 1系统 2商家 3买家
     * @param channel Integer 动作
     * @param operatorId Integer 操作人id
     * @param content String 内容
     */
    default void add(Integer orderId, Integer type, Integer channel, Integer operatorId, String content) {
        /**
         * TODO 补充方法说明
         */
        MallOrderLog orderLog = new MallOrderLog();
        /**
         * TODO 补充方法说明
         */
        orderLog.setOrderId(orderId);
        /**
         * TODO 补充方法说明
         */
        orderLog.setOperatorType(type);
        /**
         * TODO 补充方法说明
         */
        orderLog.setOperatorId(operatorId);
        /**
         * TODO 补充方法说明
         */
        orderLog.setChannel(channel);
        /**
         * TODO 补充方法说明
         */
        orderLog.setContent(content);
        /**
         * TODO 补充方法说明
         */
        orderLog.setCreateTime(System.currentTimeMillis() / 1000);
        /**
         * TODO 补充方法说明
         */
        orderLog.setUpdateTime(System.currentTimeMillis() / 1000);
        /**
         * TODO 补充方法说明
         */
        this.insert(orderLog);
    }
}
