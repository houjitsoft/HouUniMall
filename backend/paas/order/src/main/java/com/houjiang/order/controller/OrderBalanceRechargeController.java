package com.houjiang.order.controller;


import com.houjiang.commons.util.PayToolUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.order.OrderBalanceRechargeVO;
import com.houjiang.order.dto.OrderBalanceRechargeDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName OrderBalanceRechargeController
 * @Description  余额充值订单表
 * @Author Amos
 * @Date 2024-09-19 09:35:39
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("orderBalanceRecharge")
public class OrderBalanceRechargeController extends BaseController {

    /**
     * 创建余额充值订单
     * @param orderBalanceRechargeDTO
     * @return
     */
    @PostMapping("createBalanceRechargeOrder")
    public ResponseResult createBalanceRechargeOrder(@RequestBody OrderBalanceRechargeDTO orderBalanceRechargeDTO){

        return orderBalanceRechargeService.createBalanceRechargeOrder(orderBalanceRechargeDTO);
    }
    /**
     * 修改余额充值订单
     * @param map
     * @return
     */
    @PostMapping("updateBalanceRechargeOrder")
    public ResponseResult updateBalanceRechargeOrder(@RequestBody Map<Object, Object> map){

        return orderBalanceRechargeService.updateBalanceRechargeOrder(map);
    }

    /**
     * 平台 查询线下支付的余额充值单
     * @param orderBalanceRechargeDTO
     * @return
     */
    @PostMapping("queryOfflineRechargeOrder")
    public ResponseResult queryOfflineRechargeOrder(@RequestBody OrderBalanceRechargeDTO orderBalanceRechargeDTO){
        return orderBalanceRechargeService.queryOfflineRechargeOrder(orderBalanceRechargeDTO);
    }
    /**
     * 平台 审核线下支付的余额充值单
     * @param orderBalanceRechargeDTO
     * @return
     */
    @PostMapping("auditOfflineRechargeOrder")
    public ResponseResult auditOfflineRechargeOrder(@RequestBody OrderBalanceRechargeDTO orderBalanceRechargeDTO){
        return orderBalanceRechargeService.auditOfflineRechargeOrder(orderBalanceRechargeDTO);
    }

    /**
     * 根据充值单号查询充值单详情
     * @param rechargeOrderId
     * @return
     */
    @PostMapping("queryRechargeOrderById")
    public OrderBalanceRechargeVO queryRechargeOrderById(@RequestParam("rechargeOrderId") String rechargeOrderId){
        return orderBalanceRechargeService.queryRechargeOrderById(rechargeOrderId);
    }
}
