package com.houjiang.job.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author Amos
 * @date 2023/3/21 10:59
 */
@FeignClient("supplier")
public interface SupplierServiceClient {

    /**
     * job 供应商商品定时上架
     */
    @PostMapping("/supplierGoodsBase/info/feign/updateSupplierGoodsTimeShelfJob")
    void updateSupplierGoodsTimeShelfJob();

}
