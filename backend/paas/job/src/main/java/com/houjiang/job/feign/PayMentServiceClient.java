package com.houjiang.job.feign;


import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author Amos
 * @date 2023/3/27 10:06
 */
@FeignClient("payment")
public interface PayMentServiceClient {

    /**
     * 退款
     *
     * @param refundPara
     * @return
     */
    @PostMapping("/pay/consumeRecord/waitShipmentsRefund")
    boolean waitShipmentsRefund(@RequestBody RefundParamDTO refundPara);

    /**
     * 获取支付配置
     *
     * @param param
     * @return
     */
    @PostMapping("/pay/payConfig/queryFeign")
    ResponseResult selectPayConfig(@RequestBody Map<String, String> param);


    /**
     * 取消付款状态
     *
     * @param orderId
     * @return
     */
    @PostMapping("/pay/consumeRecord/updateCancelStatus")
    ResponseResult updateCancelStatus(@RequestBody String orderId);

    /**
     * 判断支付宝是否支付成功  feign
     *
     * @param param
     * @return
     */
    @RequestMapping("/pay/aLiPay/alipayQuery")
    ResponseResult alipayQuery(@RequestBody Map<String, Object> param);

    /**
     * 对订单支付宝 已支付成功 回调失败的异常单进行处理 feign
     *
     * @param param
     * @return
     */
    @RequestMapping("/pay/aLiPay/alipayCompensate")
    ResponseResult alipayCompensate(@RequestBody Map<String, Object> param);

    /**
     * 根据订单号查询微信支付状态 feign
     */
    @PostMapping("/pay/WXPay/queryWxPay")
    ResponseResult queryWxPay(@RequestBody Map<String, Object> map);

    /**
     * 对订单微信 已支付成功 回调失败的异常单进行处理 feign
     *
     * @param param
     * @return
     */
    @RequestMapping("/pay/WXPay/wxPayCompensate")
    ResponseResult wxPayCompensate(@RequestBody Map<String, Object> param);
}
