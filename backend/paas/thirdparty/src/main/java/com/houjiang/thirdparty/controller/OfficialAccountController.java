package com.houjiang.thirdparty.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.service.OfficialAccountService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/officialAccount")
public class OfficialAccountController {

    @Resource
    private OfficialAccountService officialAccountService;

    /**
     * 使用POST方法通过指定路径发送通知
     *
     * @param param 包含通知相关参数的Map对象，通过请求体传递
     * @return 返回处理结果，具体类型由officialAccountService定义
     * <p>
     * 该方法的主要作用是调用officialAccountService的sendOfficialAccountNotice方法
     * 以发送官方账号通知它不直接处理请求参数，而是将参数转发给服务层方法
     */
    @PostMapping("/sendNotice")
    public ResponseResult sendNotice(@RequestBody Map<String, Object> param) {
        return officialAccountService.sendOfficialAccountNotice(param);
    }
}
