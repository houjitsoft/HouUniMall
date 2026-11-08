package com.houjiang.thirdparty.utils;

import com.alibaba.fastjson.JSON;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20190711.SmsClient;
import com.tencentcloudapi.sms.v20190711.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20190711.models.SendSmsResponse;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.exception.GlobalExceptionEnum;
import com.houjiang.thirdparty.constants.TemplateIdConstant;
import com.houjiang.thirdparty.dto.TencentSmsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
public class TencentSmsUtils {
    private static final Logger LOG = LoggerFactory.getLogger(TencentSmsUtils.class);

    public static void testSms(TencentSmsDTO param) {
        try {
            // 检查配置是否完整
            if (TemplateIdConstant.SECRET_ID == null || TemplateIdConstant.SECRET_ID.trim().isEmpty()) {
                LOG.error("腾讯云短信配置错误：SECRET_ID为空");
                throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "短信配置错误：缺少SecretId");
            }
            if (TemplateIdConstant.SECRET_KEY == null || TemplateIdConstant.SECRET_KEY.trim().isEmpty()) {
                LOG.error("腾讯云短信配置错误：SECRET_KEY为空");
                throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "短信配置错误：缺少SecretKey");
            }
            if (TemplateIdConstant.SMS_SDK_APP_ID == null || TemplateIdConstant.SMS_SDK_APP_ID.trim().isEmpty()) {
                LOG.error("腾讯云短信配置错误：SMS_SDK_APP_ID（应用ID）为空");
                throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "短信配置错误：缺少应用ID");
            }
            if (TemplateIdConstant.SIGN == null || TemplateIdConstant.SIGN.trim().isEmpty()) {
                LOG.error("腾讯云短信配置错误：SIGN（签名）为空");
                throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "短信配置错误：缺少签名");
            }
            if (param.getTemplateID() == null || param.getTemplateID().trim().isEmpty()) {
                LOG.error("腾讯云短信配置错误：模板ID为空");
                throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "短信配置错误：缺少模板ID");
            }
            
            LOG.info("=== 腾讯云短信发送开始 ===");
            LOG.info("手机号: {}, 签名: {}, 模板ID: {}, 应用ID: {}", 
                param.getPhoneNumberSet() != null && !param.getPhoneNumberSet().isEmpty() ? param.getPhoneNumberSet().get(0) : "null",
                TemplateIdConstant.SIGN, 
                param.getTemplateID(),
                TemplateIdConstant.SMS_SDK_APP_ID);
            
            Credential cred = new Credential(TemplateIdConstant.SECRET_ID, TemplateIdConstant.SECRET_KEY);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            SmsClient client = new SmsClient(cred, "ap-shanghai", clientProfile);
            param.setSmsSdkAppid(TemplateIdConstant.SMS_SDK_APP_ID);
            param.setSign(TemplateIdConstant.SIGN);
            
            LOG.info("=== 腾讯云短信请求参数: {}", JSON.toJSONString(param));
            SendSmsRequest req = SendSmsRequest.fromJsonString(JSON.toJSONString(param), SendSmsRequest.class);

            SendSmsResponse resp = client.SendSms(req);

            LOG.info("=== 腾讯云短信响应: {}", SendSmsRequest.toJsonString(resp));

            if (resp.getSendStatusSet() == null || resp.getSendStatusSet().length == 0) {
                LOG.error("=== 腾讯云短信响应异常：SendStatusSet为空");
                throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "短信发送失败：响应数据异常");
            }
            
            String code = resp.getSendStatusSet()[0].getCode();
            String message = resp.getSendStatusSet()[0].getMessage();
            
            if (!"Ok".equalsIgnoreCase(code)) {
                String errorMsg = String.format("腾讯云短信发送失败: Code=%s, Message=%s", code, message);
                LOG.error("=== {}", errorMsg);
                throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), 
                    "短信发送失败: " + message + " (Code: " + code + ")");
            }
            
            LOG.info("=== 腾讯云短信发送成功 ===");
        } catch (TencentCloudSDKException e) {
            String errorMsg = String.format("腾讯云短信发送异常: ErrorCode=%s, ErrorMessage=%s, RequestId=%s", 
                e.getErrorCode(), e.getMessage(), e.getRequestId());
            LOG.error("=== {}", errorMsg, e);
            throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), 
                "短信发送失败: " + e.getMessage() + " (ErrorCode: " + e.getErrorCode() + ")");
        } catch (Exception e) {
            LOG.error("=== 腾讯云短信发送未知异常: {}", e.getMessage(), e);
            throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), 
                "短信发送失败: " + e.getMessage());
        }

    }

    /**
     *  unimall云短信通道
     *
     * @param mobile
     * @param content
     */
    public static void send(String mobile,String content,String interDialingCode){
        // -----------------------对接unimall云短信--------------------
        //用户名和密码需要unimall配置
        String  name = TemplateIdConstant.SECRET_ID;
        String  password = MD5.md5(TemplateIdConstant.SECRET_KEY);
        String url = "http://sms.unimall.cn";
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("password", password);
        params.put("mob", mobile);
        params.put("pre", interDialingCode);
        params.put("content", content);
        String result = HttpGet.get(url,params);
        System.out.println(" ------------------unimall云短信请求结果--------------------");
        System.out.println(result);
    }

    /**
     *  unimall云短信通道--通用模板
     *
     * @param mobile
     * @param text
     */
    public static void general(String mobile,String text,String interDialingCode){
        String  content  = "您的验证码是{1}，5分钟内有效。为了保护您的账号安全，验证短信请勿转发他人。如非本人操作，请忽略本短信。";
        content  = content.replace("{1}",text);
        send(mobile,content,interDialingCode);
    }
}
