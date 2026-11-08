package com.houjiang.auth.feign;


import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Amos
 * @date 2023/3/27 10:06
 */
@FeignClient("payment")
public interface PayMentServiceClient {



    /**
     * 获取支付配置
     *
     * @param param
     * @return
     */
    @PostMapping("/pay/payConfig/queryFeign")
    ResponseResult selectPayConfig(@RequestBody Map<String, Object> param);


    /**
     * 查询品牌店铺小程序授权配置
     * @param map
     * @return
     */
    @PostMapping("pay/authorizationAppletInformation/queryConfigFeign")
    public ResponseResult queryShopAppletConfigFeign(@RequestBody Map<String,Object> map);

    /**
     * 删除微客信息
     * @param userId
     * @return
     */
    @PostMapping("/pay/payConfig/h5/delFxInfo")
    ResponseResult delFxInfo(@RequestParam String userId);
}
