package com.hgl.hglpicturebackend.model.dto.spaceuser;


import com.hgl.hglpicturebackend.common.DeleteRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName SpaceUserDeleteRequest
 * @Author 请别把我整破防
 * @Description //TODO
 * @Date 2025/6/30 15:09
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserDeleteRequest extends DeleteRequest {
    /**
     * 团队空间id
     */
    private Long spaceId;
}
