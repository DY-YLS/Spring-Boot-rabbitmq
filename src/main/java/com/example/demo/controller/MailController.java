package com.example.demo.controller;

import com.example.demo.entity.Mail;
import com.example.demo.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.time.LocalDate;
import java.util.Date;

@RestController
public class MailController {
    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    private MailService mailService;

    @RequestMapping("/sendMail")
    public void sendMail(MultipartFile[] files) throws MessagingException {
        sendComplexMail(files);
    }


    @RequestMapping("/sendMail2")
    public void sendMail2(MultipartFile[] files) {
        Mail mail = new Mail();
        mail.setText("ahaahhahah");
        mail.setSubject("1234");
        mail.setTo("1783134916@qq.com");
        mail.setCc("973086732@qq.com,1329926763@qq.com");
        mail.setMultipartFiles(files);
        mail = mailService.sendMail(mail);
        System.out.println(mail.toString());
    }

    /**
     * 发送简单邮件
     */
    private void sendSimpleMail() {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("1329926763@qq.com");
        simpleMailMessage.setTo("1783134916@qq.com");
        simpleMailMessage.setText("新年快乐");
        simpleMailMessage.setSubject("happy new year");
        mailSender.send(simpleMailMessage);
    }

    /**
     * 发送复杂邮件
     *
     * @param files
     * @throws MessagingException
     */
    private void sendComplexMail(MultipartFile[] files) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);//true表示支持复杂类型
        mimeMessageHelper.setFrom("1329926763@qq.com");
        mimeMessageHelper.setTo("1783134916@qq.com");
        mimeMessageHelper.setSubject("happy");
        mimeMessageHelper.setText("快乐");
        mimeMessageHelper.setSentDate(new Date());
        if (files != null) {
            for (MultipartFile file : files
            ) {
                mimeMessageHelper.addAttachment(file.getOriginalFilename(), file);
                System.out.println(file.getOriginalFilename());
            }
        }
        mailSender.send(mimeMessage);
    }
}
