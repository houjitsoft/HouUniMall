package com.houjiang.thirdparty.service.message.sender;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * 邮件发送器
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Slf4j
@Component
public class EmailSender implements MessageSender {
    
    @Override
    public String getChannelCode() {
        return "EMAIL";
    }
    
    @Override
    public SendResult send(String receiver, String title, String content, 
                           Map<String, Object> config, Map<String, Object> params) {
        try {
            // 从配置中获取SMTP参数
            String host = getConfigValue(config, "host", "smtp.qq.com");
            int port = Integer.parseInt(getConfigValue(config, "port", "465"));
            String username = getConfigValue(config, "username", "");
            String password = getConfigValue(config, "password", "");
            String from = getConfigValue(config, "from", username);
            String fromName = getConfigValue(config, "fromName", "");
            String encryption = getConfigValue(config, "encryption", "");
            boolean ssl = "ssl".equalsIgnoreCase(encryption)
                    || Boolean.parseBoolean(getConfigValue(config, "ssl", "true"));
            
            if (username.isEmpty() || password.isEmpty()) {
                return SendResult.fail("CONFIG_ERROR", "邮件配置不完整，缺少用户名或密码");
            }
            
            // 与 MailUtil 保持一致，避免企业邮 SSL 握手/认证差异
            System.setProperty("java.net.preferIPv4Stack", "true");
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", String.valueOf(port));
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.timeout", "200000");
            
            if (ssl) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.port", String.valueOf(port));
                props.put("mail.smtp.socketFactory.fallback", "false");
            } else if (!"none".equalsIgnoreCase(encryption)) {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
            }
            
            // 创建会话
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            // 创建邮件消息
            MimeMessage message = new MimeMessage(session);
            if (StringUtils.hasText(fromName)) {
                message.setFrom(new InternetAddress(from, fromName, "UTF-8"));
            } else {
                message.setFrom(new InternetAddress(from));
            }
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
            message.setSubject(title, "UTF-8");
            
            // 判断是否为HTML内容
            boolean isHtml = params != null && Boolean.TRUE.equals(params.get("isHtml"));
            if (isHtml) {
                message.setContent(content, "text/html;charset=UTF-8");
            } else {
                message.setText(content, "UTF-8");
            }
            
            // 发送邮件
            Transport.send(message);
            
            String messageId = UUID.randomUUID().toString();
            log.info("邮件发送成功: to={}, subject={}, messageId={}", receiver, title, messageId);
            return SendResult.success(messageId);
            
        } catch (MessagingException e) {
            log.error("邮件发送失败: to={}, error={}", receiver, e.getMessage(), e);
            return SendResult.fail("SEND_ERROR", "邮件发送失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("邮件发送异常: to={}, error={}", receiver, e.getMessage(), e);
            return SendResult.fail("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validateConfig(Map<String, Object> config) {
        if (config == null) return false;
        String username = getConfigValue(config, "username", "");
        String password = getConfigValue(config, "password", "");
        return !username.isEmpty() && !password.isEmpty();
    }
    
    private String getConfigValue(Map<String, Object> config, String key, String defaultValue) {
        if (config == null) return defaultValue;
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
