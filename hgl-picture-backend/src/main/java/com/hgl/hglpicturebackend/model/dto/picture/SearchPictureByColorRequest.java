package com.hgl.hglpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: SearchPictureByColorRequest
 * Package: com.hgl.hglpicturebackend.model.dto.picture
 * @Description: 按照图片主色调搜索图片
 *
 * @Author HGL
 * @Create: 2025/4/9 13:27
 */
@Data
public class SearchPictureByColorRequest implements Serializable {
    /**
     * 空间id
     */
    private Long spaceId;
    /**
     * 图片主色调
     */
    private String picColor;
    private static final long serialVersionUID = 1L;

}
