package com.houjiang.order.controller;


import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName OrderInvoiceInfoController
 * @Description  订单发票表
 * @Author Amos
 * @Date 2023-06-25 10:33:23
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("orderInvoiceInfo")
public class OrderInvoiceInfoController extends BaseController{


    /**
     * 买家申请发票功能
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/apply/invoice")
    public ResponseResult applyInvoice(@RequestBody Map<Object, Object> param) {
        ResponseResult result = orderInvoiceInfoService.applyInvoice(param);
        return result;

    }

    /**
     * 买家批量申请发票
     */
    @PostMapping("apply/invoice/batch")
    public ResponseResult applyInvoiceBatch(@RequestBody Map<Object,Object> param){
        ResponseResult result = orderInvoiceInfoService.applyInvoiceBatch(param);
        return result;
    }

    /**
     * 买家中心-发票详情
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/invoice/detail")
    public ResponseResult queryInvoiceDetail(@RequestBody Map<Object, Object> param) {
        ResponseResult result = orderInvoiceInfoService.queryInvoiceDetail(param);
        return result;
    }

    /**
     * 查询平台订单发票列表
     *
     * @param param Map<String,Object>
     * @return ResultResp
     */
    @PostMapping("/platform/query/list")
    public ResponseResult queryPlatformOrderInvoiceList(@RequestBody Map<String,Object> param) {
        ResponseResult result = orderInvoiceInfoService.queryPlatformOrderInvoiceList(param);
        return result;
    }

    /**
     * 平台查看发票详情
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/platform/query/detail")
    public ResponseResult queryPlatformInvoiceDetail(@RequestBody Map<Object,Object> param) {
        ResponseResult result = orderInvoiceInfoService.queryPlatformInvoiceDetail(param);
        return result;
    }

    /**
     * 查询卖家中心审核发票
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/seller/audit/invoice")
    public ResponseResult auditInvoiceForSeller(@RequestBody Map<Object,Object> param) {
        ResponseResult result = orderInvoiceInfoService.auditInvoiceForSeller(param);
        return result;
    }

    /**
     * 查询卖家中心查看发票详情
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/seller/query/detail")
    public ResponseResult querySellerInvoiceDetail(@RequestBody Map<Object,Object> param) {
        ResponseResult result = orderInvoiceInfoService.querySellerInvoiceDetail(param);
        return result;
    }

    /**
     * 查询卖家中心订单发票列表
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/seller/query/list")
    public ResponseResult querySellerOrderInvoiceList(@RequestBody Map<String, Object> param) {
        return orderInvoiceInfoService.querySellerOrderInvoiceList(param);
    }

}
