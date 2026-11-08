package com.houjiang.thirdparty.service;


import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface OfficialAccountService {

    /**
     * 使用POST方法通过指定路径发送通知
     *
     * @param param 包含通知相关参数的Map对象，通过请求体传递
     * @return 返回处理结果，具体类型由officialAccountService定义
     * <p>
     * 该方法的主要作用是调用officialAccountService的sendOfficialAccountNotice方法
     * 以发送官方账号通知它不直接处理请求参数，而是将参数转发给服务层方法
     */
    ResponseResult sendOfficialAccountNotice(Map<String, Object> param);
}
