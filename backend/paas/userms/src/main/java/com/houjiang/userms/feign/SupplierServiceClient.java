package com.houjiang.userms.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
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
    ResponseResult queryShopStatus(@RequestBody Map<String, String> map);

    /**
     * 查找店铺的活动权限
     *
     * @param shopId
     * @return
     */
    @GetMapping("/supplierShopLevel/queryMarketingActivitiesList")
    List<String> queryMarketingActivitiesList(@RequestParam("shopId") Integer shopId);
}
