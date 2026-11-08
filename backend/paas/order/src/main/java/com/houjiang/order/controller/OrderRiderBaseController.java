package com.houjiang.order.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.dto.OrderBaseRiderDTO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName OrderRiderBaseController
 * @Description 骑手订单信息
 * @Author Amos
 * @Date 2024/4/29 14:02
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("order/base/rider")
public class OrderRiderBaseController extends BaseController {

    /**
     * 分页查询订单管理列表-商家确认
     * @param param
     * @return
     */
    @PostMapping("/queryOrderMerchantConfirmationPage")
    public ResponseResult queryMerchantConfirmationPage(@RequestBody OrderBaseRiderDTO param) {
        return orderRiderBaseService.queryMerchantConfirmationPage(param);
    }

    /**
     * 分页查询订单管理列表-商家不确认
     * @param param
     * @return
     */
    @PostMapping("/queryOrderMerchantNotConfirmationPage")
    public ResponseResult queryMerchantNotConfirmationPage(@RequestBody OrderBaseRiderDTO param) {
        return orderRiderBaseService.queryMerchantNotConfirmationPage(param);
    }

    /**
     * 订单详情
     * @param orderId
     * @return
     */
    @GetMapping("/detail/{orderId}")
    public ResponseResult detail(@PathVariable("orderId") String orderId){
        return orderRiderBaseService.detail(orderId);
    }

    /**
     * 订单删除
     * @param orderId
     * @return
     */
    @PostMapping("/delete/{orderId}")
    public ResponseResult delete(@PathVariable("orderId") String orderId){
        return orderRiderBaseService.delete(orderId);
    }


    /**
     * 商家接单确认
     * @param orderId
     * @return
     */
    @PostMapping("/shopAffirm/{orderId}")
    public ResponseResult shopAffirm(@PathVariable("orderId") String orderId){
        return orderRiderBaseService.shopAffirm(orderId);
    }

    /**
     * 抢单
     * @param orderId
     * @param token
     * @return
     */
    @PostMapping("/orderGrabbing")
    public ResponseResult orderGrabbing(@RequestParam("orderId") String orderId,@RequestParam("token") String token){
        return orderRiderBaseService.orderGrabbing(orderId,token);
    }

    /**
     * 送达
     * @param orderId
     * @return
     */
    @PostMapping("/complete/{orderId}")
    public ResponseResult complete(@PathVariable("orderId") String orderId){
        return orderRiderBaseService.complete(orderId);
    }

    /**
     * 取货
     * @param orderId
     * @return
     */
    @PostMapping("/picking/{orderId}")
    public ResponseResult picking(@PathVariable("orderId") String orderId){
        return orderRiderBaseService.picking(orderId);
    }

    /**
     * 配送
     * @param orderId
     * @return
     */
    @PostMapping("/delivery/{orderId}")
    public ResponseResult delivery(@PathVariable("orderId") String orderId){
        return orderRiderBaseService.delivery(orderId);
    }

    /**
     * 供应详情
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/wait/supply/detail")
    public ResponseResult supplyDetail(@RequestBody Map<String,Object> param) throws Exception {
        return orderRiderBaseService.supplyDetail(param);
    }

}
