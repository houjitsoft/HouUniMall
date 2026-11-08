package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.dto.PaymentVocherDTO;
import com.houjiang.order.entity.OrderPaymentVocherEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface OrderPaymentVocherService extends IService<OrderPaymentVocherEntity> {

    /**
     * 添加支付凭证
     *
     * @param param
     * @return
     */
    ResponseResult insertPaymentVocher(PaymentVocherDTO param);

    /**
     * 根据orderId 获取已经上传的支付凭证信息
     *
     * @param paymentVocherDTO
     * @return
     */
    ResponseResult querkVocherImages(PaymentVocherDTO paymentVocherDTO);

    /**
     * 订单支付（BFF 层 payOrder 对应接口）
     * 业务职责：校验 orderId、订单状态与金额，返回供 payment 服务使用的支付参数（orderNo / payableAmount / merchantId / clientType 等）。
     * 实际的收款通道由 payment 微服务完成，此接口仅负责订单侧的验证与参数组装。
     *
     * @param param 支付入参，至少包含 orderId / token；可选 clientType / payCode
     * @return 支付参数结果，用于调用 payment 服务发起实际扣款
     */
    ResponseResult payOrder(Map<String, Object> param);
}
