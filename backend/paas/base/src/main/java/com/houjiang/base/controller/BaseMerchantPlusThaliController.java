package com.houjiang.base.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName BaseMerchantPlusThaliController
 * @Description plus会员套餐
 * @Author Amos
 * @Date 2023/7/4 9:53
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/plusRuleThali")
public class BaseMerchantPlusThaliController extends BaseController{


    /**
     * 查询plus 会员 套餐信息  feign
     */
    @PostMapping("/query")
    public ResponseResult queryMerchantPlusThali(@RequestBody Map<String, Object> map) throws IllegalAccessException {
        ResponseResult result=baseMerchantPlusThaliService.queryMerchantPlusThali(map);
        return result;
    }

    /**
     *plus套餐查询
/**
     * @param map
     * @return
     */
    @PostMapping("/selectPlusThali")
    public ResponseResult selectplusthali(@RequestBody Map<String, Object> map) {
        ResponseResult result=baseMerchantPlusThaliService.selectplusthali(map);
        return result;
    }

    /**
     *会员充值列表查看权益
/**
     * @param map
     * @return
     */
    @PostMapping("/renewPlusThali")
    public ResponseResult renewplusthali(@RequestBody Map<String, Object> map) {
        ResponseResult result=baseMerchantPlusThaliService.renewplusthali(map);
        return result;
    }

    /**
     * plus套餐设置
     * @param map
     * @return
     */
    @RequestMapping("/setPlusThali")
    public ResponseResult setplusthali(@RequestBody Map<String, Object> map) {
        ResponseResult result=baseMerchantPlusThaliService.setplusthali(map);
        return result;
    }


    /**
     * 查询折扣率
     * @return
     */
    @RequestMapping("/selectPlusDiscount")
    public ResponseResult selectplusdiscount(){
        ResponseResult result=baseMerchantPlusThaliService.selectplusdiscount();
        return result;
    }
}
