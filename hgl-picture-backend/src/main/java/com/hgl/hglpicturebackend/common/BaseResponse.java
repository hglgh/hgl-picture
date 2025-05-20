package com.hgl.hglpicturebackend.common;

import com.hgl.hglpicturebackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: BaseResponse
 * Package: com.hgl.hglpicturebackend.common
 * Description: 全局响应封装类
 *
 * @Author HGL
 * @Create: 2024/12/8 11:16
 */
@Data
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *
     * 返回状态码
     */
    private int code;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code, T data) {
        this(code, "", data);
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
