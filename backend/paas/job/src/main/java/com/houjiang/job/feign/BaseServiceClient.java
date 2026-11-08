package com.houjiang.job.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Amos
 * @date 2023/5/17 15:43
 */
@FeignClient("base")
public interface BaseServiceClient {


    /**
     * 查询积分规则
     *
     * @return
     */
    @PostMapping("/base/integralRules/queryIntegralRules")
    ResponseResult queryIntegralRules();


    /**
     * 查询微客配置
     *
     * @return
     */
    @GetMapping("baseVkSet/queryGoodsVkSet")
    ResponseResult queryGoodsVkSet();

    /**
     * 根据configKey 查询配置 feign
     */
    @PostMapping("base/sysConfig/queryConfigKeyFeign")
    String selectConfigValueFeign(@RequestParam("configKey") String configKey);

    /**
     * 发送消息模板通知（站内信 + 短信 + 微信，由 base 侧根据模板开关分发）。
     * 秒杀开抢预约提醒等无请求上下文的定时任务复用此端点。
     */
    @PostMapping("/baseMessageTemplate/sendMessageTemplate")
    ResponseResult sendMessageTemplate(@RequestBody Map<String, Object> param);

}
