package com.houjiang.payment.controller;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.service.PayEntranceService;
import io.seata.core.exception.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 支付统一入口
 *
 * @author Amos
 * @date 2023/4/27 9:51
 */
@RestController
@RequestMapping("pay/entrance")
public class PayEntranceController extends BaseController {

    @Autowired
    private PayEntranceService payEntranceService;

    /**
     *支付统一入口
/**
     *
     * @param payInfo
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/userPay")
    public ResponseResult payMentUnifyInlet(@RequestBody Map<Object, Object> payInfo, HttpServletRequest request,
                                            HttpServletResponse response) throws TransactionException {

        ResponseResult result = payEntranceService.payMentUnifyInlet(payInfo, request, response);

        return result;
    }
}
