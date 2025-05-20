package com.hgl.hglpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: UserLoginRequest
 * Package: com.hgl.hglpicturebackend.model.dto
 * Description: 用户登录请求类
 *
 * @Author HGL
 * @Create: 2024/12/10 23:49
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -2878250363351610424L;
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;
}
