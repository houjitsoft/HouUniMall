package com.houjiang.merchant.service.impl;

import com.houjiang.merchant.service.ISettingProtocolService;
import com.houjiang.merchant.validate.setting.SettingAgreementValidate;
import com.houjiang.merchant.vo.setting.SettingAgreementVo;
import com.houjiang.merchant.common.util.ConfigUtils;
import org.springframework.stereotype.Service;

/**
 * 政策接口服务实现类
 */
@Service
public class SettingProtocolServiceImpl implements ISettingProtocolService {

    /**
     * 获取政策协议信息
     *
     * @author Amos
     * @return SettingProtocolDetailVo
     */
    @Override
    public SettingAgreementVo getAgreement() {
        SettingAgreementVo vo = new SettingAgreementVo();
        vo.setServiceTitle(ConfigUtils.get("agreement", "service_title", ""));
        vo.setServiceContent(ConfigUtils.get("agreement", "service_content", ""));
        vo.setPrivacyTitle(ConfigUtils.get("agreement", "privacy_title", ""));
        vo.setPrivacyContent(ConfigUtils.get("agreement", "privacy_content", ""));
        return vo;
    }

    /**
     * 保存政策协议信息
     *
     * @author Amos
     * @param protocolValidate 参数
     */
    @Override
    public void setAgreement(SettingAgreementValidate protocolValidate) {
        ConfigUtils.set("agreement","service_title", protocolValidate.getServiceTitle());
        ConfigUtils.set("agreement","service_content", protocolValidate.getServiceContent());
        ConfigUtils.set("agreement","privacy_title", protocolValidate.getPrivacyTitle());
        ConfigUtils.set("agreement","privacy_content", protocolValidate.getPrivacyContent());
    }

}
