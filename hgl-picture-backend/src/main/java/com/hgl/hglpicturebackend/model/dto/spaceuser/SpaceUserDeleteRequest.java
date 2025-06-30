package com.hgl.hglpicturebackend.model.dto.spaceuser;


import com.hgl.hglpicturebackend.common.DeleteRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @ClassName SpaceUserDeleteRequest
 * @Author 请别把我整破防
 * @Description //TODO
 * @Date 2025/6/30 15:09
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserDeleteRequest extends DeleteRequest implements Serializable {
    /**
     * 团队空间id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
