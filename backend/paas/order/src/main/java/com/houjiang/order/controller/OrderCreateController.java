package com.houjiang.order.controller;

import com.houjiang.commons.annnotation.RepeatClick;
import com.houjiang.commons.dto.job.ExpireMarkIdUpdateDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.ExpireMarkIdUpdateParamVO;
import com.houjiang.order.dto.CreateProductVoucherOrdersDTO;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @ClassName OrderCreateController
 * @Description 创建订单 计算价格
 * @Author Amos
 * @Date 2023-05-26 13:28:35
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("order")
public class OrderCreateController extends BaseController {



    /**
     * 计算价格
     *
     * @param message
     * @return
     */
    @RequestMapping("/calculatePrice")
    public ResponseResult calculatePrice(@RequestBody String message) {
        ResponseResult result = orderCreateService.calculatePrice(message);
        return result;
    }


    /**
     * App下单
     *
     * @param para
     * @return
     */
    @GlobalTransactional
    @RequestMapping("/appCreateOrders")
    public ResponseResult AppCreateOrders(@RequestBody String para, HttpServletRequest request)
            throws TransactionException {
        ResponseResult result = orderCreateService.createOrders(para, request);
        return result;

    }
    /**
     * 兑换券商品下单
     *
     * @param para
     * @return
     */
    @RepeatClick
    @GlobalTransactional
    @PostMapping("/productVoucherCreateOrders")
    public ResponseResult productVoucherCreateOrders(@RequestBody CreateProductVoucherOrdersDTO para) throws TransactionException {
        ResponseResult result = orderCreateService.productVoucherCreateOrders(para);
        return result;

    }

}
