package com.houjiang.commons.util;

import com.sun.mail.util.MailSSLSocketFactory;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.xbill.DNS.Record;
import org.xbill.DNS.*;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

/**
 * 日期：2022/7/15 - 18:04
 * 作用：发送邮件
 * 需求：文本，图片，附件(文本，图片，视频，音乐)与拼接发送
 * setContent();    文本
 * setContentID();  图片
 * setFileName();   附件
 * 使用的路径是本项目的 resources 目录
 * @author Amos
 */
public class SendMailUtil {

    public static boolean validateEmail(String from) throws TextParseException {
        boolean isValid = false;
        String domain = from.substring(from.indexOf('@') + 1);
        Record[] records = new Lookup(domain, Type.MX).run();
        if (records != null) {
            for (Record record : records) {
                MXRecord mx = (MXRecord) record;
                System.err.println("MX Server:  " + mx.getTarget());
                if (mx.getTarget().toString().contains("qq.com") && !from.contains("@qq.com")) {
                    isValid = true;
                }
            }
        }
        return isValid;
    }

    /**
     * 发送发票
     */
    public static void sendInvoice(Map<String, Object> map) throws Exception {

        // 给用户发送邮件的邮箱
        final String from = (String) map.get("sendEmail");
        // 邮箱的用户名
        final String username = (String) map.get("sendEmail");
        // 邮箱授权码
        final String password = (String) map.get("emailCode");
        // 发送邮件的服务器地址，QQ服务器
        String host = (String) map.get("server");
        // 接收人邮箱
        final String to = (String) map.get("receivingEmail");
        // 邮件主题
        final String title = "商品订单发票";

        // 使用QQ邮箱时配置
        Properties prop = new Properties();

        if (validateEmail(from)) {
            // 设置QQ邮件服务器
            host = "smtp.exmail.qq.com";
        }
        prop.setProperty("mail.host", host);
        // 设置端口为 465
        prop.setProperty("mail.smtp.port", "465");
        // 邮件发送协议
        prop.setProperty("mail.transport.protocol", "smtp");
        // 需要验证用户名和密码
        prop.setProperty("mail.smtp.auth", "true");

        Session session = null;
        // 关于QQ邮箱，还要设置SSL加密，其他邮箱不需要
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", sf);
        // 创建定义整个邮件程序所需的环境信息的 Session 对象，QQ才有，其他邮箱就不用了
        session = Session.getDefaultInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // 发件人邮箱用户名，授权码
                return new PasswordAuthentication(username, password);
            }
        });

        // 开启 Session 的 debug 模式，这样就可以查看程序发送 Email 的运行状态
        session.setDebug(true);

        // 创建邮件，写邮件
        // 需要传递 session
        MimeMessage message = new MimeMessage(session);
        // 指明邮件的发件人
        message.setFrom(new InternetAddress(from));
        // 指明邮件的收件人
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        // 邮件主题
        message.setSubject(title);

        // 文本
        MimeBodyPart body2 = new MimeBodyPart();
        body2.setContent("""
                尊敬的客户:
                   您的发票已发送，请下载附件文档。下载后，如果是PDF文件，可使用PDF阅读器查看。本邮件由系统自动发送，请勿直接回复。""", "text/html;charset=utf-8");

        // 附件
        MimeBodyPart body3 = new MimeBodyPart();
        String route = (String) map.get("route");
        String fileName = "invoice.png";
        URL url = new URL(route);
        // 下载网络文件到本地临时文件
        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
             FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
        body3.setDataHandler(new DataHandler(new FileDataSource(fileName)));
        body3.setFileName(MimeUtility.encodeText("invoice.png", "utf-8", null));

        // 拼接邮件正文内容
        MimeMultipart mimeMultipart1 = new MimeMultipart();
        mimeMultipart1.addBodyPart(body2);
        // 文本和图片内嵌成功
        mimeMultipart1.setSubType("related");

        // 将拼装好的正文内容设置为主体
        MimeBodyPart contentText = new MimeBodyPart();
        contentText.setContent(mimeMultipart1);

        // 拼接附件与正文内容
        MimeMultipart allFile = new MimeMultipart();
        allFile.addBodyPart(body3);
        allFile.addBodyPart(contentText);
        // 正文和附件都存在邮件中，所以类型设置为 mixed，三种方式，两个附件单词都可用：mixed(附件) > related(附件) > alternative(纯文本)
        allFile.setSubType("mixed");

        message.setFrom(new InternetAddress(from, "商城平台", "utf-8"));
        System.getProperties().setProperty("mail.mime.splitlongparameters", "false");
        System.getProperties().setProperty("mail.mime.charset", "UTF-8");

        // 设置到消息中，保存修改
        // 把最后编辑好的邮件放到消息当中
        message.setContent(allFile);
        // 保存修改
        message.saveChanges();
        // 发送邮件
        Transport.send(message);

    }

    /**
     * 使用Base64解码并写入文件
     * 该方法接收一个Base64编码的字符串作为路径，解码它，然后将解码后的字节写入一个文件
     *
     * @param route    需要解码的Base64编码字符串，被视为编码后的文件内容
     * @param fileName 指定要写入的文件名称
     */
    private static void base64DecodeWrite(String route, String fileName) {
        // 定义一个字节数组来存储解码后的数据
        byte[] buffer;
        try {
            // 使用Base64解码器对输入的编码字符串进行解码，得到原始的字节数据
            buffer = Base64.getDecoder().decode(route);
            // 创建一个文件输出流，用于将解码后的数据写入到指定的文件中
            FileOutputStream out = new FileOutputStream(fileName);
            // 将解码后的字节数据写入文件
            out.write(buffer);
            // 关闭文件输出流，释放资源
            out.close();
        } catch (Exception e) {
            // 如果在解码或文件写入过程中发生异常，则输出异常信息
            System.err.println("异常信息:" + e.getMessage());
        }
    }
}
