package com.houjiang.order.service;

import com.houjiang.commons.dto.payment.MallPaymentNotifyRequest;
import com.houjiang.commons.dto.payment.MallCashierCreateRequest;
import com.houjiang.commons.dto.payment.MallCashierCreateResponse;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.order.entity.OrderBaseEntity;
import com.houjiang.order.mapper.OrderBaseMapper;
import com.houjiang.order.feign.PayMentServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
class MallPaymentApplicationServiceTest {

    @Mock
    private OrderBaseMapper orderMapper;
    @Mock
    private OrderInventoryLifecycleService inventoryLifecycleService;
    @Mock
    private PayMentServiceClient paymentClient;

    private MallPaymentApplicationService service;

    @BeforeEach
    void setUp() {
        service = new MallPaymentApplicationService(orderMapper, inventoryLifecycleService, paymentClient);
    }

    @Test
    void paidCallbackMovesPendingOrderOnce() {
        OrderBaseEntity pending = order("MO1", 1, "88.00", null);
        OrderBaseEntity paid = order("MO1", 2, "88.00", "CS1");
        when(orderMapper.selectMallPaymentOrder("MO1")).thenReturn(pending, paid);
        when(orderMapper.markMallOrderPaid("MO1", 1, 2, "CS1", "CH1", paidNotify().getPaidTime()))
                .thenReturn(1);

        service.handlePaid(paidNotify());
        assertDoesNotThrow(() -> service.handlePaid(paidNotify()));

        verify(orderMapper, times(1))
                .markMallOrderPaid("MO1", 1, 2, "CS1", "CH1", paidNotify().getPaidTime());
        verify(inventoryLifecycleService, times(1)).markPaid("MO1");
    }

    @Test
    void paidCallbackRejectsAmountMismatch() {
        when(orderMapper.selectMallPaymentOrder("MO1"))
                .thenReturn(order("MO1", 1, "89.00", null));

        assertThrows(DefinedException.class, () -> service.handlePaid(paidNotify()));
    }

    @Test
    void paidCallbackRejectsNonSuccessStatus() {
        MallPaymentNotifyRequest request = paidNotify();
        request.setStatus("FAILED");

        assertThrows(DefinedException.class, () -> service.handlePaid(request));
    }

    @Test
    void cashierCreateUsesDatabaseAmountAndOrderOwner() {
        OrderBaseEntity order = order("MO1", 1, "88.00", null);
        order.setBuyerUserId("U1");
        when(orderMapper.selectMallPaymentOrder("MO1")).thenReturn(order);
        MallCashierCreateResponse created = new MallCashierCreateResponse();
        created.setCashierNo("CS1");
        when(paymentClient.createCashier(argThat(request ->
                new BigDecimal("88.00").compareTo(request.getAmount()) == 0
                        && "U1".equals(request.getUserId()))))
                .thenReturn(ResponseResult.success(created));
        when(orderMapper.bindMallCashier("MO1", 1, "CS1")).thenReturn(1);
        MallCashierCreateRequest request = new MallCashierCreateRequest();
        request.setMallOrderNo("MO1");
        request.setUserId("U1");
        request.setAmount(new BigDecimal("0.01"));

        MallCashierCreateResponse result = service.createCashier(request);

        org.junit.jupiter.api.Assertions.assertEquals("CS1", result.getCashierNo());
    }

    private MallPaymentNotifyRequest paidNotify() {
        MallPaymentNotifyRequest request = new MallPaymentNotifyRequest();
        request.setMallOrderNo("MO1");
        request.setCashierNo("CS1");
        request.setChannelOrderNo("CH1");
        request.setAmount(new BigDecimal("88.00"));
        request.setStatus("SUCCESS");
        request.setPaidTime(LocalDateTime.of(2026, 7, 10, 12, 30));
        return request;
    }

    private OrderBaseEntity order(String orderId, int status, String amount, String cashierNo) {
        OrderBaseEntity order = new OrderBaseEntity();
        order.setOrderId(orderId);
        order.setOrderStatus(status);
        order.setOrderPaymentAmount(new BigDecimal(amount));
        order.setPaymentNumber(cashierNo);
        return order;
    }
}
