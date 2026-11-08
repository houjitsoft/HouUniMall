package com.houjiang.payment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @ClassName SupplierServiceClient
 * @Description
 * @Author Amos
 * @Date 2025/2/28 16:20
 * @Version 1.0
 * @author Amos
 */
@FeignClient("supplier")
public interface SupplierServiceClient {

    /**
     * 根据供应商店铺id查询用户id
     * @param shopId
     * @return
     */
    @GetMapping("/supplierShopBase/info/feign/queryShopId")
    String queryShopId(@RequestParam("shopId") String shopId);

    /**
     * 查找供应商名称
     * @param supplyShopId
     * @return
     */
    @GetMapping("/supplierShopBase/info/feign/selectSupplierShopName")
    String selectSupplierShopName(@RequestParam("supplyShopId") Integer supplyShopId);
}
