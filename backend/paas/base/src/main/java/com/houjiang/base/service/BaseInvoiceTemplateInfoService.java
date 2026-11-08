package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.entity.BaseInvoiceTemplateInfoEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface BaseInvoiceTemplateInfoService extends IService<BaseInvoiceTemplateInfoEntity> {

    /**
     * 查询发票信息列表
     *
     * @param param
     * @return
     */
    ResponseResult queryInvoiceInfoList(Map<String, Object> param);

    /**
     * 新建发票模版信息
     *
     * @param param
     * @return
     */
    ResponseResult insertInvoiceTmp(Map<String, Object> param);

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
     * 删除发票模版信息
     *
     * @param param
     * @return
     */
    ResponseResult deleteInvoiceTmp(Map<String, Object> param);
}
