package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.setting.SettingAgreementValidate;
import com.houjiang.merchant.vo.setting.SettingAgreementVo;

/**
 * 政策协议服务接口类
 */
public interface ISettingProtocolService {

    /**
     * 获取政策协议信息
     *
     * @author Amos
     * @return SettingProtocolDetailVo
     */
    SettingAgreementVo getAgreement();

    /**
     * 保存政策协议信息
     *
     * @author Amos
     * @param protocolValidate 参数
     */
    void setAgreement(SettingAgreementValidate protocolValidate);

}
