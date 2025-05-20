package com.hgl.hglpicturebackend.model.vo.picture;

import lombok.Data;

import java.util.List;

/**
 * ClassName: PictureTagCategory
 * Package: com.hgl.hglpicturebackend.model.vo.picture
 * Description: 图片标签分类
 *
 * @Author HGL
 * @Create: 2024/12/29 14:32
 */
@Data
public class PictureTagCategory {
    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;
}
