package com.houjiang.merchant.service.impl.finance;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinanceWalletServiceImplTest {

    @Test
    void mapsAccountTypeToChangeObject() throws Exception {
        Method method = FinanceWalletServiceImpl.class.getDeclaredMethod("changeObjectForAccountType", String.class);
        method.setAccessible(true);

        assertEquals(0, method.invoke(null, "balance"));
        assertEquals(1, method.invoke(null, "commission"));
        assertEquals(null, method.invoke(null, ""));
    }
}
