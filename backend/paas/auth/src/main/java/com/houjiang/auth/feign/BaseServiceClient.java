package com.houjiang.auth.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Amos
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
     * 根据用户id查询会员等级id
     *
     * @param userId
     * @return
     */
    @PostMapping("/base/tagClass/queryByUserId")
    ResponseResult getMemberClassIdByUserId(@RequestParam("userId") String userId);


    /**
     * 根据会员等级id查询会员等级
     *
     * @param id
     * @return
     */
    @PostMapping("/base/membershipGrade/getMemberNameByClassId")
    ResponseResult getMemberNameByClassId(@RequestParam("id") String id);


    /**
     * 根据configKey查configValue
     *
     * @param configKey
     * @return
     */
    @GetMapping("/base/sysConfig/selectByConfigKey")
    ResponseResult selectByConfigKey(@RequestParam("configKey") String configKey);


    /**
     * 根据会员等级id查询会员等级
     *
     * @param memberLevel 等级名称
     * @param userId      用户id
     * @return
     */
    @PostMapping("/base/membershipGrade/updateMemberGrade")
    void updateMemberGrade(@RequestParam("customerType") Integer customerType,
                           @RequestParam("memberLevel") String memberLevel,
                           @RequestParam("userId") String userId);

}
