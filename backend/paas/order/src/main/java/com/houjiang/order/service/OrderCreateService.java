package com.houjiang.order.service;

import com.houjiang.commons.dto.job.ExpireMarkIdUpdateDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.ExpireMarkIdUpdateParamVO;
import com.houjiang.order.dto.CreateProductVoucherOrdersDTO;
import io.seata.core.exception.TransactionException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @ClassName OrderCreateService
 * @Description 创建订单 计算价格
 * @Author Amos
 * @Date 2023-05-26 13:28:35
 * @Version 1.0
 * @author Amos
 */
public interface OrderCreateService {

    /**
     * 计算价格
     *
     * @param message
     * @return
     */
    ResponseResult calculatePrice(String message);

    /**
     * App下单
     * @param para
     * @param request
     * @return
     */
    ResponseResult createOrders(String para, HttpServletRequest request) throws TransactionException;

    /**
     * 兑换券商品下单
     * @param para
     * @return
     */
    ResponseResult productVoucherCreateOrders(CreateProductVoucherOrdersDTO para) throws TransactionException;


}
