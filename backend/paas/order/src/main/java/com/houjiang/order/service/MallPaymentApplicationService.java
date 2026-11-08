package com.houjiang.order.service;

import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.payment.MallPaymentNotifyRequest;
import com.houjiang.commons.dto.payment.MallCashierCreateRequest;
import com.houjiang.commons.dto.payment.MallCashierCreateResponse;
import com.houjiang.commons.dto.payment.MallCashierStatusResponse;
import com.houjiang.commons.dto.payment.MallRefundRequest;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.feign.PayMentServiceClient;
import com.houjiang.order.feign.ActivityServiceClient;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.order.entity.OrderBaseEntity;
import com.houjiang.order.mapper.OrderBaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Amos
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MallPaymentApplicationService {

    private static final Set<String> SUCCESS_STATUSES = Set.of("SUCCESS", "PAID", "2");

    private final OrderBaseMapper orderMapper;
    private final OrderInventoryLifecycleService inventoryLifecycleService;
    private final PayMentServiceClient paymentClient;
    private final ActivityServiceClient activityServiceClient;

    @Value("${mall.payment.notify-url:http://order/order/mall/payment/notify}")
    private String paymentNotifyUrl;

    public MallCashierCreateResponse createCashier(MallCashierCreateRequest request) {
        if (request == null || !StringUtils.hasText(request.getMallOrderNo())
                || !StringUtils.hasText(request.getUserId())) {
            throw new DefinedException("商城支付参数不完整");
        }
        OrderBaseEntity order = requireOwnedOrder(request.getMallOrderNo(), request.getUserId());
        if (!CommonType.orderStatus.TO_BE_PAID.getCode().equals(order.getOrderStatus())) {
            throw new DefinedException("订单状态不允许发起支付");
        }
        request.setAmount(order.getOrderPaymentAmount());
        request.setNotifyUrl(paymentNotifyUrl);
        request.setSubject("商城订单 " + order.getOrderId());
        ResponseResult<MallCashierCreateResponse> response = paymentClient.createCashier(request);
        MallCashierCreateResponse created = requirePaymentData(response);
        if (orderMapper.bindMallCashier(order.getOrderId(), CommonType.orderStatus.TO_BE_PAID.getCode(),
                created.getCashierNo()) != 1) {
            throw new DefinedException("订单绑定收银台失败");
        }
        return created;
    }

    public MallCashierStatusResponse queryStatus(String cashierNo, String userId) {
        if (!StringUtils.hasText(cashierNo)) throw new DefinedException("收银台单号不能为空");
        OrderBaseEntity order = orderMapper.selectMallOrderByCashierNo(cashierNo);
        verifyOwner(order, userId);
        return requirePaymentData(paymentClient.cashierStatus(cashierNo));
    }

    public Object refund(MallRefundRequest request, String userId) {
        if (request == null) throw new DefinedException("退款参数不能为空");
        OrderBaseEntity order = requireOwnedOrder(request.getMallOrderNo(), userId);
        if (order.getOrderPaymentAmount() == null || request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0
                || request.getAmount().compareTo(order.getOrderPaymentAmount()) > 0) {
            throw new DefinedException("退款金额无效");
        }
        request.setCashierNo(order.getPaymentNumber());
        return requirePaymentData(paymentClient.refund(request));
    }

    public String balance(String userId) {
        if (!StringUtils.hasText(userId)) throw new DefinedException("用户不能为空");
        return paymentClient.queryAccountBalance(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handlePaid(MallPaymentNotifyRequest request) {
        validateRequest(request);
        OrderBaseEntity order = orderMapper.selectMallPaymentOrder(request.getMallOrderNo());
        if (order == null) {
            throw new DefinedException("商城订单不存在: " + request.getMallOrderNo());
        }
        if (order.getOrderPaymentAmount() == null
                || order.getOrderPaymentAmount().compareTo(request.getAmount()) != 0) {
            throw new DefinedException("支付回调金额与订单应付金额不一致");
        }

        int pending = CommonType.orderStatus.TO_BE_PAID.getCode();
        int paid = CommonType.orderStatus.PAID.getCode();
        if (Integer.valueOf(paid).equals(order.getOrderStatus())) {
            verifyPaidIdentity(order, request);
            return;
        }
        if (!Integer.valueOf(pending).equals(order.getOrderStatus())) {
            throw new DefinedException("订单状态不允许支付: " + order.getOrderStatus());
        }

        int changed = orderMapper.markMallOrderPaid(request.getMallOrderNo(), pending, paid,
                request.getCashierNo(), request.getChannelOrderNo(), request.getPaidTime());
        if (changed == 0) {
            OrderBaseEntity current = orderMapper.selectMallPaymentOrder(request.getMallOrderNo());
            if (current == null || !Integer.valueOf(paid).equals(current.getOrderStatus())) {
                throw new DefinedException("订单支付状态更新失败");
            }
            verifyPaidIdentity(current, request);
            return;
        }
        inventoryLifecycleService.markPaid(request.getMallOrderNo());

        // 拼团（迁 Mall 收银台）：支付成功后按已付款人数判定成团，满员则推进团内已付款订单为已成团
        if (StringUtils.hasText(order.getPintuanMarkid())) {
            try {
                Map<String, Object> paidParam = new HashMap<>();
                paidParam.put("markId", order.getPintuanMarkid());
                paidParam.put("orderId", order.getOrderId());
                ResponseResult paidResp = activityServiceClient.markPaidAndEvaluate(paidParam);
                if (paidResp != null && paidResp.getData() instanceof Map) {
                    Map<?, ?> data = (Map<?, ?>) paidResp.getData();
                    Object formed = data.get("formed");
                    Object ids = data.get("orderIdList");
                    if (formed != null && "1".equals(String.valueOf(formed))
                            && ids instanceof List && !((List<?>) ids).isEmpty()) {
                        @SuppressWarnings("unchecked")
                        List<String> orderIdList = (List<String>) ids;
                        orderMapper.updateGroupBookingBaseStatus(orderIdList);
                    }
                }
            } catch (Exception e) {
                log.error("拼团支付成功成团判定失败 markId={} orderId={}", order.getPintuanMarkid(), order.getOrderId(), e);
            }
        }
    }

    private void validateRequest(MallPaymentNotifyRequest request) {
        if (request == null || !StringUtils.hasText(request.getMallOrderNo())
                || !StringUtils.hasText(request.getCashierNo()) || request.getAmount() == null) {
            throw new DefinedException("支付回调参数不完整");
        }
        if (!StringUtils.hasText(request.getStatus())
                || !SUCCESS_STATUSES.contains(request.getStatus().toUpperCase())) {
            throw new DefinedException("支付回调不是成功状态");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DefinedException("支付回调金额必须大于零");
        }
        if (request.getPaidTime() == null) {
            request.setPaidTime(LocalDateTime.now());
        }
    }

    private void verifyPaidIdentity(OrderBaseEntity order, MallPaymentNotifyRequest request) {
        if (StringUtils.hasText(order.getPaymentNumber())
                && !order.getPaymentNumber().equals(request.getCashierNo())) {
            throw new DefinedException("订单已由其他支付单完成");
        }
    }

    private OrderBaseEntity requireOwnedOrder(String orderNo, String userId) {
        OrderBaseEntity order = orderMapper.selectMallPaymentOrder(orderNo);
        verifyOwner(order, userId);
        return order;
    }

    private void verifyOwner(OrderBaseEntity order, String userId) {
        if (order == null) throw new DefinedException("商城订单不存在");
        if (!StringUtils.hasText(userId) || !userId.equals(order.getBuyerUserId())) {
            throw new DefinedException("无权访问该订单");
        }
    }

    private <T> T requirePaymentData(ResponseResult<T> response) {
        if (response == null || response.getCode() != 1) {
            throw new DefinedException(response == null ? "支付中心无响应" : response.getMessage());
        }
        return response.getData();
    }
}
