package com.houjiang.payment.service;

import com.houjiang.commons.util.ResponseResult;
import io.seata.core.exception.TransactionException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 支付模块统一接口
 *
 * @author Amos
 * @date 2023/4/27 9:28
 */
public interface PayStrategyHandleService {

    /**
     * 支付方法
     *
     * @param param    支付参数
     * @param request
     * @param response
     * @return
     */
    ResponseResult userPayEntrance(Map<Object, Object> param, HttpServletRequest request, HttpServletResponse response) throws TransactionException;
}
