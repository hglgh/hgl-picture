package com.hgl.hglpicturebackend.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 请别把我整破防
 */
@Data
public class SpaceRankAnalyzeRequest implements Serializable {

    /**
     * 排名前 N 的空间
     */
    private Integer topN = 10;

    private static final long serialVersionUID = 1L;
}
