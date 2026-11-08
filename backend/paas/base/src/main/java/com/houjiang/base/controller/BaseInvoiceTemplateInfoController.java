package com.houjiang.base.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName BaseInvoiceTemplateInfoController
 * @Description
 * @Author Amos
 * @Date 2023/6/28 13:55
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/invoiceTemplateInfo")
public class BaseInvoiceTemplateInfoController extends BaseController{

    /**
     * 查询发票信息列表
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/invoice/info/list")
    public ResponseResult queryInvoiceInfoList(@RequestBody Map<String, Object> param) {

        ResponseResult result = baseInvoiceTemplateInfoService.queryInvoiceInfoList(param);

        return result;
    }


    /**
     * 新建发票模版信息
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/insert/invoiceTmp")
    public ResponseResult insertInvoiceTmp(@RequestBody Map<String, Object> param) {
        ResponseResult result = baseInvoiceTemplateInfoService.insertInvoiceTmp(param);

        return result;

    }

    /**
     * 查询发票模版信息
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/query/invoiceTmp")
    public ResponseResult queryInvoiceTmp(@RequestBody Map<String, Object> param) {
        ResponseResult result = baseInvoiceTemplateInfoService.queryInvoiceTmp(param);

        return result;
    }

    /**
     * 更新发票模版信息
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/update/invoiceTmp")
    public ResponseResult updateInvoiceTmp(@RequestBody Map<String, Object> param) {
        ResponseResult result = baseInvoiceTemplateInfoService.updateInvoiceTmp(param);

        return result;
    }


    /**
     * 删除发票模版信息
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/delete/invoiceTmp")
    public ResponseResult deleteInvoiceTmp(@RequestBody Map<String, Object> param) {
        ResponseResult result = baseInvoiceTemplateInfoService.deleteInvoiceTmp(param);

        return result;
    }
}
