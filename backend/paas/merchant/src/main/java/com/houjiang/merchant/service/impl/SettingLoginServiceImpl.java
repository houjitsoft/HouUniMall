package com.houjiang.merchant.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.service.ISettingLoginService;
import com.houjiang.merchant.validate.setting.SettingLoginValidate;
import com.houjiang.merchant.vo.setting.SettingLoginVo;
import com.houjiang.merchant.common.util.ConfigUtils;
import org.springframework.stereotype.Service;
import com.houjiang.merchant.common.validation.Assert;

import java.util.Map;

/**
 * 登录设置服务接口类
 */
@Service
public class SettingLoginServiceImpl implements ISettingLoginService {

    /**
     * 登录设置详情
     *
     * @author Amos
     * @return SettingLoginVo
     */
    @Override
    public SettingLoginVo getRegisterConfig() {
        Map<String, String> config = ConfigUtils.get("login");

        SettingLoginVo vo =  new SettingLoginVo();
        vo.setLoginWay(JSONArray.parse(config.getOrDefault("login_way", "")));
        vo.setCoerceMobile(Integer.parseInt(config.getOrDefault("coerce_mobile", "0")));
        vo.setLoginAgreement(Integer.parseInt(config.getOrDefault("login_agreement", "0")));
        vo.setThirdAuth(Integer.parseInt(config.getOrDefault("third_auth", "0")));
        vo.setWechatAuth(Integer.parseInt(config.getOrDefault("wechat_auth", "0")));
        vo.setQqAuth(Integer.parseInt(config.getOrDefault("qq_auth", "0")));
        return vo;
    }

    /**
     * 登录设置保存
     *
     * @author Amos
     * @param loginValidate 参数
     */
    @Override
    public void setRegisterConfig(SettingLoginValidate loginValidate) {

        JSONArray loginway = loginValidate.getLoginWay();

        Assert.isTrue(loginway != null && loginway.size() > 0, "系统通用登录方式，至少选择一项");


        ConfigUtils.set("login", "login_way", loginValidate.getLoginWay().toJSONString());
        ConfigUtils.set("login", "coerce_mobile", String.valueOf(loginValidate.getCoerceMobile()));
        ConfigUtils.set("login", "login_agreement", String.valueOf(loginValidate.getLoginAgreement()));
        ConfigUtils.set("login", "third_auth", String.valueOf(loginValidate.getThirdAuth()));
        ConfigUtils.set("login", "wechat_auth", String.valueOf(loginValidate.getWechatAuth()));
        ConfigUtils.set("login", "qq_auth", String.valueOf(loginValidate.getQqAuth()));
    }

}
