package com.houjiang.order.service;

import com.houjiang.commons.dto.mall.MallSkuSnapshot;
import com.houjiang.order.feign.GoodServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPricingServiceTest {

    @Mock
    private GoodServiceClient goodsClient;

    private OrderPricingService service;

    @BeforeEach
    void setUp() {
        service = new OrderPricingService(goodsClient);
    }

    @Test
    void orderUsesCurrentSkuPriceInsteadOfClientPrice() {
        when(goodsClient.requireSaleableSku(1L)).thenReturn(sku("39.90", 10));

        OrderPricingService.PricingResult result = service.price(List.of(
                new OrderPricingService.PricingItem(1L, 2, new BigDecimal("0.01"))));

        assertEquals(new BigDecimal("79.80"), result.goodsAmount());
        assertEquals(new BigDecimal("39.90"), result.lines().getFirst().unitPrice());
    }

    @Test
    void orderRejectsMissingSku() {
        assertThrows(IllegalArgumentException.class, () -> service.price(List.of(
                new OrderPricingService.PricingItem(null, 1, BigDecimal.ONE))));
    }

    @Test
    void orderRejectsQuantityAboveSaleableStock() {
        when(goodsClient.requireSaleableSku(1L)).thenReturn(sku("39.90", 1));

        assertThrows(IllegalStateException.class, () -> service.price(List.of(
                new OrderPricingService.PricingItem(1L, 2, BigDecimal.ONE))));
    }

    private MallSkuSnapshot sku(String price, int stock) {
        MallSkuSnapshot snapshot = new MallSkuSnapshot();
        snapshot.setSkuId(1L);
        snapshot.setGoodsId(10L);
        snapshot.setGoodsName("测试商品");
        snapshot.setSalePrice(new BigDecimal(price));
        snapshot.setSaleableStock(stock);
        snapshot.setSaleable(true);
        return snapshot;
    }
}
