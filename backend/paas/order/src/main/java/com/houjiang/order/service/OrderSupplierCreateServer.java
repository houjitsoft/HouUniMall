package com.houjiang.order.service;

import com.houjiang.commons.util.ResponseResult;
import io.seata.core.exception.TransactionException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

/**
 * @ClassName OrderSupplierCreateController
 * @Description 供应商 创建订单 计算价格
 * @Author Amos
 * @Date 2023/7/14 15:23
 * @Version 1.0
 * @author Amos
 */
public interface OrderSupplierCreateServer {
    /**
     * 计算价格
     * @param message
     * @return
     */
    ResponseResult calculatePrice(String message);

    /**
     * 创建供应商订单
     * @param message
     * @param request
     * @return
     */
    ResponseResult createOrders(String message, HttpServletRequest request);

    /**
     * 计算金额
     *
     * @param message
     * @return
     */
    ResponseResult SupplierCalculatePrice(@RequestBody String message);

    /**
     * 选品中心下单提交订单
     *
     * @param params
     * @param request
     * @return
     */
    ResponseResult supplierCreateOrders(String params, HttpServletRequest request) throws IOException, TransactionException;

}
