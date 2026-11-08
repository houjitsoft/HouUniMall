package com.houjiang.job.feign;


import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Amos
 * @date 2023/3/20 11:30
 */
@FeignClient("userms")
public interface UserServiceClient {

    /**
     * 根据userId查用户信息
     *
     * @param userId
     * @return
     */
    @GetMapping("user/ms/job/queryUserInfoByjob")
    AcquiescenceVO queryUserInfoByjob(@RequestParam("userId") String userId);

    /**
     * 删除临时账号
     */
    @GetMapping("user/ms/job/deleteTemporaryAccount")
    void deleteTemporaryAccount();

    /**
     * 修改用户积分
     *
     * @param param
     * @return
     */
    @PostMapping("/user/ms/updateMemberIntegral")
    ResponseResult updateMemberIntegral(@RequestBody MemberMessageListIntegralDTO param);


    /**
     * 修改用户积分
     *
     * @param map
     * @return
     */
    @PostMapping("/user/pointsLog/insertByMap")
    ResponseResult insertPointLogByMap(@RequestBody Map<String, Object> map);


    /**
     * plus会员过期更新
     */
    @PostMapping("/user/plusInfo/updatePlus")
    void updateplus();


    /**
     * plus会员门槛红包发放
     */
    @PostMapping("/user/plusInfo/redgrant")
    void redgrant();

    /**
     * plus会员额度更新
     */
    @PostMapping("/user/plusInfo/updateLimit")
    ResponseResult updatelimit();

    /**
     * 微信 支付宝 回调失败后 pilus 会员权益补偿
     *
     * @param map
     * @return
     */
    @PostMapping("user/plusInfo/plusOrderCompensate")
    ResponseResult plusOrderCompensate(@RequestBody Map<String, Object> map);

    /**
     * 查询用户微客信息
     * @param paraMap
     * @return
     */
    @PostMapping("userVk/queryVKUserInfo")
    ResponseResult queryVKUserInfo(@RequestBody Map<String, String> paraMap);

}
