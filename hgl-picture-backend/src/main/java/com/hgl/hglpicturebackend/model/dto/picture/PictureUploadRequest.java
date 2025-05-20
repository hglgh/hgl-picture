package com.hgl.hglpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: PictureUploadRequest
 * Package: com.hgl.hglpicturebackend.model.dto.picture
 * Description:
 *
 * @Author HGL
 * @Create: 2024/12/28 15:21
 */
@Data
public class PictureUploadRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 图片id(用于修改)
     */
    private Long id;
    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 图片名称
     */
    private String pictureName;

    /**
     * 空间Id
     */
    private Long spaceId;
}
