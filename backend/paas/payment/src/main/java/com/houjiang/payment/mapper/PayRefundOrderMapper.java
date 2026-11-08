package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayRefundOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Amos
 */
@Mapper
public interface PayRefundOrderMapper extends BaseMapper<PayRefundOrderEntity> {

    /**
     * @return 处理结果
     */
    PayRefundOrderEntity selectByRefundNo(@Param("refundNo") String refundNo);

    /**
     * @param refundOrder 退款订单
     * @return 处理结果
     */
    int insertIfAbsent(PayRefundOrderEntity refundOrder);
}
