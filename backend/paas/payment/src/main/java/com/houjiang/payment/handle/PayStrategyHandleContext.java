package com.houjiang.payment.handle;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.service.PayStrategyHandleService;
import io.seata.core.exception.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @PROJECT_NAME: UniMall
 * @DESCRIPTION: 策略类, 指定走具体哪一个策略
 * @USER: Amos
 * @DATE: 2023/1/14 23:12
 * @author Amos
 */

public class PayStrategyHandleContext {

    /**
     * 注入PayStrategyHandleService服务，用于处理支付策略的业务逻辑。
     */
    @Autowired
    private PayStrategyHandleService payStrategyHandleService;

    /**
     * 设置PayStrategyHandleService服务。
     *
     * @param payStrategyHandleService 支付策略处理服务实例。
     */
    public void setPayStrategyHandleService(PayStrategyHandleService payStrategyHandleService) {
        this.payStrategyHandleService = payStrategyHandleService;
    }

    /**
     * 处理用户支付请求的方法。
     *
     * 本方法作为用户支付的入口，负责调用支付策略处理服务来执行实际的支付流程。
     *
     * @param param 一个Map对象，包含支付请求的各种参数。
     * @param request 用户的HTTP请求对象，可能包含额外的请求信息。
     * @param response 用户的HTTP响应对象，用于返回支付结果或相关信息。
     * @return 返回一个ResponseResult对象，包含支付结果或相关响应信息。
     * @throws TransactionException 如果支付过程中发生交易异常，则抛出此异常。
     */
    public ResponseResult getMethod(Map<Object, Object> param, HttpServletRequest request, HttpServletResponse response) throws TransactionException {
        // 调用支付策略处理服务，执行用户支付入口逻辑。
        ResponseResult result = payStrategyHandleService.userPayEntrance(param, request, response);
        return result;
    }

}
