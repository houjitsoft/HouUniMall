package com.houjiang.order.controller;


import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName OrderInvoiceAddressController
 * @Description  发票地址管理表
 * @Author Amos
 * @Date 2023-07-04 14:05:34
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("order/invoiceAddress")
public class OrderInvoiceAddressController extends BaseController {
    /**
     * 查询收票人地址列表
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody Map<String,Object> param) {
        ResponseResult list = orderInvoiceAddressService.list(param);
        return list;
    }

    /**
     * 插入发票地址模版信息
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @SensitiveCheck(field = {"userName","addressDetail"})
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Map<String,Object> param) {
        ResponseResult result = orderInvoiceAddressService.add(param);
        return result;
    }

    /**
     * 删除发票地址模版信息
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/delete")
    public ResponseResult deleteAddressTmp(@RequestBody Map<String, Object> param) {
        ResponseResult result = orderInvoiceAddressService.deleteAddressTmp(param);
        return result;
    }

    /**
     * 查询发票地址模版信息
     * @param param
     * @return
     */
    @PostMapping("/queryById")
    public ResponseResult queryById(@RequestBody Map<String,Object> param) {
        ResponseResult list = orderInvoiceAddressService.queryById(param);
        return list;
    }
}
