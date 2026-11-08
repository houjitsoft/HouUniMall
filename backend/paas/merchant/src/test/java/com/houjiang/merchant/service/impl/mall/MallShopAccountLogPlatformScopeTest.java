package com.houjiang.merchant.service.impl.mall;

import com.houjiang.merchant.validate.mall.MallShopAccountLogSearchValidate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MallShopAccountLogPlatformScopeTest {

    @Test
    void platformSearchCanRepresentNoMatchingShopsWithoutFallingBackToAllShops() {
        MallShopAccountLogSearchValidate search = new MallShopAccountLogSearchValidate();
        search.setShopIds(List.of());

        assertEquals(List.of(), search.getShopIds());
        assertEquals(null, search.getShopId());
    }
}
