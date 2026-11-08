package com.houjiang.merchant.common.mapper.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.mall.MallOrderLog;
import com.houjiang.merchant.common.entity.mall.MallRefund;
import com.houjiang.merchant.common.entity.mall.MallRefundLog;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.ToolUtils;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

/**
 * 退款订单日志Mapper
 * @author Amos
 */
@Mapper
public interface MallRefundLogMapper extends IBaseMapper<MallRefundLog> {

    /**
     * 生成唯一流水号
     *
     * @author Amos
     * @param field String
     * @return String
     */
    default String randMakeOrderSn(String field) {
        /**
         * TODO 补充方法说明
         */
        String date = TimeUtils.timestampToDate(System.currentTimeMillis()/1000, "yyyyMMddHHmmss");
        String sn;
        /**
         * TODO 补充方法说明
         */
        while (true) {
            /**
             * TODO 补充方法说明
             */
            sn = date + ToolUtils.randomInt(4);
            /**
             * TODO 补充方法说明
             * @param QueryWrapper<MallRefundLog>( TODO
             */
            MallRefundLog snModel = this.selectOne(
                    /**
                     * TODO 补充方法说明
                     * @return TODO
                     */
                    new QueryWrapper<MallRefundLog>()
                            .select("id")
                            .eq(field, sn)
                            .last("limit 1"));
            /**
             * TODO 补充方法说明
             * @param null TODO
             */
            if (snModel == null) {
                break;
            }
        }
        return sn;
    }

    /**
     * 写入订单日志
     *
     * @author Amos
     * @param orderId Integer 订单id
     * @param type Integer 类型 1系统 2商家 3买家
     * @param sn Integer sn
     * @param operatorId Integer 操作人id
     * @param content String 内容
     */
    default void add(Integer orderId, Integer type, String sn, Integer operatorId, String content, Integer refundStatus, BigDecimal money) {
        /**
         * TODO 补充方法说明
         */
        MallRefundLog orderLog = new MallRefundLog();
        /**
         * TODO 补充方法说明
         */
        orderLog.setRefundId(orderId);
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
        orderLog.setSn(sn);
        /**
         * TODO 补充方法说明
         */
        orderLog.setContent(content);
        /**
         * TODO 补充方法说明
         */
        orderLog.setRefundStatus(refundStatus);
        /**
         * TODO 补充方法说明
         */
        orderLog.setMoney(money);
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
