package com.houjiang.merchant.common.plugin.sms.engine;

import com.alibaba.fastjson2.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;

import java.util.Map;

/**
 * 阿里云短信
 */
public class AliSms {

    /** 发送结果 */
    private Integer sendResult;
    /** 手机号码 */
    private String mobile;
    /** 短信模板 */
    private String templateId;
    /** 短信参数 */
    private String templateParams;
    /** 短信配置 */
    private final Map<String, String> config;

    /**
     * 构造方法
     *
     * @author Amos
     * @param config 短信配置
     */
    public AliSms(Map<String, String> config) {
        this.config = config;
    }

    /**
     * 设置手机号
     *
     * @author Amos
     * @param mobile 手机号码
     * @return AliSms
     */
    public AliSms setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    /**
     * 设置模板id
     *
     * @author Amos
     * @param templateId 模板id
     * @return AliSms
     */
    public AliSms setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    /**
     * 设置模板参数
     *
     * @author Amos
     * @param templateParams 模板参数
     * @return AliSms
     */
    public AliSms setTemplateParams(String templateParams) {
        this.templateParams = templateParams;
        return this;
    }

    /**
     * 获取发送结果
     *
     * @author Amos
     * @return Integer [1=成功, 2=失败]
     */
    public Integer getSendResult() {
        return this.sendResult;
    }

    /**
     * 发送短信
     *
     * @author Amos
     * @return String
     */
    public String send() {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", this.config.get("app_key"), this.config.get("secret_key"));
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("PhoneNumbers", this.mobile);
        request.putQueryParameter("SignName", this.config.get("sign"));
        request.putQueryParameter("TemplateCode", this.templateId);
        request.putQueryParameter("TemplateParam", this.templateParams);
        try {
            CommonResponse response = client.getCommonResponse(request);
            JSONObject res = JSONObject.parseObject(response.getData());

            if (!res.get("Code").equals("OK") || !res.get("Message").equals("OK")) {
                this.sendResult = 2;
                return res.get("Message").toString();
            }

            this.sendResult = 1;
            return response.getData();
        } catch (Exception e) {
            this.sendResult = 2;
            return e.getMessage();
        }
    }

}
