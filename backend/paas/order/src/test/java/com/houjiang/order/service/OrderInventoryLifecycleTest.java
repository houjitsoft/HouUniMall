package com.houjiang.order.service;

import com.houjiang.commons.dto.mall.MallStockReservationRequest;
import com.houjiang.order.feign.GoodsStockLifecycleClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderInventoryLifecycleTest {

    @Mock
    private GoodsStockLifecycleClient stockClient;

    private OrderInventoryLifecycleService service;

    @BeforeEach
    void setUp() {
        service = new OrderInventoryLifecycleService(stockClient);
    }

    @Test
    void closedUnpaidOrderReleasesReservation() {
        MallStockReservationRequest line = line();

        service.reserve("MO2", List.of(line));
        service.closeUnpaid("MO2");

        verify(stockClient).reserve(List.of(line));
        verify(stockClient).release("MO2");
    }

    @Test
    void paidOrderCommitsReservation() {
        service.markPaid("MO2");

        verify(stockClient).commit("MO2");
    }

    @Test
    void refundWithoutWarehouseReceiptCannotRestoreStock() {
        assertThrows(IllegalStateException.class,
                () -> service.returnToStock("MO2", false));
    }

    private MallStockReservationRequest line() {
        MallStockReservationRequest request = new MallStockReservationRequest();
        request.setOrderNo("MO2");
        request.setWarehouseId(1L);
        request.setSkuId(10L);
        request.setQuantity(2);
        return request;
    }
}
