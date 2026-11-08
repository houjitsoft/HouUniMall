package com.houjiang.order.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName OrderReturnReasonController
 * @Description 退货原因
 * @Author Amos
 * @Date 2023/6/26 13:45
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("order/returnReason")
public class OrderReturnReasonYiyatongController extends BaseController{

    /**
     * 获取怡亚通退款原因
     *
     * @return
     */
    @GetMapping("/yiyatong/listreturnreason")
    public ResponseResult listCancelReasonByYiyatong(@RequestParam("orderReturnReasonType") Integer orderReturnReasonType) {
        ResponseResult result = orderReturnReasonService.listCancelReasonByYiyatong(orderReturnReasonType);
        return result;
    }

}
