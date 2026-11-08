package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PayCardInfoEntity;
import io.seata.core.exception.TransactionException;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author Amos
 */
public interface PayCardInfoService extends IService<PayCardInfoEntity> {

    /**
     * 充值卡充值
     *
     * @param map
     * @return
     */
    ResponseResult rechargeCard(Map<Object, Object> map) throws TransactionException;

    /**
     * @param paraMap
     * @param response
     * @return 已完成导出办卡 @throws IOException
     */

    /**
     * @param paraMap paramap
     * @param response 响应
     * @return 处理结果
     */
    ResponseResult execl(Map<String, Object> paraMap, HttpServletResponse response) throws IOException;
}
