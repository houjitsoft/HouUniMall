package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName PayFxRecordController
 * @Description
 * @Author Amos
 * @Date 2023/8/25 9:30
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("pay/fxRecord")
public class PayFxRecordController extends BaseController{



    /**
     * 用户提现记录列表
     *
     * @param param
     * @return
     */
    @PostMapping("/query")
    public ResponseResult query(@RequestBody Map<String, Object> param) {
        ResponseResult result=payFxRecordService.query(param);
        return result;
    }
}
