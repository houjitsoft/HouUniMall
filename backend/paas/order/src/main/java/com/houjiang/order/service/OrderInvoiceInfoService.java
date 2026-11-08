package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.entity.OrderInvoiceInfoEntity;

import java.util.Map;

/**
 * @ClassName OrderInvoiceInfoService
 * @Description  订单发票表
 * @Author Amos
 * @Date 2023-06-25 10:33:24
 * @Version 1.0
 * @author Amos
 */
public interface OrderInvoiceInfoService extends IService<OrderInvoiceInfoEntity> {

    /**
     * 买家申请发票功能
     *
     * @param param
     * @return
     */
    ResponseResult applyInvoice(Map<Object, Object> param);

    /**
     * 买家批量申请发票
     * @param param
     * @return
     */
    ResponseResult applyInvoiceBatch(Map<Object, Object> param);

    /**
     * 买家中心-发票详情
     *
     * @param param
     * @return
     */
    ResponseResult queryInvoiceDetail(Map<Object, Object> param);

    /**
     * 查询平台订单发票列表
     */
    ResponseResult queryPlatformOrderInvoiceList(Map<String, Object> param);

    /**
     * 平台查看发票详情
     */
    ResponseResult queryPlatformInvoiceDetail(Map<Object, Object> param);

    /**
     * 审核发票
     * @param param
     * @return
     */
    ResponseResult auditInvoiceForSeller(Map<Object, Object> param);

    /**
     * 查询卖家中心查看发票详情
     * @param param
     * @return
     */
    ResponseResult querySellerInvoiceDetail(Map<Object,Object> param);

    /**
     * 查询卖家中心订单发票列表
     *
     * @param param
     * @return
     */
    ResponseResult querySellerOrderInvoiceList(Map<String, Object> param);
}
