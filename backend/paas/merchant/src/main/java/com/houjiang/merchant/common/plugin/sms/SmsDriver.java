package com.houjiang.merchant.common.plugin.sms;

import com.alibaba.fastjson2.JSON;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.plugin.sms.engine.AliSms;
import com.houjiang.merchant.common.plugin.sms.engine.TencentSms;
import com.houjiang.merchant.common.util.ConfigUtils;

import java.util.Map;

public class SmsDriver {

    /** 手机号码 */
    private String mobile;
    /** 短信模板 */
    private String templateCode;
    /** 短信参数 */
    private Map<String, String> templateParam;
    /** 短信引擎 */
    private final String engine;
    /** 短信配置 */
    private final Map<String, String> config;

    /**
     * 构造方法
     */
    public SmsDriver() {
        this.engine = ConfigUtils.get("sms", "engine", "").toLowerCase();
        this.config = ConfigUtils.getMap("sms", this.engine);
    }

    /**
     * 设置手机号
     *
     * @author Amos
     * @param mobile 手机号
     * @return SmsDriver
     */
    public SmsDriver setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    /**
     * 设置模板编号
     *
     * @author Amos
     * @param code 短信编码
     * @return SmsDriver
     */
    public SmsDriver setTemplateCode(String code) {
        this.templateCode = code;
        return this;
    }

    /**
     * 设置模板参数
     *
     * @author Amos
     * @param param 参数
     * @return SmsDriver
     */
    public SmsDriver setTemplateParam(Map<String, String> param) {
        this.templateParam = param;
        return this;
    }

    /**
     * 发送短信
     *
     * @author Amos
     */
    public void sendSms() {
        String templateParam = JSON.toJSONString(this.templateParam);
        Integer sendResult = 0;
        String results = "";
        switch (this.engine) {
            case "ali":
                AliSms aliSms = new AliSms(this.config);
                results = aliSms.setMobile(this.mobile)
                    .setTemplateId(this.templateCode)
                    .setTemplateParams(templateParam)
                    .send();

                sendResult = aliSms.getSendResult();
                break;
            case "tencent":
                TencentSms tencentSms = new TencentSms(this.config);
                results = tencentSms.setMobile(this.mobile)
                        .setTemplateId(this.templateCode)
                        .setTemplateParams(this.templateParam)
                        .send();
                sendResult = tencentSms.getSendResult();
                break;
            default:
                throw new OperateException("短信类型错误");
        }

        if (sendResult == 2) {
            throw new OperateException(results);
        }
    }

}
