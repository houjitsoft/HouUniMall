package com.houjiang.thirdparty.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @FeignClient 指明我们要从注册中心中发现的服务的名称
 * @author Amos
 */
@FeignClient("userms")
public interface UserServiceClient {

    /**
     * 校验用户是否已经注册
     * @param paraMap
     * @return
     */
    @PostMapping("user/ms/checkPhoneNumberExist")
    String checkPhoneNumber(@RequestBody Map<String, String> paraMap);

    /**
     * 根据手机号查询用户是否存在
     *
     * @return
     */
    @RequestMapping("/user/ms/userAccountByMap")
    ResponseResult getUserAccountByMap(@RequestBody Map<String, String> map);
    /**
     * 根据用户id查找绑定业务员的openId
     *
     * @param userId
     * @return
     */
    @GetMapping("/user/bindConnect/queryUserOpenId")
    Map<String, String> queryUserOpenId(@RequestParam("userId") String userId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/user/ms/organization/checkUserOrgExists")
    ResponseResult checkUserOrgExists(
            @RequestParam("userId") String userId,
            @RequestParam("orgId") String orgId);
}
