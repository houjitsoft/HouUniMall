package com.houjiang.base.feign;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.order.OrderCommissionVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * @ClassName OrderClient
 * @Description
 * @Author Amos
 * @Date 2023/4/13 11:17
 * @Version 1.0
 * @author Amos
 */
@FeignClient("order")
public interface OrderClient {

    /**
     * TODO 补充方法说明
     * @param map TODO
     * @return TODO
     */
    @RequestMapping("order/base/queryIntegraOrdersCount")
    ResponseResult queryIntegraOrdersCount(@RequestBody Map<String, Object> map);

    /**
     * 新增发票
     *
     * @param param
     */
    @PostMapping("/order/invoiceTemplateInfo/feign/insertInvoiceTmpFeign")
    void insertInvoiceTmpFeign(@RequestBody Map<String, Object> param);


    /**
     * 修改发票
     *
     * @param param
     */
    @PostMapping("/order/invoiceTemplateInfo/feign/updateInvoiceTmpFeign")
    void updateInvoiceTmpFeign(@RequestBody Map<String, Object> param);


    /**
     * 修改订单分佣状态
     * @param orderId
     */
    @GetMapping("/order/commission/feign/updateOrderCommissionState")
    void updateOrderCommissionState(@RequestParam("orderId") String orderId);

    /**
     * 查询佣金订单
     * @param orderId
     * @return
     */
    @GetMapping("/order/commission/feign/selectOrderCommission")
    OrderCommissionVO selectOrderCommission(@RequestParam("orderId") String orderId);
}
