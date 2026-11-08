package com.houjiang.payment.mapper;

import com.houjiang.payment.entity.PayCashierOrderEntity;
import com.houjiang.payment.entity.PayRefundOrderEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayCashierOrderMapperTest {

    @Test
    void cashierOrderCarriesBusinessIdempotencyKeys() {
        PayCashierOrderEntity order = new PayCashierOrderEntity();
        order.setRequestNo("REQ-001");
        order.setMallOrderNo("MO-001");

        assertEquals("REQ-001", order.getRequestNo());
        assertEquals("MO-001", order.getMallOrderNo());
    }

    @Test
    void refundOrderCarriesIndependentIdempotencyKey() {
        PayRefundOrderEntity refund = new PayRefundOrderEntity();
        refund.setRefundNo("RF-001");
        refund.setCashierNo("CS-001");
        refund.setMallOrderNo("MO-001");
        refund.setAmount(new BigDecimal("10.00"));

        assertEquals("RF-001", refund.getRefundNo());
        assertEquals("CS-001", refund.getCashierNo());
        assertEquals("MO-001", refund.getMallOrderNo());
        assertEquals(new BigDecimal("10.00"), refund.getAmount());
    }
}
