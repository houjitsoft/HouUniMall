package com.houjiang.base.controller;


import com.houjiang.base.dto.BaseCurrencySymbolDTO;
import com.houjiang.base.service.BaseCurrencySymbolService;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * (BaseCurrencySymbol)表控制层
 *
 * @author Amos
 * @since 2024-05-23 16:56:35
 */
@RestController
@RequestMapping("base/currencySymbol")
public class BaseCurrencySymbolController {
    /**
     * 服务对象
     */
    @Autowired
    private BaseCurrencySymbolService baseCurrencySymbolService;

    /**
     * 查询货币符号列表
     * @param currencySymbolDTO 查询条件
     * @return 返回查询结果
     */
    @PostMapping("list")
    public ResponseResult listCurrencySymbol(@RequestBody BaseCurrencySymbolDTO currencySymbolDTO){
        return baseCurrencySymbolService.listCurrencySymbol(currencySymbolDTO);
    }
    /**
     * 提供开启货币符号功能的接口。
     *
     * @return ResponseResult 返回操作的结果，包括成功或失败的信息。
     */
    @GetMapping("open")
    public ResponseResult openCurrencySymbol(){
        // 调用服务层方法，执行开启货币符号的操作
        return baseCurrencySymbolService.openCurrencySymbol();
    }

    /**
     * 插入新的货币符号
     * @param currencySymbolDTO 货币符号信息
     * @return 返回操作结果
     */
    @PostMapping("insert")
    public ResponseResult insertCurrencySymbol(@RequestBody BaseCurrencySymbolDTO currencySymbolDTO){
        return baseCurrencySymbolService.insertCurrencySymbol(currencySymbolDTO);
    }

    /**
     * 更新货币符号信息
     * @param currencySymbolDTO 货币符号信息
     * @return 返回操作结果
     */
    @PostMapping("update")
    public ResponseResult updateCurrencySymbol(@RequestBody BaseCurrencySymbolDTO currencySymbolDTO){
        return baseCurrencySymbolService.updateCurrencySymbol(currencySymbolDTO);
    }

    /**
     * 删除货币符号
     * @param currencySymbolDTO 删除条件
     * @return 返回操作结果
     */
    @PostMapping("delete")
    public ResponseResult deleteCurrencySymbol(@RequestBody BaseCurrencySymbolDTO currencySymbolDTO){
        return baseCurrencySymbolService.deleteCurrencySymbol(currencySymbolDTO);
    }
    /**
     * 根据货币识别获取相关信息
     *
     * @param currencyCode 货币识别参数，用于指定查询的货币符号
     * @return 返回包含查询结果的ResponseResult对象
     */
    @GetMapping("/getByCurrencyDetection")
    public ResponseResult getByCurrencyDetection(@RequestParam("currencyCode") String currencyCode){
        return baseCurrencySymbolService.getByCurrencyDetection(currencyCode);
    }
}
