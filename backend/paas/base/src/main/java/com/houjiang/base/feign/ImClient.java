package com.houjiang.base.feign;


import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import com.houjiang.commons.dto.userms.UserTokenIntegralDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Amos
 */
@FeignClient(name = "im", configuration = ImFeignConfig.class)
public interface ImClient {
    /**
     * 根据配置键查询即时通讯配置信息
     *
     * @param config 配置键，用于查询特定的即时通讯配置
     * @return ResponseResult 包含查询结果的响应对象
     */
    @GetMapping("/imConfig/queryCustomerServiceConfig")
    ResponseResult queryImConfig(@RequestParam("configKey") String config);

    /**
     * 查询ImAppKey
     * @return
     */
    @GetMapping("/imConfig/queryImKeySecret")
    ResponseResult queryImKeySecret();

    /**
     * 根据商城用户id 查询是否有消息未读
     * @param mallUserId 商城用户ID，用于查询未读消息
     * @return 返回查询结果，包含是否有未读消息的信息
     */
    @GetMapping("/imChatHistory/queryImChatHistoryRead")
    ResponseResult queryImChatHistoryRead(@RequestParam("mallUserId")String mallUserId);

}
