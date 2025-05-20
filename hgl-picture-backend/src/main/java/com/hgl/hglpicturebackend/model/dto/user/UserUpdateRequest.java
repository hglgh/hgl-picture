package com.hgl.hglpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: UserUpdateRequest
 * Package: com.hgl.hglpicturebackend.model.dto.user
 * Description: 修改用户请求类
 *
 * @Author HGL
 * @Create: 2024/12/16 23:01
 */
@Data
public class UserUpdateRequest implements Serializable {

    private static final long serialVersionUID = -5636449440778719532L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/vip
     */
    private String userRole;
}
