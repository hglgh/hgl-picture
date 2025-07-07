package com.hgl.hglpicturebackend.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import com.hgl.hglpicturebackend.common.BaseResponse;
import com.hgl.hglpicturebackend.common.ResultUtils;
import com.hgl.hglpicturebackend.model.vo.captcha.CaptchaVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: MainController
 * Package: com.hgl.hglpicturebackend.controller
 * Description:
 *
 * @Author HGL
 * @Create: 2024/12/8 11:45
 */
@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("healthy");
    }

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public BaseResponse<CaptchaVO> captcha() {
        // 创建指定宽高和位数的验证码（默认是4位）
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(130, 48);

        // 设置只显示数字（不显示字母）
        captcha.setGenerator(new RandomGenerator("0123456789", 4));

        // 获取验证码文本
        String captchaCode = captcha.getCode();

        // 获取 Base64 编码的图片数据（Data URI 格式）
        String captchaImageBase64 = captcha.getImageBase64();
        // 将图片数据转换为 Data URI,加上 MIME 前缀
        captchaImageBase64 = String.format("data:image/png;base64,%s", captchaImageBase64);
        return ResultUtils.success(new CaptchaVO(captchaCode, captchaImageBase64));
    }
}
