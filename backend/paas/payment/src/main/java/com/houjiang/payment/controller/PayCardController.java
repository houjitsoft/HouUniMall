package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.CheckRechargeCardDTO;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName PayCardController
 * @Description
 * @Author Amos
 * @Date 2023/7/27 9:42
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("pay/card")
public class PayCardController extends BaseController {

    /**
     * 查询激活数据
     *
     * @param param
     * @return
     */
    @PostMapping("/query")
    public ResponseResult query(@RequestBody CheckRechargeCardDTO param) {
        ResponseResult result = payCardService.query(param);
        return result;
    }
}
