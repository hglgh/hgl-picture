package com.hgl.hglpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: SearchPictureByPictureRequest
 * Package: com.hgl.hglpicturebackend.model.dto.picture
 * Description:
 *
 * @Author HGL
 * @Create: 2025/4/8 18:56
 */
@Data
public class SearchPictureByPictureRequest implements Serializable {
    /**
     * 图片 id
     */
    private Long pictureId;

    private static final long serialVersionUID = 1L;
}
