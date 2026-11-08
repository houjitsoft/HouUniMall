package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.order.ConsumeTradeParaDTO;
import com.houjiang.payment.entity.PayConsumeTradeEntity;
import com.houjiang.payment.vo.CallBackVO;

import java.util.List;

/**
 * @ClassName PayConsumeTradeService
 * @Description  交易订单表-强调唯一订单-充值则先创建充值订单
 * @Author Amos
 * @Date 2023-05-31 16:13:57
 * @Version 1.0
 * @author Amos
 */
public interface PayConsumeTradeService extends IService<PayConsumeTradeEntity> {
     /**
      * 插入交易记录
      * @param ctp
      * @return
      */
     boolean insert(ConsumeTradeParaDTO ctp);
     /**
      * 促销订单
      *
      * @param token
      * @param orderId
      * @return
      */
     List<CallBackVO> treasureOrder(String token, String orderId);
    /**
     * 根据订单号查询订单信息。
     *
     * 本函数通过调用数据库或API等途径，使用给定的订单号查询相应的订单信息。
     * 具体实现可能涉及数据库查询语句的编写或API调用的逻辑。
     *
     * @param orderId 待查询的订单号。这是一个唯一标识订单的字符串。
     * @return 返回查询到的订单信息。如果未查询到相关信息，可能返回空字符串或特定的错误信息。
     */
    String queryOrderId(String orderId);
}
