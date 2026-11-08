package com.houjiang.payment.mapper;


import com.houjiang.payment.dto.GoodsDTO;
import com.houjiang.payment.dto.RecordDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @Entity com.houjiang.payment.entity.PayConsumeTradeBack
 * @author Amos
 */
@Mapper
public interface PayConsumeTradeBackMapper {


    /**
     * 查询订单描述
     *
     * @param orderId
     * @param userId
     * @return
     */
    GoodsDTO queryGoodsPriceAndName(@Param("orderId") String orderId, @Param("userId") String userId);

    /**
     * 查询金额和名称
     *
     * @param outTradeNo
     * @return
     */
    RecordDTO queryRecordPriceAndName(@Param("outTradeNo") String outTradeNo);

    /**
     * 更新消费信息
     *
     * @param mapDigital
     * @return
     */
    boolean updateConsumeTradeByPureDigital(Map<String, Object> mapDigital);

    /**
     * 更新消费
     *
     * @param mapDigital
     * @return
     */
    boolean updateConsumeTrade(Map<String, Object> mapDigital);

    /**
     * 查询金额
     *
     * @param orderId
     * @return
     */
    RecordDTO queryRecordPriceAndNameByTradeId(@Param("orderId") String orderId);

}




