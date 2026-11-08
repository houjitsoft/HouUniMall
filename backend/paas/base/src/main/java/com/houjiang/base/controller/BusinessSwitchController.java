package com.houjiang.base.controller;


import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
/**
 * @ClassName BusinessSwitchController
 * @Description 企业注册配置
 * @Author Amos
 * @Date 2023/5/19 18:04
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/businessSwitch")
public class BusinessSwitchController extends BaseController {



    /**
     * 查询企业注册开关
     *
     * @param
     * @return
     */
    @PostMapping(value = "/query")
    public ResponseResult query(@RequestBody Map<String, Object> param) {
        param.put("configKey", "business_switch");
        return businessSwitchService.query(param);
    }

    /**
     * 更新企业注册开关
     *
     * @param
     * @return
     */
    @PostMapping(value = "/update")
    public ResponseResult update(@RequestBody Map<String, Object> param) {
        param.put("configKey", "business_switch");
        return businessSwitchService.update(param);
    }

    /**
     * 查询注册密码配置
     *
     * @param
     * @return
     */
    @PostMapping(value = "/pwdConfig/query")
    public ResponseResult queryPwdConfig(@RequestBody Map<String, Object> param) {
        return businessSwitchService.queryPwdConfig(param);

    }

    /**
     * 更新注册密码配置
     *
     * @param
     * @return
     */
    @PostMapping(value = "/pwdConfig/update")
    public ResponseResult updatePwdConfig(@RequestBody Map<String, Object> param) {
        return businessSwitchService.updatePwdConfig(param);

    }

}
