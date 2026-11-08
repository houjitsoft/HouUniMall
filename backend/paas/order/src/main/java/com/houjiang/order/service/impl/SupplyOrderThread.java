package com.houjiang.order.service.impl;


import com.alibaba.fastjson.JSON;
import com.houjiang.commons.vo.order.DealerOrderGoodsVO;
import com.houjiang.order.feign.GoodServiceClient;
import com.houjiang.order.mapper.OrderBaseMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * @date 2021-05-3113:52
 * @Description: 供应商订单支付回调异步接口
 **/
public class SupplyOrderThread implements Runnable{
    private GoodServiceClient goodServiceClient;;
    private OrderBaseMapper orderBaseMapper;
    private String paymentNum;
    private String token;

    public SupplyOrderThread(GoodServiceClient goodServiceClient, OrderBaseMapper orderBaseMapper, String paymentNum, String token) {
        this.goodServiceClient = goodServiceClient;
        this.orderBaseMapper = orderBaseMapper;
        this.paymentNum = paymentNum;
        this.token = token;
    }

    /**
     * 执行订单供应商品信息的处理任务。
     * 该方法通过调用查询接口获取特定支付编号的订单商品信息，然后将这些信息封装并发送给商品服务客户端。
     * 主要用于处理供应链订单的后台任务，不直接与用户界面交互。
     */
    @Override
    public void run() {
        // 根据支付编号查询订单商品信息
        List<DealerOrderGoodsVO> dealerOrderGoodsList = orderBaseMapper.querySupplyOrder(paymentNum);

        // 初始化并配置订单商品信息的传输映射
        Map<String,String> map = new HashMap<>(8);
        // 设置令牌信息，用于身份验证和权限校验
        map.put("token",token);
        // 将订单商品信息转换为JSON字符串并添加到映射中，以便于跨服务传输
        map.put("dealerOrderGoodsList",JSON.toJSONString(dealerOrderGoodsList));

        // 调用商品服务客户端，发送订单商品信息
        goodServiceClient.dealerOrderGoods(map);
    }

}
