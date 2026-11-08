package com.houjiang.thirdparty.service.message.sender;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 短信发送器 - 阿里云短信
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Slf4j
@Component
public class SmsSender implements MessageSender {
    
    @Override
    public String getChannelCode() {
        return "SMS";
    }
    
    @Override
    public SendResult send(String receiver, String title, String content, 
                           Map<String, Object> config, Map<String, Object> params) {
        try {
            // 从配置中获取阿里云短信参数
            String accessKeyId = getConfigValue(config, "accessKeyId", "");
            String accessKeySecret = getConfigValue(config, "accessKeySecret", "");
            String signName = getConfigValue(config, "signName", "");
            String templateCode = getConfigValue(config, "templateCode", "");
            
            if (accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
                return SendResult.fail("CONFIG_ERROR", "短信配置不完整，缺少AccessKey");
            }
            
            if (signName.isEmpty()) {
                return SendResult.fail("CONFIG_ERROR", "短信配置不完整，缺少签名");
            }
            
            // 如果params中有模板编码，优先使用
            if (params != null && params.get("templateCode") != null) {
                templateCode = params.get("templateCode").toString();
            }
            
            if (templateCode.isEmpty()) {
                return SendResult.fail("CONFIG_ERROR", "短信配置不完整，缺少模板编码");
            }
            
            // 获取模板参数
            String templateParam = content;
            if (params != null && params.get("templateParam") != null) {
                templateParam = params.get("templateParam").toString();
            }
            
            // 创建阿里云客户端
            DefaultProfile profile = DefaultProfile.getProfile(
                "cn-hangzhou", accessKeyId, accessKeySecret);
            IAcsClient client = new DefaultAcsClient(profile);
            
            // 创建发送请求
            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(receiver);
            request.setSignName(signName);
            request.setTemplateCode(templateCode);
            request.setTemplateParam(templateParam);
            
            // 发送短信
            SendSmsResponse response = client.getAcsResponse(request);
            
            if ("OK".equals(response.getCode())) {
                log.info("短信发送成功: phone={}, bizId={}", receiver, response.getBizId());
                return SendResult.success(response.getBizId());
            } else {
                log.error("短信发送失败: phone={}, code={}, message={}", 
                    receiver, response.getCode(), response.getMessage());
                return SendResult.fail(response.getCode(), response.getMessage());
            }
            
        } catch (Exception e) {
            log.error("短信发送异常: phone={}, error={}", receiver, e.getMessage(), e);
            return SendResult.fail("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validateConfig(Map<String, Object> config) {
        if (config == null) return false;
        String accessKeyId = getConfigValue(config, "accessKeyId", "");
        String accessKeySecret = getConfigValue(config, "accessKeySecret", "");
        String signName = getConfigValue(config, "signName", "");
        return !accessKeyId.isEmpty() && !accessKeySecret.isEmpty() && !signName.isEmpty();
    }
    
    private String getConfigValue(Map<String, Object> config, String key, String defaultValue) {
        if (config == null) return defaultValue;
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
