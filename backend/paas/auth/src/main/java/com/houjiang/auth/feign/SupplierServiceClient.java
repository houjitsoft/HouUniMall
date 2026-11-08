package com.houjiang.auth.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * @author Amos
 */
@FeignClient("supplier")
public interface SupplierServiceClient {

    /**
     * 查询是否正常商家
     *
     * @param map
     * @return
     */
    @PostMapping("/supplierShopBase/info/shopName/queryShop")
    ResponseResult queryShopStatus(@RequestBody Map<String, String> map,
            @RequestHeader("X-Auth-Supplier-Token") String serviceToken,
            @RequestHeader("Authorization") String authorization);

}
