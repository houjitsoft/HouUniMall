package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.WithdrawRequestDTO;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @ClassName PayFxAccountController
 * @Description
 * @Author Amos
 * @Date 2023/8/25 9:39
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("pay/fxAccount")
public class PayFxAccountController extends BaseController{

    /**
     * 用户提现
     * @param withdrawRequest
     * @param request
     * @return
     */
    @RequestMapping("/withdraw")
    public ResponseResult withdraw(@RequestBody WithdrawRequestDTO withdrawRequest, HttpServletRequest request){
        ResponseResult result=payFxAccountService.withdraw(withdrawRequest,request);
        return result;
    }

}
