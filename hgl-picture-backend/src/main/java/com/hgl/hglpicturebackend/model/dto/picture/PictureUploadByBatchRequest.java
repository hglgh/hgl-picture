package com.hgl.hglpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: PictureUploadRequest
 * Package: com.hgl.hglpicturebackend.model.dto.picture
 * Description: 批量导入图片的请求体
 *
 * @Author HGL
 * @Create: 2024/12/28 15:21
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 抓取数量
     */
    private Integer count = 10;
    /**
     * 搜索关键词
     */
    private String searchText;

    /**
     * 图片名称前缀
     */
    private String namePrefix;

}
