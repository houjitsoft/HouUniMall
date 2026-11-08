package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.CardAmountDTO;
import com.houjiang.payment.dto.RechargeCardDTO;
import com.houjiang.payment.dto.ReturnCardBaseInfoDTO;
import io.seata.core.exception.TransactionException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName PayCardBaseController
 * @Description
 * @Author Amos
 * @Date 2023/7/19 13:28
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("pay/cardBase")
public class PayCardBaseController extends BaseController {

    /**
     * 充值卡详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/queryDetail")
    public ResponseResult queryDetail(@RequestBody ReturnCardBaseInfoDTO dto) {
        ResponseResult result = payCardBaseService.queryDetail(dto);
        return result;
    }

    /**
     * 充值卡管理列表
     *
     * @param map
     * @return
     */
    @PostMapping("/queryList")
    public ResponseResult queryList(@RequestBody Map<String, Object> map) {
        ResponseResult result = payCardBaseService.queryList(map);
        return result;
    }

    /**
     * 查询购物卡消费支出列表 导出
     *
     * @param param
     * @return
     */
    @PostMapping("/consume/queryExcel")
    public ResponseResult queryCardConsumeExcel(@RequestBody CardAmountDTO param) {
        ResponseResult result = payCardBaseService.queryCardConsumeExcel(param);
        return result;
    }

    /**
     * 查询购物卡消费支出列表
     *
     * @param param
     * @return
     */
    @PostMapping("/consume/query")
    public ResponseResult queryCardConsume(@RequestBody CardAmountDTO param) {
        ResponseResult result = payCardBaseService.queryCardConsume(param);
        return result;
    }

    /**
     * 平台查询 激活充值卡列表 导出
     *
     * @param cardAmountQueryParam
     * @return
     */
    @PostMapping("/amount/queryExcel")
    public ResponseResult queryCardAmountListExcel(@RequestBody CardAmountDTO cardAmountQueryParam) {
        ResponseResult result = payCardBaseService.queryCardAmountListExcel(cardAmountQueryParam);
        return result;
    }

    /**
     * 平台查询 激活充值卡列表
     *
     * @param cardAmountQueryParam
     * @return
     */
    @PostMapping("/amount/query")
    public ResponseResult queryCardAmountList(@RequestBody CardAmountDTO cardAmountQueryParam) {
        ResponseResult result = payCardBaseService.queryCardAmountList(cardAmountQueryParam);
        return result;
    }

    /**
     * 平台 激活充值卡
     *
     * @param dto
     * @return
     */
    @PostMapping("/check")
    public ResponseResult check(@RequestBody RechargeCardDTO dto) throws TransactionException {
        ResponseResult result = payCardBaseService.check(dto);
        return result;
    }
}
