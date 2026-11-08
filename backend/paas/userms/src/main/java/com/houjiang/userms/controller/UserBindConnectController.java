package com.houjiang.userms.controller;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName UserBindConnectController
 * @Description  用户绑定表
 * @Author Amos
 * @Date 2023-07-20 12:00:12
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/bindConnect")
public class UserBindConnectController extends BaseController {

    /**
     * 判断用户是否授权登录过
     */
    @PostMapping("/selectUserIdByOpenid")
    public String selectUserIdByOpenid(@RequestParam("bindOpenid") String bindOpenid){
        String result= userBindConnectService.selectUserIdByOpenid(bindOpenid);
        return result;
    }

    /**
     * 根据授权id 查询用户信息
     * @param bindId
     * @return
     */
    @PostMapping("/getUserInfoByUserId")
    public UserInfoVO getUserInfoByUserId(@RequestParam("bindOpenid")String bindId){
        UserInfoVO userInfoVO=userBindConnectService.getUserInfoByUserId(bindId);
        return userInfoVO;
    }
    /**
     * 修改当前用户 是否自动登录
     */
    @PostMapping("/updateAotuLogin")
    public ResponseResult updateAotuLogin(@RequestBody Map<String, Object> paramMap){
        ResponseResult result=userBindConnectService.updateAotuLogin(paramMap);
        return result;
    }

    /**
     * 插入第三方用户关联信息
     * @param userThreeBindMap
     */
    @PostMapping("/insertUserThreeBindMap")
    public ResponseResult insertUserThreeBindMap(@RequestBody Map<String, Object> userThreeBindMap){
        ResponseResult result=userBindConnectService.insertUserThreeBindMap(userThreeBindMap);
        return result;

    }

    /**
     * 查询用户是否自动登录
     */
    @PostMapping("/queryAutoLogin")
    public Integer queryAutoLogin(@RequestParam("openId") String openId){
        Integer result=userBindConnectService.queryAutoLogin(openId);
        return result;
    }
    /**
     * 根据用户id查找绑定授权的openId
     *
     * @param userId 用户id
     * @return 绑定授权的openId
     */
    @GetMapping("/queryUserOpenId")
    public Map<String, String> queryUserOpenId(@RequestParam("userId") String userId) {
        return userBindConnectService.queryUserOpenId(userId);
    }
    /**
     * 根据unionId插入openId
     *
     * @param openId
     * @param unionId
     */
    @PostMapping("/insertOpenIdByUnionId")
    void insertOpenIdByUnionId(@RequestParam("openId") String openId, @RequestParam("unionId") String unionId) {
        userBindConnectService.insertOpenIdByUnionId(openId, unionId);
    }
}
