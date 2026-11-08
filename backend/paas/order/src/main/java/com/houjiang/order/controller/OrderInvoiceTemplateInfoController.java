package com.houjiang.order.controller;


import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.util.ResponseResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName OrderInvoiceTemplateInfoController
 * @Description  发票模版管理表
 * @Author Amos
 * @Date 2023-07-04 13:32:32
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("order/invoiceTemplateInfo")
public class OrderInvoiceTemplateInfoController extends BaseController {

    /***
     * 查询发票信息列表
     * @param param
     * @return
     */
    @PostMapping("/list")
    public ResponseResult queryInvoiceInfoList(@RequestBody Map<String,Object> param) {
        ResponseResult result = orderInvoiceTemplateInfoService.queryInvoiceInfoList(param);
        return result;
    }

    /**
     * 插入发票模版信息
     * @param param
     * @return
     */
    @SensitiveCheck(field = {"invoiceHead"})
    @PostMapping("/insert")
    public ResponseResult insertInvoiceTmp(@RequestBody Map<String,Object> param) {
        ResponseResult result = orderInvoiceTemplateInfoService.insertInvoiceTmp(param);
        return result;
    }

    /**
     * 删除发票模版信息
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/delete")
    public ResponseResult deleteInvoiceTmp(@RequestBody Map<String, Object> param) {
        ResponseResult result = orderInvoiceTemplateInfoService.deleteInvoiceTmp(param);
        return result;
    }

    /**
     * 查询发票模版信息
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/query")
    public ResponseResult queryInvoiceTmp(@RequestBody Map<String, Object> param) {
        ResponseResult result = orderInvoiceTemplateInfoService.queryInvoiceTmp(param);
        return result;
    }

    /**
     * 更新发票模版信息
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @SensitiveCheck(field = {"invoiceHead"})
    @PostMapping("/update")
    public ResponseResult updateInvoiceTmp(@RequestBody Map<String, Object> param) {
        ResponseResult result = orderInvoiceTemplateInfoService.updateInvoiceTmp(param);
        return result;
    }

    /**
     * 新增发票
     *
     * @param param
     */
    @PostMapping("/feign/insertInvoiceTmpFeign")
    void insertInvoiceTmpFeign(@RequestBody Map<String, Object> param){
        orderInvoiceTemplateInfoService.insertInvoiceTmpFeign(param);
    }


    /**
     * 修改发票
     *
     * @param param
     */
    @PostMapping("/feign/updateInvoiceTmpFeign")
    void updateInvoiceTmpFeign(@RequestBody Map<String, Object> param){
        orderInvoiceTemplateInfoService.updateInvoiceTmpFeign(param);
    }

}
