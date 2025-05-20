package com.hgl.hglpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: UserAddRequest
 * Package: com.hgl.hglpicturebackend.model.dto.user
 * Description: 新增用户请求类
 *
 * @Author HGL
 * @Create: 2024/12/16 22:54
 */
@Data
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = 6944418306146058835L;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    private String userRole;
}
