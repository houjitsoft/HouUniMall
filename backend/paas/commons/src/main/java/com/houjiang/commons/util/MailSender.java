package com.houjiang.commons.util;

import com.sun.mail.util.MailSSLSocketFactory;
import jakarta.activation.DataHandler;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.util.ByteArrayDataSource;

import java.util.List;
import java.util.Properties;

/**
 * 通用邮件发送工具（支持附件）
 *
 * 适用场景：薪资表/合同/发票等字节流附件的邮件发送。
 *
 * 使用 QQ 邮箱 SMTP（smtp.qq.com:465, SSL）。
 * 账号与授权码目前以常量形式写死，如后续需改动请同步调整 SENDER_EMAIL / SENDER_AUTH。
 *
 * @author Amos
 * @since 2026-04-13
 */
public class MailSender {

    /** 发件邮箱（QQ 邮箱） */
    public static final String SENDER_EMAIL = "2869571987@qq.com";
    /** QQ 邮箱 SMTP 授权码（非登录密码） */
    public static final String SENDER_AUTH  = "kgxhplovwavgdfda";
    /** 发件人显示名 */
    public static final String SENDER_NAME  = "HouCloud薪资系统";
    /** SMTP 主机 */
    public static final String SMTP_HOST    = "smtp.qq.com";
    /** SMTP 端口（SSL） */
    public static final String SMTP_PORT    = "465";

    /**
     * 发送带附件的 HTML 邮件
     *
     * @param to         主送收件人（必填）
     * @param cc         抄送邮箱列表（可空）
     * @param subject    主题
     * @param htmlBody   HTML 正文
     * @param attachName 附件文件名（含扩展名，可空=无附件）
     * @param attachData 附件字节（可空=无附件）
     * @param contentType 附件 MIME（例 application/vnd.openxmlformats-officedocument.spreadsheetml.sheet）
     * @return true=成功
     */
    public static boolean sendHtmlWithAttachment(String to, List<String> cc,
                                                 String subject, String htmlBody,
                                                 String attachName, byte[] attachData,
                                                 String contentType) throws Exception {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("收件人不能为空");
        }
        Properties prop = new Properties();
        prop.setProperty("mail.host", SMTP_HOST);
        prop.setProperty("mail.smtp.port", SMTP_PORT);
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", sf);

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_AUTH);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME, "utf-8"));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to.trim()));
        if (cc != null && !cc.isEmpty()) {
            for (String addr : cc) {
                if (addr != null && !addr.trim().isEmpty()) {
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress(addr.trim()));
                }
            }
        }
        message.setSubject(subject == null ? "" : subject, "utf-8");

        // 正文
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(htmlBody == null ? "" : htmlBody, "text/html;charset=utf-8");

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(bodyPart);

        // 附件
        if (attachName != null && !attachName.isEmpty() && attachData != null && attachData.length > 0) {
            MimeBodyPart attach = new MimeBodyPart();
            String mime = (contentType == null || contentType.isEmpty())
                    ? "application/octet-stream" : contentType;
            attach.setDataHandler(new DataHandler(new ByteArrayDataSource(attachData, mime)));
            attach.setFileName(MimeUtility.encodeText(attachName, "utf-8", null));
            multipart.addBodyPart(attach);
        }
        multipart.setSubType("mixed");

        message.setContent(multipart);
        System.getProperties().setProperty("mail.mime.splitlongparameters", "false");
        System.getProperties().setProperty("mail.mime.charset", "UTF-8");
        message.saveChanges();
        Transport.send(message);
        return true;
    }

    /**
     * 发送 xlsx 附件邮件（封装常用 MIME）
     */
    public static boolean sendExcel(String to, List<String> cc, String subject,
                                    String htmlBody, String fileName, byte[] xlsxData) throws Exception {
        return sendHtmlWithAttachment(to, cc, subject, htmlBody, fileName, xlsxData,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
}
