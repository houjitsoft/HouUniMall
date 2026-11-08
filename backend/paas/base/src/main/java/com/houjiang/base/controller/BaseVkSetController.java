package com.houjiang.base.controller;


import com.houjiang.base.dto.CommissionSettingVO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.fx.GoodsVkSetVO;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName BaseVkSetController
 * @Description 微客配置表
 * @Author Amos
 * @Date 2023-06-05 10:48:39
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("baseVkSet")
public class BaseVkSetController extends BaseController {
    /**
     * 查询微客配置
     * @return
     */
    @GetMapping("queryGoodsVkSet")
    public ResponseResult queryGoodsVkSet() {
        GoodsVkSetVO goodsVkSet = baseVkSetService.queryGoodsVkSet();
        return ResponseResult.success(goodsVkSet);
    }


    /**
     * 添加或者修改分佣设置
     */
    @PostMapping("/addOrUpdateCommissionSetting")
    public ResponseResult addOrUpdateCommissionSetting(@RequestBody CommissionSettingVO param){
        ResponseResult result=baseVkSetService.addOrUpdateCommissionSetting(param);

        return result;
    }


    /**
     * 查询分佣设置
     */
    @GetMapping("/commissionQuerySetting")
    public ResponseResult commissionSetting(){
        ResponseResult result=baseVkSetService.commissionSetting();
        return result;
    }

    /**
     * 分佣订单售后时间更新
     * @return
     */
    @PostMapping("/updateOrderAfterSale")
    public ResponseResult updateOrderAfterSale(@RequestParam("orderId") String orderId) {
        return baseVkSetService.updateOrderAfterSale(orderId);
    }
}
