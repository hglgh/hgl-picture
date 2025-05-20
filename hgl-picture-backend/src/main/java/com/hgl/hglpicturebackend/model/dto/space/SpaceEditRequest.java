package com.hgl.hglpicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 请别把我整破防
 * @desc 空间编辑请求，给用户使用，目前仅允许编辑空间名称
 */
@Data
public class SpaceEditRequest implements Serializable {

    /**
     * 空间 id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    private static final long serialVersionUID = 1L;
}
