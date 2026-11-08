package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.entity.OrderInvoiceAddressEntity;

import java.util.Map;

/**
 * @ClassName OrderInvoiceAddressService
 * @Description  发票地址管理表
 * @Author Amos
 * @Date 2023-07-04 14:05:35
 * @Version 1.0
 * @author Amos
 */
public interface OrderInvoiceAddressService extends IService<OrderInvoiceAddressEntity> {
    /**
     * 查询收票人地址列表
     * @param param
     * @return
     */
    ResponseResult list(Map<String, Object> param);

    /**
     * 插入发票地址模版信息
     * @param param
     * @return
     */
    ResponseResult add(Map<String, Object> param);

    /**
     * 删除发票地址模版信息
     *
     * @param param
     * @return
     */
    ResponseResult deleteAddressTmp(Map<String, Object> param);

    /**
     * 查询发票地址模版信息
     * @param param
     * @return
     */
    ResponseResult queryById(Map<String, Object> param);

}
