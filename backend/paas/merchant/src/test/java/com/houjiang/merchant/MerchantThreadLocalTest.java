package com.houjiang.merchant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MerchantThreadLocalTest {

    @AfterEach
    void clear() {
        MerchantThreadLocal.remove();
    }

    @Test
    void returnsCurrentMerchantIdentity() {
        MerchantThreadLocal.put("adminId", 7);
        MerchantThreadLocal.put("shopId", 12);
        MerchantThreadLocal.put("isSuper", 0);

        assertEquals(7, MerchantThreadLocal.getAdminId());
        assertEquals(12, MerchantThreadLocal.getShopId());
        assertEquals(0, MerchantThreadLocal.getIsSuper());
    }

    @Test
    void isolatesMerchantIdentityBetweenThreads() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch read = new CountDownLatch(1);

        Future<?> first = executor.submit(() -> assertThreadIdentity(7, 12, 1, ready, read));
        Future<?> second = executor.submit(() -> assertThreadIdentity(27, 32, 0, ready, read));

        try {
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            assertEquals(0, MerchantThreadLocal.getAdminId());
            assertEquals(0, MerchantThreadLocal.getShopId());
            assertEquals(0, MerchantThreadLocal.getIsSuper());

            read.countDown();
            first.get(5, TimeUnit.SECONDS);
            second.get(5, TimeUnit.SECONDS);
        } finally {
            read.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private static void assertThreadIdentity(
            int adminId,
            int shopId,
            int isSuper,
            CountDownLatch ready,
            CountDownLatch read) {
        try {
            MerchantThreadLocal.put("adminId", adminId);
            MerchantThreadLocal.put("shopId", shopId);
            MerchantThreadLocal.put("isSuper", isSuper);
            ready.countDown();

            assertTrue(read.await(5, TimeUnit.SECONDS));
            assertEquals(adminId, MerchantThreadLocal.getAdminId());
            assertEquals(shopId, MerchantThreadLocal.getShopId());
            assertEquals(isSuper, MerchantThreadLocal.getIsSuper());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        } finally {
            MerchantThreadLocal.remove();
        }
    }
}
