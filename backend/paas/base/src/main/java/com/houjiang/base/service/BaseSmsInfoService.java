package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseSmsConfigDTO;
import com.houjiang.base.entity.BaseSmsInfoEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface BaseSmsInfoService extends IService<BaseSmsInfoEntity> {


    /**
     * 初始化SMS配置信息
     *
     * @param
     * @return
     */
    ResponseResult selectSMSInfo2();


    /**
     * update sms配置 存在就更新不存在就添加
     *
     * @param baseSmsConfigReq
     * @return
     */
    ResponseResult updateSmsConfig(BaseSmsConfigDTO baseSmsConfigReq);

    /**
     * 更新SMS配置信息
     * @param param
     * @return
     */
    ResponseResult updateSMS(BaseSmsConfigDTO baseSmsConfigReq);

}
