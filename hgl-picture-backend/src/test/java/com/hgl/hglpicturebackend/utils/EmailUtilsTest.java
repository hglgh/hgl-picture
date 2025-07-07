package com.hgl.hglpicturebackend.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class EmailUtilsTest {
    @Resource
    private EmailUtils emailUtils;

    @Test
    void sendEmail() {
        emailUtils.sendSimpleEmail("2897957163@qq.com", "测试邮件", "这是一封测试邮件，请勿回复。");
    }
}