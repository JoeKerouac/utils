package com.joe.utils.common;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * jmail工具，要在类路径配置一个mail.properties文件，需要包含： mail.username; mail.password;
 * mail.protocol; mail.host; mail.port;
 *
 * @author joe
 */
public class JMail {
    private static final Logger logger = LoggerFactory.getLogger(JMail.class);
    private static Properties props;
    private static Session mailSession;
    // 用户名
    private static String username;
    // 邮箱host
    private static String host;
    //邮箱协议
    private static String protocol;
    // 邮箱密码
    private static String password;
    private Transport transport;

    private JMail() {
    }

    static {
        try {
            props = new Properties();
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("mail.properties");
            props.load(is);
            is.close();

            mailSession = Session.getDefaultInstance(props);
            username = props.getProperty("mail.username");
            host = props.getProperty("mail.host");
            password = props.getProperty("mail.password");
            protocol = props.getProperty("mail.protocol");
        } catch (Exception e) {
            logger.error("配置加载失败", e);
        }
    }

    /**
     * 发送邮件
     *
     * @param body  邮件内容
     * @param to    邮件接收人
     * @param title 邮件标题
     * @return 如果发送成功则返回<code>true</code>
     */
    public static boolean sendEmail(EmailBody body, String to, String title) {
        return sendEmail(body, to, title, null);
    }

    /**
     * 发送带附件的邮件
     *
     * @param body      邮件正文
     * @param to        邮件接收人
     * @param title     邮件标题
     * @param fileParts 邮件附件
     * @return 如果发送成功则返回<code>true</code>
     */
    public static boolean sendEmail(EmailBody body, String to, String title, FilePart[] fileParts) {
        if (StringUtils.isEmpty(to) || (body == null && fileParts == null)) {
            logger.debug("邮件没有设置接收人或者没有正文");
            return false;
        }

        Message msg;
        JMail jmail = new JMail();
        try {
            logger.debug("准备发送邮件");
            msg = new MimeMessage(mailSession);
            logger.debug("开始设置消息头");
            /* 邮件消息头设置 */
            msg.setFrom(new InternetAddress(username));
            msg.setRecipients(RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(title);

            logger.debug("消息头设置完毕，开始设置正文");
            /* 邮件消息内容设置 */
            Multipart msgPart = new MimeMultipart("mixed");
            msg.setContent(msgPart);

			/* 正文 */
            MimeBodyPart bodyPart = new MimeBodyPart(); // 表示正文
            bodyPart.setContent(body.body, (body.html ? "text/html" : "text/plain") + ";charset=" + Charset
                    .defaultCharset().name());
            msgPart.addBodyPart(bodyPart);

            logger.debug("正文设置完毕，开始添加附件");

			/* 下面为设置附件 */
            if (fileParts != null && fileParts.length != 0) {
                for (FilePart filePart : fileParts) {
                    MimeBodyPart attach = new MimeBodyPart();
                    attach.attachFile(filePart.getFile());
                    attach.setFileName(filePart.getName());
                    msgPart.addBodyPart(attach);
                }
            }
        } catch (Exception e) {
            logger.error("邮件消息生成失败", e);
            return false;
        }
        logger.debug("开始发送邮件");

        jmail.connect();
        try {
            jmail.transport.sendMessage(msg, msg.getAllRecipients());
            logger.debug("邮件发送成功");
            return true;
        } catch (Exception e) {
            logger.error("邮件发送异常", e);
            return false;
        } finally {
            jmail.close();
        }
    }

    /**
     * 连接邮箱服务器
     */
    private boolean connect() {
        if (transport != null) {
            if (transport.isConnected()) {
                logger.debug("当前邮箱处于在线状态");
                return true;
            }
            try {
                transport.close();
            } catch (Exception e) {
                logger.error("关闭现有的transport异常");
            }
            transport = null;
        }

        try {
            logger.debug("开始加载邮箱配置");

            transport = mailSession.getTransport(protocol);
            transport.connect(host, username, password);
            logger.debug("邮箱配置加载完毕");
            return true;
        } catch (Exception e) {
            logger.error("邮箱加载失败", e);
            return false;
        }
    }

    /**
     * 关闭客户端连接
     */
    private void close() {
        if (transport != null) {
            if (transport.isConnected()) {
                try {
                    transport.close();
                    logger.debug("邮件客户端关闭完成");
                } catch (MessagingException e) {
                    logger.debug("关闭邮件客户端时发生异常", e);
                }
            }
        }

    }

    /**
     * 附件
     *
     * @author joe
     */
    @Data
    public static class FilePart {
        // 附件文件
        private File file;
        // 附件名称
        private String name;

        /**
         * 创建附件
         *
         * @param file 附件文件
         * @param name 附件文件名称
         */
        public FilePart(final File file, final String name) {
            if (file == null || StringUtils.isEmpty(name)) {
                throw new NullPointerException("文件和文件名不能为空");
            }
            this.file = file;
            this.name = name;
        }
    }

    /**
     * 邮件内容
     */
    @Data
    public static class EmailBody {
        //内容
        private String body;
        //是否是html
        private boolean html = false;

        public EmailBody(String body, boolean html) {
            this.body = body;
            this.html = html;
        }
    }
}
