package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseSmsConfigDTO;
import com.houjiang.base.entity.SmsInfoEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @ClassName SmsInfoService
 * @Description
 * @Author Amos
 * @Date 2023-05-22 16:51:47
 * @Version 1.0
 * @author Amos
 */
public interface SmsInfoService extends IService<SmsInfoEntity> {


    /**
     * 初始化SMS配置信息 加密返回
     * @return
     */
    ResponseResult selectSMSInfoEncryption() throws IllegalAccessException;

    /**
     * update sms配置 存在就更新不存在就添加
     *
     * @param baseSmsConfigReq
     * @return
     */
    ResponseResult updateSmsConfig(BaseSmsConfigDTO baseSmsConfigReq);

    /**
     * 查询开启的sms配置
     * @return 开启的sms配置
     */
    String querySmsStates();

    /**
     * 根据短信类型查询sms配置
     * @return 开启的sms配置
     */
    ResponseResult<Map<String, Object>> selectSMSInfoByType(String type);

}
