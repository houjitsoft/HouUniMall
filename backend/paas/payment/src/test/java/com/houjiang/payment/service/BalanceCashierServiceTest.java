package com.houjiang.payment.service;

import com.houjiang.payment.channel.biz.PayChannelStrategyFactory;
import com.houjiang.payment.constant.CashierOrderStatusEnum;
import com.houjiang.payment.dto.cashier.CashierCreateRequest;
import com.houjiang.payment.entity.PayCashierOrderEntity;
import com.houjiang.payment.mapper.PayCashierOrderMapper;
import com.houjiang.payment.mapper.PayUserResourceMapper;
import com.houjiang.payment.mapper.PayRefundOrderMapper;
import com.houjiang.payment.entity.PayRefundOrderEntity;
import com.houjiang.payment.service.cashier.BalanceRefundService;
import com.houjiang.payment.service.cashier.WxJsapiPayHandler;
import com.houjiang.payment.service.impl.CashierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BalanceCashierServiceTest {

    private PayCashierOrderMapper cashierMapper;
    private PayUserResourceMapper resourceMapper;
    private CashierServiceImpl service;

    @BeforeEach
    void setUp() {
        cashierMapper = mock(PayCashierOrderMapper.class);
        resourceMapper = mock(PayUserResourceMapper.class);
        service = new CashierServiceImpl(cashierMapper, resourceMapper,
                mock(WxJsapiPayHandler.class), mock(PayChannelStrategyFactory.class));
    }

    @Test
    void creatingBalanceCashierNeverDebitsBeforePasswordVerification() {
        CashierCreateRequest request = new CashierCreateRequest();
        request.setTerminal("PC");
        request.setPayMethod("BALANCE");
        request.setAmount(new BigDecimal("30.00"));

        var response = service.create(request, "U1");

        assertEquals(CashierOrderStatusEnum.WAIT_PAY.getCode(), response.getStatus());
        assertEquals(true, response.getPayData().get("requiresPassword"));
        verify(resourceMapper, never()).updateMoney(any(), any());
    }

    @Test
    void repeatedBalancePayOnlyDebitsOnce() {
        PayCashierOrderEntity order = balanceOrder();
        when(cashierMapper.selectByCashierNo("CS003")).thenReturn(order);
        when(cashierMapper.claimForPay("CS003",
                CashierOrderStatusEnum.WAIT_PAY.getCode(), CashierOrderStatusEnum.PAYING.getCode()))
                .thenReturn(1);
        when(resourceMapper.deductBalance("U1", new BigDecimal("30.00"))).thenReturn(1);
        when(cashierMapper.markBalanceSuccess(any(), anyInt(), anyInt(), any(), any())).thenReturn(1);

        service.payByBalance("CS003", "U1");
        service.payByBalance("CS003", "U1");

        verify(resourceMapper, times(1)).deductBalance("U1", new BigDecimal("30.00"));
        verify(cashierMapper, times(1)).claimForPay("CS003",
                CashierOrderStatusEnum.WAIT_PAY.getCode(), CashierOrderStatusEnum.PAYING.getCode());
    }

    @Test
    void repeatedBalanceRefundOnlyCreditsOnce() {
        PayRefundOrderMapper refundMapper = mock(PayRefundOrderMapper.class);
        PayRefundOrderEntity completed = new PayRefundOrderEntity();
        completed.setRefundNo("RF003");
        completed.setStatus("SUCCESS");
        when(refundMapper.selectByRefundNo("RF003")).thenReturn(null, completed);
        when(refundMapper.insertIfAbsent(any())).thenReturn(1);
        when(refundMapper.updateById(any(PayRefundOrderEntity.class))).thenReturn(1);
        when(resourceMapper.creditBalance("U1", new BigDecimal("10.00"))).thenReturn(1);
        BalanceRefundService refundService = new BalanceRefundService(refundMapper, resourceMapper);

        refundService.refund("RF003", "CS003", "MO003", "U1", new BigDecimal("10.00"), "refund");
        refundService.refund("RF003", "CS003", "MO003", "U1", new BigDecimal("10.00"), "refund");

        verify(resourceMapper, times(1)).creditBalance("U1", new BigDecimal("10.00"));
    }

    private PayCashierOrderEntity balanceOrder() {
        PayCashierOrderEntity order = new PayCashierOrderEntity();
        order.setCashierNo("CS003");
        order.setUserId("U1");
        order.setPayMethod("BALANCE");
        order.setAmount(new BigDecimal("30.00"));
        order.setStatus(CashierOrderStatusEnum.WAIT_PAY.getCode());
        order.setExpireTime(new Date(System.currentTimeMillis() + 60_000));
        return order;
    }
}
