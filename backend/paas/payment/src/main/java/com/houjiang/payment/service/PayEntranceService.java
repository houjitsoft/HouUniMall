package com.houjiang.payment.service;

import com.houjiang.commons.util.ResponseResult;
import io.seata.core.exception.TransactionException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/4/27 17:36
 */
public interface PayEntranceService {

    /**
     * pc 端支付统一入口
     *
     * @param payInfo
     * @param request
     * @param response
     * @return
     */
    ResponseResult payMentUnifyInlet(Map<Object, Object> payInfo, HttpServletRequest request,
                                     HttpServletResponse response) throws TransactionException;
}
