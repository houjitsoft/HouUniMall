package com.houjiang.order.service;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.dto.OrderBaseRiderDTO;

import java.io.IOException;
import java.util.Map;

/**
 * @ClassName OrderRiderBaseService
 * @Description 骑手订单接口
 * @Author Amos
 * @Date 2024/4/29 14:04
 * @Version 1.0
 * @author Amos
 */
public interface OrderRiderBaseService {

    /**
     * 分页查询订单管理列表-商家确认
     * @param param
     * @return
     */
    ResponseResult queryMerchantConfirmationPage(OrderBaseRiderDTO param);

    /**
     * 分页查询订单管理列表-商家不确认
     * @param param
     * @return
     */
    ResponseResult queryMerchantNotConfirmationPage(OrderBaseRiderDTO param);

    /**
     * 订单详情
     * @param orderId
     * @return
     */
    ResponseResult detail(String orderId);

    /**
     * 订单删除
     * @param orderId
     * @return
     */
    ResponseResult delete(String orderId);

    /**
     * 商家接单确认
     * @param orderId
     * @return
     */
    ResponseResult shopAffirm(String orderId);

    /**
     * 抢单
     * @param orderId
     * @param token
     * @return
     */
    ResponseResult orderGrabbing(String orderId,String token);

    /**
     * 送达
     * @param orderId
     * @return
     */
    ResponseResult complete(String orderId);

    /**
     * 取货
     * @param orderId
     * @return
     */
    ResponseResult picking(String orderId);

    /**
     * 配送
     * @param orderId
     * @return
     */
    ResponseResult delivery(String orderId);

    /**
     * 供应详情
     * @param param
     * @return
     * @throws IOException
     */
    ResponseResult supplyDetail(Map<String,Object> param) throws IOException;

}
