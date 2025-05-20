package com.hgl.hglpicturebackend.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ClassName: SpaceLevel
 * Package: com.hgl.hglpicturebackend.model.dto.space
 * Description:
 *
 * @Author HGL
 * @Create: 2025/4/6 11:51
 */
@Data
@AllArgsConstructor
public class SpaceLevel {
    /**
     * 中文
     */
    private String text;

    /**
     * 值
     */
    private int value;

    /**
     * 最大数量
     */
    private long maxCount;

    /**
     * 最大容量
     */
    private long maxSize;
}
