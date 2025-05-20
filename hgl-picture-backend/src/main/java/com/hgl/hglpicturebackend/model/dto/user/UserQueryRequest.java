package com.hgl.hglpicturebackend.model.dto.user;

import com.hgl.hglpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * ClassName: UserQueryRequest
 * Package: com.hgl.hglpicturebackend.model.dto.user
 * Description: 用户查询请求类
 *
 * @Author HGL
 * @Create: 2024/12/16 22:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = -4059789821112092490L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;
}
