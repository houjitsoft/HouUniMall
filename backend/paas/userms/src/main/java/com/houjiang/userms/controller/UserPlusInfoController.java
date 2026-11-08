package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName UserPlusInfoController
 * @Description plus会员信息
 * @Author Amos
 * @Date 2023/5/18 17:17
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/plusInfo")
public class UserPlusInfoController extends BaseController{



    /**
     * 查询plus会员信息
     *
     * @param map
     * @return
     */
    @PostMapping("/selectByUserId")
    public ResponseResult selectByUserId(@RequestBody Map<String, String> map) {
        ResponseResult result = userPlusInfoService.selectByUserId(map);
        return result;
    }

    /**
     * 查询当前用户的plus 会员信息 feign
     */
    @PostMapping("/queryUserPlusInfo")
    public ResponseResult queryUserPlusInfo(@RequestParam("userId") String userId) {
        ResponseResult result = userPlusInfoService.queryUserPlusInfo(userId);
        return result;
    }

    /**
     * 修改当前用户 plus信息表已用额度
     */
    @PostMapping("/updateUserPluslimit")
    public ResponseResult updateUserPluslimit(@RequestBody HashMap<Object, Object> hashMap) {
        ResponseResult result = userPlusInfoService.updateUserPluslimit(hashMap);
        return result;
    }

    /**
     * 开通plus会员
     * @param map
     * @return
     */
    @PostMapping("/plusMember/openMember")
    public ResponseResult plusMemberOpen(@RequestBody Map<String,Object> map){
        ResponseResult result = userPlusInfoService.plusMemberOpen(map);
        return result;
    }

    /**
     * 开通页面权益查询
     * @param map
     * @return
     */
    @PostMapping("/selectPlusMember")
    public ResponseResult selectPlusMember(@RequestBody Map<String,Object> map){
        ResponseResult result = userPlusInfoService.selectPlusMember(map);
        return result;
    }


    /**
     * 平台开通plus会员
     * @param map
     * @return
     */
    @PostMapping("/openMember")
    public ResponseResult openMember(@RequestBody Map<String, Object> map) throws IllegalAccessException {
        ResponseResult result = userPlusInfoService.openMember(map);
        return result;
    }

    /**
     * plus会员过期更新
     */
    @PostMapping("/updatePlus")
    public void updatePlus(){
        userPlusInfoService.plusMemberUpdate();

    }

    /**
     * plus会员额度更新
     */
    @PostMapping("/updateLimit")
    public ResponseResult updateLimit(){
        ResponseResult result = userPlusInfoService.updateLimit();
        return result;
    }


    /**
     * plus会员门槛红包发放
     */
    @PostMapping("/redgrant")
    public void redgrant(){
        userPlusInfoService.redgrant();
    }

    /**
     * 微信 支付宝 回调失败后 pilus 会员权益补偿
     * @param map
     * @return
     */
    @PostMapping("/plusOrderCompensate")
    public ResponseResult plusOrderCompensate(@RequestBody Map<String, Object> map)  {
        ResponseResult result = userPlusInfoService.plusOrderCompensate(map);
        return result;
    }
}
