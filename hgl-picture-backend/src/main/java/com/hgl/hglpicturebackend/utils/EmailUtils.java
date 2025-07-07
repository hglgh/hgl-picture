package com.hgl.hglpicturebackend.utils;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName EmailUtils
 * @Author 请别把我整破防
 * @Description //TODO
 * @Date 2025/7/2 16:30
 */
@Component
public class EmailUtils {

    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.nickname}")
    private String nickname;

    /**
     * 发送简单文本邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param text    邮件内容
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        String format = String.format("【%s】-%s", nickname, subject);
        message.setSubject(format);
        message.setText(text);
        // 设置发件人邮箱
        message.setFrom("2897957163@qq.com");


        javaMailSender.send(message);
    }
}
