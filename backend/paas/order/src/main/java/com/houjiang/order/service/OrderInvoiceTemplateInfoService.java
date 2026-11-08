package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.entity.OrderInvoiceTemplateInfoEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName OrderInvoiceTemplateInfoService
 * @Description  发票模版管理表
 * @Author Amos
 * @Date 2023-07-04 13:32:33
 * @Version 1.0
 * @author Amos
 */
public interface OrderInvoiceTemplateInfoService extends IService<OrderInvoiceTemplateInfoEntity> {
    /**
     * 查询发票信息列表
     * @param param
     * @return
     */
    ResponseResult queryInvoiceInfoList(Map<String,Object> param);

    /**
     * 插入发票模版信息
     * @param param
     * @return
     */
    ResponseResult insertInvoiceTmp(Map<String, Object> param);

    /**
     * 删除发票模版信息
     *
     * @param param
     * @return
     */
    ResponseResult deleteInvoiceTmp(Map<String, Object> param);

    /**
     * 查询发票模版信息
     *
     * @param param
     * @return
     */
    ResponseResult queryInvoiceTmp(Map<String, Object> param);

    /**
     * 更新发票模版信息
     *
     * @param param
     * @return
     */
    ResponseResult updateInvoiceTmp(Map<String, Object> param);


    /**
     * 新增发票
     *
     * @param param
     */
    void insertInvoiceTmpFeign(Map<String, Object> param);


    /**
     * 修改发票
     *
     * @param param
     */
    void updateInvoiceTmpFeign(Map<String, Object> param);
}
