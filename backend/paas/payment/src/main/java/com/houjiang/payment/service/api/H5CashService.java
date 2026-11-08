package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.h5cash.H5CashOrderRequest;
import com.houjiang.payment.dto.h5cash.H5CashOrderResponse;
import com.houjiang.payment.entity.H5CashOrderEntity;

import java.util.Map;

/**
 * 通企付 H5 收银台服务接口
 *
 * @author Amos
 */
public interface H5CashService {

    /**
     * 27. H5 收银台下单
     * 调用通企付 /h5Cash/getH5Url，落库 hou_pay_h5cash_order，返回 H5Url 用于前端跳转
     *
     * @param request H5 收银台下单请求
     * @return H5 收银台下单响应（含 H5Url）
     */
    H5CashOrderResponse getH5Url(H5CashOrderRequest request);

    /**
     * 解析 jumpUrl 回跳参数
     * 1) 用通企付密钥验签
     * 2) 根据 state 更新本地订单（2-成功 / 4-关闭 / 5-异常）
     * 3) 返回最新订单实体给前端展示
     *
     * @param params jumpUrl 携带的 query 参数（含 mchNo/mchOrderNo/payOrderId/amount/state/sign/signType/reqTime）
     * @return 最新本地订单实体（验签失败抛 PaymentException）
     */
    H5CashOrderEntity parseJumpResult(Map<String, String> params);

    /**
     * 商户订单号查询订单
     *
     * @param mchNo      商户号
     * @param mchOrderNo 商户订单号
     * @return 订单实体（不存在返回 null）
     */
    H5CashOrderEntity queryByMchOrderNo(String mchNo, String mchOrderNo);
}
