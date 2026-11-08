package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayCashierOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 统一收银台订单 Mapper
 *
 * @author Amos
 */
@Mapper
public interface PayCashierOrderMapper extends BaseMapper<PayCashierOrderEntity> {

    /**
     * 根据收银台订单号查询
     * @param cashierNo 收银台订单号
     * @return 订单实体
     */
    PayCashierOrderEntity selectByCashierNo(@Param("cashierNo") String cashierNo);

    /**
     * 根据支付平台订单号查询
     * @param payOrderId 支付平台订单号
     * @return 订单实体
     */
    PayCashierOrderEntity selectByPayOrderId(@Param("payOrderId") String payOrderId);

    /**
     * @return 处理结果
     */
    PayCashierOrderEntity selectByRequestNo(@Param("requestNo") String requestNo);

    /**
     * @return 处理结果
     */
    int claimForPay(@Param("cashierNo") String cashierNo,
                    @Param("fromStatus") int fromStatus,
                    @Param("toStatus") int toStatus);

    /**
     * @return 处理结果
     */
    int markBalanceSuccess(@Param("cashierNo") String cashierNo,
                           @Param("fromStatus") int fromStatus,
                           @Param("toStatus") int toStatus,
                           @Param("payOrderId") String payOrderId,
                           @Param("successTime") Date successTime);

    /**
     * @return 处理结果
     */
    int markPaymentSuccess(@Param("cashierNo") String cashierNo,
                           @Param("fromStatus") int fromStatus,
                           @Param("toStatus") int toStatus,
                           @Param("payOrderId") String payOrderId,
                           @Param("successTime") Date successTime);
}
