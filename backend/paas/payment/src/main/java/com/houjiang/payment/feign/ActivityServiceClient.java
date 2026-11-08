package com.houjiang.payment.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author Amos
 * @date 2023/4/12 10:46
 */
@FeignClient("activity")
public interface ActivityServiceClient {

    /**
     * 修改拼团支付状态
     */
    @PostMapping(value = "/activity/piecingTogetherMark/updatePayStart")
    ResponseResult updatePinTuanPayStart(@RequestBody Map<String,Object> param);
}
