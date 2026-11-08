package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.mch.MchRegRequest;
import com.houjiang.payment.dto.mch.MchRegResponse;

/**
 * 商户服务接口
 *
 * 商户进件：/mch/reg
 * @author Amos
 */
public interface MchService {

    /**
     * 商户进件
     * 用于服务商进件通企付商户
     *
     * @param request 商户进件请求
     * @return 商户进件响应
     */
    MchRegResponse mchReg(MchRegRequest request);
}
