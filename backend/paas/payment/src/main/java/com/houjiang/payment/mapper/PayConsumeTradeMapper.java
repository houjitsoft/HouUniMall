package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.dto.GoodsDTO;
import com.houjiang.payment.dto.QueryGoodsParaDTO;
import com.houjiang.payment.dto.QueryRecordParaDTO;
import com.houjiang.payment.entity.PayConsumeTradeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PayConsumeTradeMapper
 * @Description  交易订单表-强调唯一订单-充值则先创建充值订单
 * @Author Amos
 * @Date 2023-05-31 16:13:57
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface PayConsumeTradeMapper extends BaseMapper<PayConsumeTradeEntity> {

    /**
     * 插入交易记录
     * @param
     * @return
     */
    boolean insertConsumeTrade(@Param("consumeTradeId") String consumeTradeId, @Param("orderId") String orderId, @Param("userId") String userId, @Param("sellerId") String sellerId, @Param("payUserId") String payUserId, @Param("orderStateId") Integer orderStateId, @Param("tradeTypeId") Integer tradeTypeId, @Param("paymentChannelId") Integer paymentChannelId, @Param("orderPaymentAmount") BigDecimal orderPaymentAmount, @Param("tradePaymentAmount") BigDecimal tradePaymentAmount, @Param("customerType") Integer customerType, @Param("tradeDiscount") BigDecimal tradeDiscount, @Param("tradeDate") String tradeDate, @Param("tradeYear") String tradeYear, @Param("tradeMonth") String tradeMonth, @Param("tradeDay") String tradeDay, @Param("tradeTitle") String tradeTitle, @Param("tradeDesc") String tradeDesc, @Param("tradeRemark") String tradeRemark, @Param("tradeCreateTime") String tradeCreateTime, @Param("totalIntegral") Integer totalIntegral, @Param("integralGoods") Integer integralGoods);


    /**
     * 查询商品金额和名称
     *
     * @param outTradeNo
     * @return
     */
    GoodsDTO queryGoodsPriceAndName(@Param("outTradeNo") String outTradeNo);

    /**
     * 获取订单信息
     *
     * @param outTradeNo
     * @return
     */
    QueryRecordParaDTO queryRecordPriceAndName(String outTradeNo);

    /**
     * 更新状态trade
     *
     * @param orderStateId
     * @param paymentTime
     * @param closeTime
     * @param outTradeNo
     * @param paymentChannelId
     * @return
     */
    boolean updateConsumeTradeByPureDigital(@Param("orderStateId") Integer orderStateId,
                                            @Param("paymentTime") String paymentTime, @Param("closeTime") String closeTime,
                                            @Param("outTradeNo") String outTradeNo, @Param("paymentChannelId") Integer paymentChannelId);



    /**
     * 更新ConsumeTrade
     *
     * @param orderStateId
     * @param paymentTime
     * @param closeTime
     * @param outTradeNo
     * @param paymentChannelId
     * @return
     */
    boolean updateConsumeTrade(@Param("orderStateId") Integer orderStateId, @Param("paymentTime") String paymentTime,
                               @Param("closeTime") String closeTime, @Param("outTradeNo") String outTradeNo,
                               @Param("paymentChannelId") Integer paymentChannelId);

    /**
     * 查询商品金额和名称
     *
     * @param orderId
     * @param userId
     * @return
     */
    QueryGoodsParaDTO queryGoodsOrderAndUser(@Param("orderId") String orderId, @Param("userId") String userId);

    /**
     * 查询订单状态
     *
     * @param orderId
     * @param userId
     * @return
     */
    Integer queryStatusByInteger(@Param("orderId") String orderId, @Param("userId") String userId);


    /**
     * 更新支付金额
     *
     * @param orderId
     * @param type
     */
    void updateConsumeTradePaymentMoney(@Param("orderId") String orderId, @Param("type") Integer type);

}
