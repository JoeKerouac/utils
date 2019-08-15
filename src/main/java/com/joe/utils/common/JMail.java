package com.joe.utils.common;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.*;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joe.utils.common.string.StringUtils;
import com.joe.utils.concurrent.ThreadUtil;

import lombok.Data;

/**
 * jmail工具
 *
 * @author joe
 */
public class JMail {
    private static final Logger          logger = LoggerFactory.getLogger(JMail.class);
    private static final Future<Boolean> ERROR  = new Future<Boolean>() {
                                                    @Override
                                                    public boolean cancel(boolean mayInterruptIfRunning) {
                                                        return false;
                                                    }

                                                    @Override
                                                    public boolean isCancelled() {
                                                        return false;
                                                    }

                                                    @Override
                                                    public boolean isDone() {
                                                        return true;
                                                    }

                                                    @Override
                                                    public Boolean get() throws InterruptedException,
                                                                         ExecutionException {
                                                        return false;
                                                    }

                                                    @Override
                                                    public Boolean get(long timeout,
                                                                       TimeUnit unit) throws InterruptedException,
                                                                                      ExecutionException,
                                                                                      TimeoutException {
                                                        return false;
                                                    }
                                                };
    private Session                      mailSession;
    // 用户名
    private String                       username;
    // 邮箱host
    private String                       host;
    //邮箱协议
    private String                       protocol;
    // 邮箱密码
    private String                       password;
    private Transport                    transport;
    /**
     * 邮件发送线程池
     */
    private ExecutorService              executor;

    /**
     * 默认构造器
     *
     * @param properties 邮箱配置
     */
    private JMail(Properties properties) {
        Properties properties1 = properties;
        this.mailSession = Session.getDefaultInstance(properties1);
        this.username = properties1.getProperty("mail.username");
        this.host = properties1.getProperty("mail.host");
        this.password = properties1.getProperty("mail.password");
        this.protocol = properties1.getProperty("mail.protocol");
        this.executor = ThreadUtil.createPool(ThreadUtil.PoolType.IO);
    }

    /**
     * 根据配置文件获取Jmail客户端
     *
     * @param properties 配置文件，需要包含mail.username、mail.host、mail.password、mail.protocol四个键值对
     * @return JMail客户端
     */
    public static JMail getInstance(Properties properties) {
        if (properties == null) {
            throw new NullPointerException("properties must not be null");
        }
        return new JMail(properties);
    }

    /**
     * 发送邮件
     *
     * @param body  邮件内容
     * @param to    邮件接收人
     * @param title 邮件标题
     * @return 如果发送成功则返回<code>true</code>
     */
    public Future<Boolean> sendEmail(EmailBody body, String to, String title) {
        return sendEmail(body, to, title, null, null);
    }

    /**
     * 发送邮件
     *
     * @param body     邮件内容
     * @param to       邮件接收人
     * @param title    邮件标题
     * @param callback 回调函数，邮件发送完毕后会执行
     * @return 如果发送成功则返回<code>true</code>
     */
    public Future<Boolean> sendEmail(EmailBody body, String to, String title,
                                     JMailCallback callback) {
        return sendEmail(body, to, title, null, callback);
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
    public Future<Boolean> sendEmail(EmailBody body, String to, String title,
                                     FilePart[] fileParts) {
        return sendEmail(body, to, title, fileParts, null);
    }

    /**
     * 发送带附件的邮件
     *
     * @param body      邮件正文
     * @param to        邮件接收人
     * @param title     邮件标题
     * @param fileParts 邮件附件
     * @param callback  回调函数，邮件发送完毕后会执行
     * @return 如果发送成功则返回<code>true</code>
     */
    public Future<Boolean> sendEmail(EmailBody body, String to, String title, FilePart[] fileParts,
                                     JMailCallback callback) {
        if (StringUtils.isEmpty(to) || (body == null && fileParts == null)) {
            logger.debug("邮件没有设置接收人或者没有正文");
            execCallback(false, body, to, title, fileParts, null, this, callback);
            return ERROR;
        }

        EmailTask task = new EmailTask(body, to, title, fileParts, callback);
        return executor.submit(task);
    }

    /**
     * 执行回调函数，如果函数为null则不执行
     *
     * @param result    结果
     * @param body      邮件正文
     * @param to        邮件接收人
     * @param title     邮件标题
     * @param fileParts 邮件附件
     * @param error     发送邮件中发生的异常，不存在则为null
     * @param mail      邮箱客户端
     * @param callback  回调函数
     */
    private void execCallback(boolean result, EmailBody body, String to, String title,
                              FilePart[] fileParts, Throwable error, JMail mail,
                              JMailCallback callback) {
        if (callback != null) {
            callback.call(result, body, to, title, fileParts, error, mail);
        }
    }

    /**
     * 检查邮箱服务器连接状态
     *
     * @return 状态正常返回true，否则抛出异常
     * @throws MessagingException 异常
     */
    private boolean checkConnectStatus() throws MessagingException {
        if (transport != null) {
            if (transport.isConnected()) {
                logger.debug("当前邮箱处于在线状态");
                return true;
            } else {
                synchronized (this) {
                    if (!transport.isConnected()) {
                        try {
                            logger.debug("当前邮箱状态不正常，关闭重新开启");
                            transport.close();
                        } catch (Exception e) {
                            logger.error("关闭现有的transport异常");
                        }
                        return connect();
                    } else {
                        logger.debug("当前邮箱处于在线状态");
                        return true;
                    }
                }
            }
        } else {
            synchronized (this) {
                if (transport != null) {
                    return checkConnectStatus();
                } else {
                    return connect();
                }
            }
        }
    }

    private boolean connect() throws MessagingException {
        logger.debug("开始加载邮箱配置");
        transport = mailSession.getTransport(protocol);
        transport.connect(host, username, password);
        logger.debug("邮箱配置加载完毕");
        return true;
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
        private File   file;
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
        private String  body;
        //是否是html
        private boolean html = false;

        public EmailBody(String body, boolean html) {
            this.body = body;
            this.html = html;
        }
    }

    /**
     * 邮件发送回调，发送完成会调用
     */
    public interface JMailCallback {
        /**
         * 邮件回调函数
         *
         * @param result    发送结果，true表示发送成功
         * @param body      本次发送的邮件body
         * @param to        本次邮件的接收人
         * @param title     本次邮件的标题
         * @param fileParts 本次邮件的附件
         * @param error     如果发送失败并且异常，那么会有该值
         * @param mail      本次邮件的客户端
         */
        void call(boolean result, EmailBody body, String to, String title, FilePart[] fileParts,
                  Throwable error, JMail mail);
    }

    /**
     * 邮件发送任务
     */
    private class EmailTask implements Callable<Boolean> {
        private EmailBody     body;
        private String        to;
        private String        title;
        private FilePart[]    fileParts;
        private JMailCallback callback;

        /**
         * 邮件任务构造函数
         *
         * @param body      邮件body
         * @param to        邮件收件人
         * @param title     邮件标题
         * @param fileParts 邮件附件
         * @param callback  邮件执行完毕回调
         */
        public EmailTask(EmailBody body, String to, String title, FilePart[] fileParts,
                         JMailCallback callback) {
            this.body = body;
            this.to = to;
            this.title = title;
            this.fileParts = fileParts;
            this.callback = callback;
        }

        @Override
        public Boolean call() {
            Message msg;
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
                bodyPart.setContent(body.body, (body.html ? "text/html" : "text/plain")
                                               + ";charset=" + Charset.defaultCharset().name());
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
                execCallback(false, body, to, title, fileParts, e, JMail.this, callback);
                return false;
            }
            logger.debug("开始发送邮件");
            try {
                logger.debug("检查客户端状态");
                checkConnectStatus();
                logger.debug("客户端状态检查完毕，准备发送");
                transport.sendMessage(msg, msg.getAllRecipients());
                logger.debug("邮件发送成功");
                execCallback(true, body, to, title, fileParts, null, JMail.this, callback);
                return true;
            } catch (Exception e) {
                logger.error("邮件发送异常", e);
                execCallback(false, body, to, title, fileParts, e, JMail.this, callback);
                return false;
            }
        }
    }
}
