package com.houjiang.thirdparty.utils;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.NavSortLogoDTO;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.vo.base.BaseMessageTemplateVO;
import com.houjiang.thirdparty.constants.TemplateIdConstant;
import com.houjiang.thirdparty.feign.BaseServiceClient;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Amos
 */
@Component
public class MailUtil {

    private static MailUtil mailUtil;
    private static String HOST = "smtp.exmail.qq.com";
    private static String PORT = "465";
    private static String USERNAME = "";
    private static String PASSWORD = "";

    /**
     * 最近一次发送失败的真实原因。
     * 调用方（验证码发码接口）据此返回可读提示，不再把所有失败都报成「格式不正确」。
     */
    private static volatile String lastError = "";

    @Autowired(required = false)
    private BaseServiceClient baseServiceClient;

    public static String getLastError() {
        return lastError == null ? "" : lastError;
    }

    /**
     * 按发件邮箱域名推导 SMTP 服务器。
     * 原来固定 smtp.exmail.qq.com，遇到个人 QQ 邮箱（@qq.com）就会认证失败。
     */
    private static String resolveHost(String username) {
        String account = username == null ? "" : username.trim().toLowerCase();
        int at = account.lastIndexOf('@');
        String domain = at >= 0 ? account.substring(at + 1) : "";
        if (domain.isEmpty()) {
            return HOST;
        }
        if (domain.equals("qq.com") || domain.equals("vip.qq.com") || domain.equals("foxmail.com")) {
            return "smtp.qq.com";
        }
        if (domain.endsWith("163.com")) {
            return "smtp.163.com";
        }
        if (domain.endsWith("126.com")) {
            return "smtp.126.com";
        }
        if (domain.contains("exmail.qq.com")) {
            return "smtp.exmail.qq.com";
        }
        // 企业自有域名邮箱：仍按腾讯企业邮默认入口
        return HOST;
    }

    @PostConstruct
    public void init() {
        mailUtil = this;
        mailUtil.baseServiceClient = this.baseServiceClient;
        // 初使化时将已静态化的testService实例化
    }
    private static void setEmailTemplate() {
        // 查询电子邮件
        NavSortLogoDTO gdbNavSortLogoDTO = new NavSortLogoDTO();
        gdbNavSortLogoDTO.setConfigType(CommonType.configKey.SETTING.getDesc());
        ResponseResult resp = mailUtil.baseServiceClient.quertNavLogo(gdbNavSortLogoDTO);
        // 配置服务不可用 / 无数据时保持既有静态配置，不再让 NPE 冒泡成 20010
        if (resp == null || resp.getData() == null) {
            return;
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) resp.getData();
        for (Map<String, Object> stringObjectMap : list) {
            if (stringObjectMap == null || stringObjectMap.get("configValue") == null) {
                continue;
            }
            if(CommonType.configKey.SETTING_EMAIL.getDesc().equals(stringObjectMap.get("configKey"))){
                USERNAME = stringObjectMap.get("configValue").toString();
                continue;
            }
            if(CommonType.configKey.SETTING_EMAIL_CODE.getDesc().equals(stringObjectMap.get("configKey"))){
                PASSWORD = stringObjectMap.get("configValue").toString();
            }
        }
    }
    private static String setContentEmail(String name,String content, List<String> list) {
        // 遍历列表中的每个元素，寻找并替换占位符
        for (int i = 1; i <= list.size(); i++) {
            // 检查内容中是否包含当前索引的占位符
            if (content.contains("{" + i + "}")) {
                // 使用列表中对应索引的元素替换占位符
                content = content.replace("{" + i + "}", "<strong>"+list.get(i - 1)+"</strong>");
            }
        }
        content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "\t<head>\n" +
                "\t\t<meta charset=\"utf-8\">\n" +
                "\t\t<meta name=\"viewport\" content=\"width=device-width,initial-scale=1,viewport-fit=cover,maximum-scale=1,minimum-scale=1\">\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t<h3 class=\"title\"><b>**"+name+"**</b></h3>\n" +
                "\t\t<p>"+content+"</p>\n" +
                "\t\t<p class=\"bottom\">感谢您对商城的信任，祝您购物愉快！</p>\n" +
                "\t</body>\n" +
                "\t<style>\n" +
                "\t\tbody{\n" +
                "\t\t\tpadding:0 5px;\n" +
                "\t\t}\n" +
                "\t\t.tc{\n" +
                "\t\t\ttext-align: center;\n" +
                "\t\t}\n" +
                "\t\t.logo{\n" +
                "\t\t\tmargin-top:20px;\n" +
                "\t\t\twidth:40px;\n" +
                "\t\t}\n" +
                "\t\t.title{\n" +
                "\t\t\tmargin-bottom:30px;\n" +
                "\t\t}\n" +
                "\t\t.title b{\n" +
                "\t\t\tfont-weight:normal;\n" +
                "\t\t}\n" +
                "\t\tp{\n" +
                "\t\t\tfont-size:14px;\n" +
                "\t\t\tline-height:24px;\n" +
                "\t\t\tcolor:#333;\n" +
                "\t\t}\n" +
                "\t\tstrong{\n" +
                "\t\t\tfont-size:22px;\n" +
                "\t\t\tcolor:#000;\n" +
                "\t\t}\n" +
                "\t\t.bottom{\n" +
                "\t\t\tmargin-top:50px;\n" +
                "\t\t}\n" +
                "\t</style>\n" +
                "</html>";
        // 返回替换后的最终内容
        return content;
    }
    /**
     * 发送邮件方法
     *
     * @param userEmail 收件人邮箱地址
     * @param userEmail 模板id
     * @param list      邮件内容
     */
    public static Boolean sendMail(String userEmail,Integer messageId,List<String> list) {
        return sendMail(userEmail, messageId, list, null);
    }

    /**
     * @param subjectOverride 非空时用消息框架里按 template_code 取到的模板名作为邮件标题/抬头。
     *                        存量按 templateId 查到的行曾出现标题与内容不匹配（验证码邮件标题显示「审批催办通知」）。
     */
    public static Boolean sendMail(String userEmail, Integer messageId, List<String> list, String subjectOverride) {
        Boolean start = false;
        String text = null;
        lastError = "";
        // 初始化 邮件模板信息
        setEmailTemplate();
        if (StringUtils.isEmpty(USERNAME) || StringUtils.isEmpty(PASSWORD)) {
            lastError = "发件邮箱未配置（后台 setting_email / setting_email_code 为空）";
            throw new IllegalStateException(lastError);
        }
        HOST = resolveHost(USERNAME);
        ResponseResult resp = mailUtil.baseServiceClient.selectListByTemplateId(messageId);
        // 旧模板表被新消息框架接管后，缺模板 / 列不兼容都会让 Feign 回落成 data=null。
        // 这里逐层判空并给出明确提示，避免 NullPointerException 被兜底成含糊的 20010。
        if (resp == null || resp.getData() == null) {
            throw new IllegalArgumentException("邮件模板未配置或已失效: templateId=" + messageId);
        }
        BaseMessageTemplateVO baseMessageTemplateVO = JSON.parseObject(JSON.toJSONString(resp.getData()), BaseMessageTemplateVO.class);
        if (baseMessageTemplateVO == null || baseMessageTemplateVO.getEmailTemplateContent() == null) {
            throw new IllegalArgumentException("邮件模板内容为空: templateId=" + messageId);
        }
        String emailTemplateContent = baseMessageTemplateVO.getEmailTemplateContent();
        String templateName = baseMessageTemplateVO.getTemplateName() == null
                ? "验证码" : baseMessageTemplateVO.getTemplateName();
        if (subjectOverride != null && !subjectOverride.trim().isEmpty()) {
            templateName = subjectOverride.trim();
        }
        text = setContentEmail(templateName, emailTemplateContent, list);
        try {
            // 创建邮件发送器实例
            JavaMailSenderImpl senderImpl = new JavaMailSenderImpl();
            // 设置系统属性以优先使用IPv4协议栈
            System.setProperty("java.net.preferIPv4Stack", "true");
            // 设定mail server
            senderImpl.setHost(HOST);

            // 创建MIME协议的邮件对象
            MimeMessage mailMessage = senderImpl.createMimeMessage();
            // 创建邮件帮助器，用于设置邮件内容等信息
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true, "utf-8");
            // 添加附件（如需附件，解除注释并提供文件名和文件对象）
            // messageHelper.addAttachment(fileName,file);

            // 设置收件人
            messageHelper.setTo(userEmail);
            // 设置发件人
            messageHelper.setFrom(USERNAME);
            // 设置邮件标题
            messageHelper.setSubject(templateName);
            // 设置邮件内容，true表示开启HTML文本格式
            messageHelper.setText(text, true);
            // 设置发件人邮箱信息，username表示用户邮箱，password表示对应邮件授权码
            senderImpl.setUsername(USERNAME);
            senderImpl.setPassword(PASSWORD);
            // 开启ssl
            Properties properties = new Properties();
            // 开启认证
            properties.setProperty("mail.smtp.auth", "true");
            // 启用调试
            properties.setProperty("mail.debug", "true");
            // 设置链接超时
            properties.setProperty("mail.smtp.timeout", "200000");
            // 设置端口
            properties.setProperty("mail.smtp.port", PORT);
            // 设置ssl端口
            properties.setProperty("mail.smtp.socketFactory.port", PORT);
            // 设置不回退到非SSL端口
            properties.setProperty("mail.smtp.socketFactory.fallback", "false");
            // 设置使用SSL协议的Socket工厂类
            properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            // 465 为 SMTPS 隐式加密口：必须显式开启 SSL，否则 AUTH 会在明文连接上失败
            properties.setProperty("mail.smtp.ssl.enable", "true");
            properties.setProperty("mail.smtp.ssl.trust", HOST);
            properties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            properties.setProperty("mail.smtp.starttls.enable", "true");
            properties.setProperty("mail.smtp.connectiontimeout", "15000");
            properties.setProperty("mail.smtp.writetimeout", "15000");
            // 设置邮件发送器的JavaMail属性
            senderImpl.setJavaMailProperties(properties);
            // 发送邮件
            senderImpl.send(mailMessage);
            // 打印成功信息
            System.out.println("邮件发送成功..");
            start = true;
        } catch (MessagingException e) {
            // 记录真实失败原因，供发码接口原样返回，避免被误报成「格式不正确」
            lastError = "SMTP 发送失败（" + HOST + ":" + PORT + "）：" + e.getClass().getSimpleName()
                    + (e.getMessage() == null ? "" : " - " + e.getMessage());
            System.out.println("=== 邮件发送失败: " + lastError);
            e.printStackTrace();
        }finally {
            return start;
        }
    }

}
