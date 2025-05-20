package com.hgl.hglpicturebackend.model.dto.file;

import lombok.Data;

/**
 * ClassName: UploadPictureResult
 * Package: com.hgl.hglpicturebackend.model.dto.file
 * Description:
 *
 * @Author HGL
 * @Create: 2024/12/28 15:33
 */

/**
 * 上传图片的结果
 * @author 请别把我整破防
 */
@Data
public class UploadPictureResult {

    /**
     * 图片地址
     */
    private String url;

    /**
     * 缩略图地址
     */
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private int picWidth;

    /**
     * 图片高度
     */
    private int picHeight;

    /**
     * 图片宽高比
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 图片主色调
     */
    private String picColor;
}