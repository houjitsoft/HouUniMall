package com.houjiang.order.feign;

import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/3/20 11:30
 */
@FeignClient("userms")
public interface UserServiceClient {

    /**
     * 查询当前用户的plus 会员信息 feign
     */
    @PostMapping("user/plusInfo/queryUserPlusInfo")
    ResponseResult queryUserPlusInfo(@RequestParam("userId") String userId);

    /**
     * 根据参数查用户信息
     *
     * @return
     */
    @PostMapping("/user/ms/queryUserInfo")
    ResponseResult queryUserInfo(@RequestBody UserInfoVO userInfoVO);


    /**
     * 根据userId获取用户信息
     *
     * @param userId
     * @return 结果
     */
    @GetMapping("/user/ms/queryUserInfoByUserId")
    ResponseResult queryUserInfoByUserId(@RequestParam("userId") String userId);

    /**
     * 根据用户id查询用户积分
     * @param userId
     * @return
     */
    @PostMapping("/user/ms/getMemberIntegralByUserId")
    Integer getMemberIntegralByUserId(@RequestParam("userId") String userId);
    /**
     * feign 根据用户id 查询当前用户冻结积分
     * @param userId
     * @return
     */
    @PostMapping("/user/ms/getMemberFrozenIntegraIntegralByUserId")
    Integer getMemberFrozenIntegraIntegralByUserId(@RequestParam("userId") String userId);
    /**
     * 更新plus信息表已用额度
     * @param hashMap
     * @return
     */
    @PostMapping("user/plusInfo/updateUserPluslimit")
    ResponseResult updateUserPluslimit(@RequestBody HashMap<Object, Object> hashMap);

    /**
     * 查询用户微客信息
     * @param paraMap
     * @return
     */
    @PostMapping("userVk/queryVKUserInfo")
    ResponseResult queryVKUserInfo(@RequestBody Map<String, String> paraMap);

    /**
     * 修改用户积分
     *
     * @param param
     * @return
     */
    @PostMapping("/user/ms/updateMemberIntegral")
    ResponseResult updateMemberIntegral(@RequestBody MemberMessageListIntegralDTO param);

    /**
     * 增加用户积分
     *
     * @return
     */
    @PostMapping("/user/pointsLog/insertByMap")
    ResponseResult insertPointLogByMap(@RequestBody Map<String, Object> map);


    /**
     * 根据shopId查询卖家信息
     *
     * @param returnMap
     * @return
     */
    @PostMapping("/user/sellerBase/info")
    ResponseResult getSellerInfo(@RequestBody Map<String, String> returnMap);

    /**
     * 根判断是否为未通过企业用户
     *
     * @param map
     * @return
     */
    @PostMapping("/user/businessInfo/infoByMap")
    ResponseResult getBusinessInfoByMap(@RequestBody Map<String, String> map);

    /**
     * 查询会员权益
     *
     * @param id
     * @return
     */
    @PostMapping("/user/plusEquity/query")
    ResponseResult selectplusequitys(@RequestBody List<Integer> id);

    /**
     * 增加用户积分
     *
     * @return
     */
    @PostMapping("/user/pointsLog/queryPointsByOrderId")
    Integer queryPointsByOrderId(@RequestParam("orderId") String orderId);

    /**
     * 根据会员id 查询会员账号 feign
     * @param userId
     * @return
     */
    @PostMapping("user/ms/queryUserMobileByuserId")
    String queryUserMobileByuserId(@RequestParam String userId);

    /**
     * 通过用户id查询mobile开头的openId-fegin
     *
     * @param userId
     * @return
     */
    @PostMapping("/user/bindConnect/queryUserOpenId")
    String queryBindInfoByuserId(@RequestParam("userId") String userId);


    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @PostMapping("/userVk/setVKInfo")
    ResponseResult setVKInfo(@RequestParam("orderId") String orderId);
}
