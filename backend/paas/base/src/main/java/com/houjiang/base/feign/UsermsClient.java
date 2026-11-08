package com.houjiang.base.feign;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.PlusEquityEntityVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@FeignClient("userms")
public interface UsermsClient {

    /**
     * 查询用户积分信息
     */
    @PostMapping(value = "/user/ms/getuserMemberInformation")
    ResponseResult getUserMemberInformation(@RequestBody Map<String, String> paraMap);


    /**
     * 查询会员权益
     *
     * @param id
     * @return
     */
    @PostMapping("/user/plusEquity/query")
    ResponseResult selectplusequitys(@RequestBody List<Integer> id);


    /**
     * 更新用户信息UserInfo单表
     */
    @PostMapping(value = "/user/ms/updateUser")
    ResponseResult updateUser(@RequestBody Map<String, Object> paraMap);

    /**
     * 根据shopId 查询店铺信息
     */
    @GetMapping(value = "/user/ms/queryShopUserInfo")
    ResponseResult queryShopUserInfo(@RequestParam("shopId") Integer shopId);

    /**
     * 根据会员id 查询会员账号 feign
     * @param userId
     * @return
     */
    @PostMapping("user/ms/queryUserMobileByuserId")
    Map<String,String> queryUserMobileByuserId(@RequestParam String userId);

    /**
     * 发送站内信 feign
     *
     * @param param
     * @return
     */
    @PostMapping("user/platformMessage/sendPlatformMessage/feign")
    ResponseResult sendPlatformMessageFeign(@RequestBody Map<String, Object> param);


    /**
     * 查询会员权益
     * @param id
     * @return
     */
    @PostMapping("/user/plusEquity/feign/selectplusequitys")
    List<PlusEquityEntityVO> selectplusequitysFeign(@RequestBody  List<Integer> id);


    /**
     * 查询会员等级下会员
     * @param id
     * @return
     */
    @GetMapping("/user/ms/feign/queryMemberMessageListIntegral")
    Integer queryMemberMessageListIntegral(@RequestParam("id")  String id);
}
