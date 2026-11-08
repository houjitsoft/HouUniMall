package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 用户收货地址服务 Feign 客户端
 *
 * @description 专用于调用 userms 的 UserAddressController，其 @RequestMapping 为 /user/address，
 *              不属于 UserInfoController 的 /user/ms 前缀。因此单独设置 path = /user，
 *              与 UserDeptServiceFeign 的做法一致，避免与 UserServiceFeign 的前缀冲突导致 404。
 * @author Amos
 */
@FeignClient(
        name = "userms-address",
        url = "${feign.userms.url:http://127.0.0.1:8195}",
        path = "/user",
        fallback = UserAddressServiceFeignFallback.class
)
public interface UserAddressServiceFeign {

    /**
     * 查询用户收货地址列表（请求体需含 token）
     */
    @PostMapping("/address/get")
    ResponseResult getAddressList(@RequestBody Map<String, String> paraMap);

    /**
     * 查询默认地址（无默认则返回第一条；请求体需含 token）
     */
    @PostMapping("/address/getDef")
    ResponseResult getDefaultAddress(@RequestBody Map<String, String> paraMap);

    /**
     * 新增收货地址（请求体需含 token 与 userAddress* 字段）
     */
    @PostMapping("/address/add")
    ResponseResult addAddress(@RequestBody Map<String, String> paraMap);

    /**
     * 更新收货地址（请求体需含 token 与 userAddressId）
     */
    @PostMapping("/address/update")
    ResponseResult updateAddress(@RequestBody Map<String, String> paraMap);

    /**
     * 删除收货地址（请求体需含 token 与 userAddressId）
     */
    @PostMapping("/address/delete")
    ResponseResult deleteAddress(@RequestBody Map<String, String> paraMap);

    /**
     * 设置默认地址（请求体需含 token 与 userAddressId）
     */
    @PostMapping("/address/setDefault")
    ResponseResult setDefaultAddress(@RequestBody Map<String, String> paraMap);
}
