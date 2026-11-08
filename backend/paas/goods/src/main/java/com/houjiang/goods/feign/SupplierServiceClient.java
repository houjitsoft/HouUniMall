package com.houjiang.goods.feign;

import com.houjiang.commons.dto.supplier.SupplierGoodsIsShelvesDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Amos
 * @date 2023/3/21 10:59
 */
@FeignClient("supplier")
public interface SupplierServiceClient {

    /**
     * 平台对供应商商品下架
     */
    @PostMapping("/supplierCommonBase/skuOffline")
    ResponseResult setSkuOfflineByPlatform(@RequestBody SupplierGoodsIsShelvesDTO param);

    /**
     * 平台对供应商商品下架
     */
    @PostMapping("/supplierCommonBase/platform/setViolation")
    ResponseResult setViolationByPlatform(@RequestBody SupplierGoodsIsShelvesDTO param);

    /**
     * 查询供应商商品库存
     *
     * @param supplierGoodsId
     * @return
     */
    @GetMapping("/supplierGoodsBase/info/feign/querySupplierGoodsStock")
    Integer querySupplierGoodsStock(@RequestParam("supplierGoodsId") Integer supplierGoodsId);

    /**
     * 添加商品版本信息
     * <p>
     * 本方法用于添加商品的版本信息，通过商品公共ID获取其最新版本信息，并将这些信息保存到数据库中
     */
    @PostMapping("/supplierGoodsVersion/addGoodsVersionToList")
    void addGoodsVersionToList(@RequestBody SupplierGoodsIsShelvesDTO param);
}
