package com.example.demo.ServiceImpl;

import com.example.demo.entity.Mail;
import com.example.demo.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;

/**
 * 邮件业务类
 */
@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSenderImpl mailSender;


    public Mail sendMail(Mail mail) {
        try {

            checkMail(mail);
            mail = sendMimeMail(mail);
            return saveMail(mail);

        } catch (Exception e) {
            mail.setStatue("fail");
            mail.setError(e.getMessage());
            return mail;
        }
    }

    //检测邮件的信息是否完整
    private void checkMail(Mail mail) {
        if (StringUtils.isEmpty(mail.getTo())) {
            throw new RuntimeException("邮件接收人不能为空");
        }
        if (StringUtils.isEmpty(mail.getSubject())) {
            throw new RuntimeException("邮件主题不能为空");
        }
        if (StringUtils.isEmpty(mail.getText())) {
            throw new RuntimeException("邮件内容不能为空");
        }
    }

    /**
     * 构建并发送复杂邮件信息
     *
     * @param mail
     */
    private Mail sendMimeMail(Mail mail) {
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mailSender.createMimeMessage(), true);//true表示支持复杂邮件

            mail.setFrom(getMailSendFrom());
            mimeMessageHelper.setFrom(mail.getFrom());
            mimeMessageHelper.setTo(mail.getTo());
            mimeMessageHelper.setText(mail.getText());
            mimeMessageHelper.setSubject(mail.getSubject());
            if (!StringUtils.isEmpty(mail.getCc())) {
                mimeMessageHelper.setCc(mail.getCc().split(","));
            }
            if (!StringUtils.isEmpty(mail.getBcc())) {
                mimeMessageHelper.setBcc(mail.getBcc().split(","));
            }
            if (mail.getMultipartFiles() != null) {
                for (MultipartFile file : mail.getMultipartFiles()

                ) {
                    mimeMessageHelper.addAttachment(file.getOriginalFilename(), file);

                }
            }

            mailSender.send(mimeMessageHelper.getMimeMessage());//正式发送邮件

            mail.setStatue("ok");
            return mail;
        } catch (Exception e) {
            throw new RuntimeException(e);//发送失败时抛出异常
        }


    }

    /**
     * 将邮箱内容保存到数据库
     */
    private Mail saveMail(Mail mail) {
        //省略数据库操作。。。
        return mail;
    }


    /**
     * 从配置中获取邮箱发送者
     *
     * @return
     */
    private String getMailSendFrom() {
        return mailSender.getJavaMailProperties().getProperty("from");
    }
}
