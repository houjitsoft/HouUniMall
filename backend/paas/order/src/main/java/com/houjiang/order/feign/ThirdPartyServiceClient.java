package com.houjiang.order.feign;


import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @FeignClient 第三方服务调用接口
 * @author Amos
 */
@FeignClient("thirdparty")
public interface ThirdPartyServiceClient {

    /**
     * 平台发送提醒发货短信
     *
     * @param paraMap
     */
    @PostMapping("/sms/register/pushTheDelivery")
    ResponseResult pushTheDelivery(@RequestBody Map<String, String> paraMap);


    /**
     * 商家发货成功
     *
     * @param paraMap
     */
    @PostMapping("/sms/register/shipments")
    void shipments(@RequestBody Map<String, String> paraMap);

    /**
     * 商家端发货成功提醒短信
     *
     * @param paraMap
     */
    @PostMapping("/sms/register/goodsObligation")
    void goodsObligation(@RequestBody Map<String, String> paraMap);

    /**
     * TODO 补充方法说明
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/web/oss/upload")
    ResponseResult uploadToOssBase64(@RequestBody OssUploadUpdateParamDTO param);
}
