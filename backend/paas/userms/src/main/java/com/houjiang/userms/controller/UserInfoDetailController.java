package com.houjiang.userms.controller;

import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.util.ResponseResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName UserInfoDetailController
 * @Description
 * @Author Amos
 * @Date 2023/7/18 11:40
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/detail")
public class UserInfoDetailController extends BaseController{

    /**
     * 用户按月统计数量
     *
     * @param map
     * @return
     */
    @PostMapping("/userGroupingStatistics")
    public ResponseResult userGroupingStatistics(@RequestBody Map<String, Object> map) {
        ResponseResult result = userInfoDetailService.userGroupingStatistics(map);
        return result;
    }

    /**
     * 获取用户安全等级
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/h5/getUserSecurityLevel")
    public ResponseResult getUserSecurityLevel(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userInfoDetailService.getUserSecurityLevel(paraMap);
        return result;
    }

    /**
     * 删除用户信息
     * @param userId
     * @return
     */
    @PostMapping("/h5/delUserInfo")
    public ResponseResult delUserInfo(@RequestParam String userId) {
        ResponseResult result =userInfoDetailService.delUserInfo(userId);
        return result;
    }

    /**
     * 更新昵称
     * @param paraMap
     * @return
     */
    @SensitiveCheck(field = {"userNickname"})
    @PostMapping("/h5/updateUserNickname")
    public ResponseResult updateUserNickname(@RequestBody Map<String, String> paraMap){
        ResponseResult result =userInfoDetailService.updateUserNickname(paraMap);
        return result;
    }

    /**
     * 查找用户的实名信息
     *
     * @param map
     * @return
     */
    @PostMapping("/queryShopPersonInfo")
    public ResponseResult queryShopPersonInfo(@RequestBody Map<String, Object> map) {
        return userInfoDetailService.queryShopPersonInfo(map);
    }

    /**
     * feign调用
     * @param param
     * @return
     */
    @PostMapping("/feign/queryShopPersonInfoFeign")
    public Map<String, Object> queryShopPersonInfoFeign(@RequestBody Map<String, Object> param){
        Map<String, Object> result = userInfoDetailService.queryShopPersonInfoFeign(param);
        return result;
    }
}
