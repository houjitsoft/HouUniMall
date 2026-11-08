package com.houjiang.order.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.houjiang.commons.annnotation.RepeatClick;
import com.houjiang.commons.util.ResponseResult;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @ClassName OrderSupplierCreateController
 * @Description 供应商 创建订单 计算价格
 * @Author Amos
 * @Date 2023/7/14 15:23
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("supply")
public class OrderSupplierCreateController extends BaseController {


    /**
     * 供应商 计算价格
     * @param message
     * @return
     */
    @RequestMapping("calculatePrice")
    public ResponseResult calculatePrice(@RequestBody String message){
        ResponseResult result = orderSupplierCreateServer.calculatePrice(message);
        return result;
    }

    /**
     * 供应商 下单
     * @param message
     * @param request
     * @return
     */
    @RequestMapping("createOrders")
    public ResponseResult createOrders(@RequestBody String message, HttpServletRequest request){
        ResponseResult orders = orderSupplierCreateServer.createOrders(message, request);
        return orders;
    }

    /**
     * 选品中心下单-计算金额
     *
     * @param message
     * @return
     */
    @PostMapping("/SupplierCalculatePrice")
    public ResponseResult SupplierCalculatePrice(@RequestBody String message) {
        ResponseResult rr = orderSupplierCreateServer.SupplierCalculatePrice(message);
        return rr;
    }

    /**
     * 选品中心下单接口。该接口用于处理App端的下单请求，通过接收订单相关信息，创建订单。
     *
     * @param params 包含订单信息的JSON字符串。该参数详细包含了创建订单所需的所有信息，例如商品信息、收货地址等。
     * @param request HttpServletRequest对象，用于获取请求相关信息，例如IP地址等。该参数在当前方法中未直接使用，但可能在底层服务中用于实现某些功能。
     * @return 返回订单创建结果的ResponseResult对象。该对象包含了订单创建是否成功的信息以及可能的错误码。
     * @throws IOException 如果处理过程中发生IO异常，则抛出该异常。
     * @throws TransactionException 如果全局事务处理过程中发生异常，则抛出该异常。
     * @throws JsonMappingException 如果JSON映射过程中发生错误，则抛出该异常。
     * @throws JsonParseException 如果JSON解析过程中发生错误，则抛出该异常。
     */
    @RepeatClick
    @PostMapping("supplierCreateOrders")
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult supplierCreateOrders(@RequestBody String params, HttpServletRequest request) throws IOException, TransactionException {
        // 调用shopBaseService中的createOrders方法处理订单创建逻辑
        ResponseResult rr = orderSupplierCreateServer.supplierCreateOrders(params, request);
        return rr;
    }

}
