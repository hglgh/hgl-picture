package com.hgl.hglpicturebackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 请别把我整破防
 * @description 编辑空间成员请求类，仅空间管理员使用
 */
@Data
public class SpaceUserEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 团队空间id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
