package com.houjiang.userms.controller;

import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName UserPlatformMessageController
 * @Description
 * @Author Amos
 * @Date 2023/8/1 15:09
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/user/platformMessage")
public class UserPlatformMessageController extends BaseController{


    /**
     * 发送站内信
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/sendPlatformMessage")
    @SysLog("发送站内信")
    public ResponseResult sendPlatformMessage(@RequestBody Map<String, Object> param) {
        ResponseResult result = userPlatformMessageService.sendPlatformMessage(param);
        return result;
    }
    /**
     * 发送站内信 feign
     *
     * @param param
     * @return
     */
    @PostMapping("/sendPlatformMessage/feign")
    public ResponseResult sendPlatformMessageFeign(@RequestBody Map<String, Object> param) {
        ResponseResult result = userPlatformMessageService.sendPlatformMessageFeign(param);
        return result;
    }
    /**
     * 查询我的站内信
     *
     * @param param
     * @return
     */
    @PostMapping("/queryPlatformMessage")
    public ResponseResult queryPlatformMessage(@RequestBody Map<String, Object> param) {
        ResponseResult result = userPlatformMessageService.queryPlatformMessage(param);
        return result;
    }

    /**
     * 阅读站内信
     *
     * @param param
     * @return
     */
    @PostMapping("/readPlatformMessage")
    public ResponseResult readPlatformMessage(@RequestBody Map<String, Object> param) {
        ResponseResult result = userPlatformMessageService.readPlatformMessage(param);
        return result;
    }


    /**
     * 删除站内信
     *
     * @param param
     * @return
     */
    @PostMapping("/deletePlatformMessage")
    @SysLog("删除站内信")
    public ResponseResult deletePlatformMessage(@RequestBody Map<String, Object> param) {
        ResponseResult result = userPlatformMessageService.deletePlatformMessage(param);
        return result;
    }

}
