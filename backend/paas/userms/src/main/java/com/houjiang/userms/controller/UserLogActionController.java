package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName UserLogActionController
 * @Description
 * @Author Amos
 * @Date 2023/7/19 10:35
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/logAction")
public class UserLogActionController extends BaseController{

    /**
     * 查询用户操作日志列表
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/query/operateLog")
    public ResponseResult queryUserOperateLog(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result=userLogActionService.queryUserOperateLog(paraMap);
        return result;
    }
}
