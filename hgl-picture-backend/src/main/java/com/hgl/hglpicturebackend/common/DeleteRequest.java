package com.hgl.hglpicturebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 请别把我整破防
 * @desc 通用的删除请求类
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
