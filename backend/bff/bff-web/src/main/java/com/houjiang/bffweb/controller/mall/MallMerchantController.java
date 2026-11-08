package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MerchantServiceFeign;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 商家经营主体管理，与商家下属门店管理分离。 */
@RestController
@RequestMapping("/web/mall/merchant")
@RequiredArgsConstructor
public class MallMerchantController {

    private final MerchantServiceFeign merchantServiceFeign;

    @PostMapping("/list")
    public Object list(@RequestBody(required = false) Map<String, Object> params) {
        return merchantServiceFeign.listProvisionedShops(params == null ? Map.of() : params);
    }

    @PostMapping("/detail")
    public Object detail(@RequestBody Map<String, Object> params) {
        return merchantServiceFeign.getProvisionedShopDetail(params);
    }

    @PostMapping("/create")
    public Object create(@RequestBody Map<String, Object> params) {
        return merchantServiceFeign.createProvisionedShop(params);
    }

    @PostMapping("/options")
    public Object options() {
        return merchantServiceFeign.provisionShopOptions();
    }

    @PostMapping("/account-options")
    public Object accountOptions(@RequestBody(required = false) Map<String, Object> params) {
        return merchantServiceFeign.provisionShopAccountOptions(params == null ? Map.of() : params);
    }

    @PostMapping("/status")
    public Object status(@RequestBody Map<String, Object> params) {
        return merchantServiceFeign.updateProvisionedShopStatus(params);
    }

    @PostMapping("/update")
    public Object update(@RequestBody Map<String, Object> params) {
        return merchantServiceFeign.updateProvisionedShop(params);
    }
}
