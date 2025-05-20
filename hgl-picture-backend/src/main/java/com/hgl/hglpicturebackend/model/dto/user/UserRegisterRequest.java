package com.hgl.hglpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: UserRegisterRequest
 * Package: com.hgl.hglpicturebackend.model.dto
 * Description: 用户注册请求类
 *
 * @Author HGL
 * @Create: 2024/12/10 22:40
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
