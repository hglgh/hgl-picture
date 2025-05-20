package com.hgl.hglpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: PictureReviewRequest
 * Package: com.hgl.hglpicturebackend.model.dto.picture
 * Description: 图片审核请求(管理员审核)
 *
 * @Author HGL
 * @Create: 2025/1/4 17:24
 */
@Data
public class PictureReviewRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;

    /**
     * 状态：0-待审核, 1-通过, 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;
}
