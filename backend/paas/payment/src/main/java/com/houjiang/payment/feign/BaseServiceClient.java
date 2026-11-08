package com.houjiang.payment.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Amos
 * @date 2023/4/3 16:42
 */
@FeignClient("base")
public interface BaseServiceClient {

    /**
     * 根据configKey查configValue
     *
     * @param configKey
     * @return
     */
    @GetMapping("/base/sysConfig/selectConfigValueByKey")
    String selectConfigValueByKey(@RequestParam("configKey") String configKey);

    /**
     * 查询地区
     * @param provinceId
     * @return
     */
    @PostMapping("/base/district/queryProvinceport")
    String queryProvinceport(@RequestParam("provinceId") String provinceId);
    /**
     * 根据货币识别获取相关信息
     *
     * @param currencyCode 货币识别参数，用于指定查询的货币符号
     * @return 返回包含查询结果的ResponseResult对象
     */
    @GetMapping("/base/currencySymbol/getByCurrencyDetection")
    ResponseResult getByCurrencyDetection(@RequestParam("currencyCode") String currencyCode);

}
