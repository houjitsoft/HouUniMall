package com.houjiang.base.controller;


import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName BaseMerchantPlusRuleController
 * @Description plus 会员
 * @Author Amos
 * @Date 2023/5/19 18:04
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/merchantPlusRule")
public class BaseMerchantPlusRuleController extends BaseController {


    /**
     * 查询plus 会员 是否开启  feign
     */
    @PostMapping("/query")
    public ResponseResult queryMerchantPlusRule() {
        return baseMerchantPlusRuleService.queryMerchantPlusRule();
    }


    /**
     * plus 用户协议查询
     * @return
     */
    @RequestMapping("/selectPlusUser")
    public ResponseResult selectPlusUser(){
        return baseMerchantPlusRuleService.selectPlusUser();
    }

    /**
     * plus 用户协议查询
     * @return
     */
    @RequestMapping("/getBusinessRule")
    public ResponseResult getBusinessRule(){
        return baseMerchantPlusRuleService.getBusinessRule();
    }

    /**
     * 查询plus规则
     * @param map
     * @return
     */
    @PostMapping("/ruleSelect")
    public ResponseResult ruleselect(@RequestBody Map<String, Object> map) {
        ResponseResult result = baseMerchantPlusRuleService.ruleselect(map);
        return result;
    }

    /**
     *更新plus规则
/**
     * @param map
     * @return
     */
    @PostMapping("/ruleUpdate")
    public ResponseResult updateplusrule(@RequestBody Map<String, Object> map) {
        ResponseResult result = baseMerchantPlusRuleService.updateplusrule(map);
        return result;
    }

    /**
     *  查询plus规则
     * @return
     */
    @PostMapping("/feign/ruleUpdate")
    public Integer selectplus(){
        Integer result = baseMerchantPlusRuleService.selectplus();
        return result;
    }

}

