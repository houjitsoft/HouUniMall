package com.houjiang.order.controller;

import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName OrderCancelReasonController
 * @Description 订单取消
 * @Author Amos
 * @Date 2023/6/26 13:50
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("order/cancelReason")
public class OrderCancelReasonController extends BaseController{

    /**
     * 订单取消原因
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = orderCancelReasonService.list(paraMap);
        return result;
    }

    /**
     * 删除退款/退货理由
     *
     * @param paraMap
     */
    @RequestMapping("/delete")
    public ResponseResult delete(@RequestBody Map<String, String> paraMap) {
        ResponseResult delete = orderCancelReasonService.delete(paraMap);
        return delete;
    }

    /**
     * 新增退款/退货理由
     *
     * @param paraMap
     * @return
     */
    @SensitiveCheck(field = {"cancelReason"})
    @RequestMapping("/platform/add")
    public ResponseResult addCancelreason(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = orderCancelReasonService.addCancelreason(paraMap);
        return result;
    }

    /**
     * 更新退款/退货理由
     *
     * @param paraMap
     * @return
     */
    @SensitiveCheck(field = {"cancelReason"})
    @RequestMapping("/platform/update")
    public ResponseResult updateCancelreason(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = orderCancelReasonService.updateCancelreason(paraMap);
        return result;
    }
}
