package com.houjiang.base.controller;

import com.houjiang.base.dto.BaseSmsConfigDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName BaseSmsInfoController
 * @Description
 * @Author Amos
 * @Date 2023/7/5 10:23
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/smsInfo")
public class BaseSmsInfoController extends BaseController{


    /**
     * 初始化SMS配置信息
     *
     * @param
     * @return
     */
    @PostMapping(value = "/selectSmsInfo")
    public ResponseResult selectSMSInfo2() {
        return baseSmsInfoService.selectSMSInfo2();
    }


    /**
     * update sms配置 存在就更新不存在就添加
     *
     * @param baseSmsConfigReq
     * @return
     */
    @PostMapping("/updateSmsConfig")
    public ResponseResult updateSmsConfig(@RequestBody BaseSmsConfigDTO baseSmsConfigReq) {
        return baseSmsInfoService.updateSmsConfig(baseSmsConfigReq);
    }
    /**
     * 更新SMS配置信息
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/updateSms")
    public ResponseResult updateSMS(@RequestBody BaseSmsConfigDTO baseSmsConfigReq) {
        return  baseSmsInfoService.updateSMS(baseSmsConfigReq);
    }
}
