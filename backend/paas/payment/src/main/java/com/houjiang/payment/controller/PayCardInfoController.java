package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import io.seata.core.exception.TransactionException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @ClassName PayCardInfoController
 * @Description 购物卡
 * @Author Amos
 * @Date 2023/6/26 14:53
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("pay/cardInfo")
public class PayCardInfoController extends BaseController {

    /**
     * 充值卡充值
     *
     * @param map
     * @return
     */
    @PostMapping("/rechargeCard")
    public ResponseResult RechargeCard(@RequestBody Map<Object, Object> map) throws TransactionException {
        ResponseResult result = payCardInfoService.rechargeCard(map);
        return result;
    }

    /**
     * @param paraMap
     * @param response
     * @return 已完成导出办卡 @throws IOException
     */
    @PostMapping("/execl")
    public ResponseResult execl(@RequestBody(required = false) Map<String, Object> paraMap,
        HttpServletResponse response) throws IOException {
        ResponseResult result = payCardInfoService.execl(paraMap, response);
        return result;
    }
}
