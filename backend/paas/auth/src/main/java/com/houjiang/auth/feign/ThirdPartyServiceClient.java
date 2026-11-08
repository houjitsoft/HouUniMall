package com.houjiang.auth.feign;


import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.Map;

/**
 * @FeignClient 第三方服务调用接口
 * @author Amos
 */
@FeignClient("thirdparty")
public interface ThirdPartyServiceClient {

     /**
     * 发送手机验证码
     * @param paraMap
     * @return
     * @throws IOException
     */
     @PostMapping("sms/code/threecheckphoneandsend")
     ResponseResult threeCheckPhoneAndSend(@RequestBody Map<String, String> paraMap);

     /**
      * 发送手机验证码2
      * @param map
      * @return
      */
     @PostMapping(value = "/sms/verifiPhoneCode2")
     public ResponseResult verifiPhoneCode2(@RequestBody Map<String,Object> map);
}
