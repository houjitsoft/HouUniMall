package com.houjiang.base.controller;

import com.houjiang.base.dto.IntegralRulesDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName BaseIntegralRulesController
 * @Description 积分规则
 * @Author Amos
 * @Date 2023/5/19 9:12
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/integralRules")
public class BaseIntegralRulesController extends BaseController{



    /**
     * 查询积分规则
     *
     * @return
     */
    @PostMapping("/queryIntegralRules")
    public ResponseResult queryIntegralRules() {
        ResponseResult result = baseIntegralRulesService.queryIntegralRules();;
        return result;
    }


    /**
     * 添加 或 修改积分规则
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/addOrUpdate")
    public ResponseResult integralRulesAddOrUpdate(@RequestBody IntegralRulesDTO param) {
        ResponseResult result = baseIntegralRulesService.integralRulesAddOrUpdate(param);;
        return result;
    }
}
