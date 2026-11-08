package com.houjiang.userms.controller;


import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.vo.UserPersonalBaseVO;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * @ClassName UserAdminBaseController
 * @Description  后台用户基础信息表
 * @Author Amos
 * @Date 2023-06-27 14:05:30
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("platform/userinfo")
public class UserAdminBaseController extends BaseController{


    /**
     * 查询用户信息(后台系统)
     *
     * @param map
     * @return
     */
    @PostMapping("/getPlatformUserInfo")
    public ResponseResult getPlatformUserInfo(@RequestBody Map<String, String> map) throws IOException {
        ResponseResult result = userAdminBaseService.getPlatformUserInfo(map);
        return result;
    }



    /**
     * 获取用户信息
     */
    @RequestMapping("/getlimit")
    public ResponseResult getLimit(@RequestBody Map<String, String> paraMap) {
        ResponseResult limit = userAdminBaseService.getLimit(paraMap);
        return limit;
    }

    /**
     * 删除平台管理员
     *
     * @param param
     * @return
     */
    @PostMapping("/deleteAdminUser")
    public ResponseResult deleteAdminUserPass(@RequestBody UserPersonalBaseVO param) {
        ResponseResult result=userAdminBaseService.deleteAdminUserPass(param);
        return result;
    }


    /**
     * 平台用户 获取个人信息
     *
     * @param param
     * @return
     */
    @PostMapping("/limit/getPersonalBase")
    public ResponseResult getPersonalBase(@RequestBody UserPersonalBaseVO param) {
        ResponseResult result=userAdminBaseService.getPersonalBase(param);
        return result;
    }


    /**
     * 添加 管理员
     *
     * @param param
     * @return
     */
    @PostMapping("/addAdminUser")
    @SysLog("添加管理员")
    public ResponseResult addAdminUser(@RequestBody Map<String, String> param) {
        ResponseResult results = userAdminBaseService.addAdminUser(param);
        return results;
    }


    /**
     * 修改平台管理员 密码 权限
     *
     * @param param
     * @return
     */
    @PostMapping("/updateAdminUserPass")
    @SysLog("修改平台管理员密码权限")
    public ResponseResult updateAdminUserPass(@RequestBody Map<String, String> param) {
        ResponseResult results = userAdminBaseService.updateAdminUserPass(param);
        return results;
    }

    /**
     * 查询平台所有管理员
     *
     * @param param
     * @return
     */
    @PostMapping("/queryAdminUserList")
    public ResponseResult queryAdminUserList(@RequestBody UserPersonalBaseVO param) {
        ResponseResult results = userAdminBaseService.queryAdminUserList(param);
        return results;

    }
}
