package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.order.OrderBalanceRechargeVO;
import com.houjiang.order.dto.OrderBalanceRechargeDTO;
import com.houjiang.order.entity.OrderBalanceRechargeEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @ClassName OrderBalanceRechargeService
 * @Description  余额充值订单表
 * @Author Amos
 * @Date 2024-09-19 09:35:39
 * @Version 1.0
 * @author Amos
 */
public interface OrderBalanceRechargeService extends IService<OrderBalanceRechargeEntity> {

    /**
     * 创建余额充值订单
     * @param orderBalanceRechargeDTO
     * @return
     */
    ResponseResult createBalanceRechargeOrder(OrderBalanceRechargeDTO orderBalanceRechargeDTO);

    /**
     * 修改余额充值订单
     * @param map
     * @return
     */
    ResponseResult updateBalanceRechargeOrder(Map<Object, Object> map);

    /**
     * 平台 查询线下支付的余额充值单
     * @param orderBalanceRechargeDTO
     * @return
     */
    ResponseResult queryOfflineRechargeOrder(OrderBalanceRechargeDTO orderBalanceRechargeDTO);

    /**
     * 平台 审核线下支付的余额充值单
     * @param orderBalanceRechargeDTO
     * @return
     */
    ResponseResult auditOfflineRechargeOrder(OrderBalanceRechargeDTO orderBalanceRechargeDTO);

    /**
     * 根据充值单号查询充值单详情
     * @param rechargeOrderId
     * @return
     */
    OrderBalanceRechargeVO queryRechargeOrderById(String rechargeOrderId);
}
