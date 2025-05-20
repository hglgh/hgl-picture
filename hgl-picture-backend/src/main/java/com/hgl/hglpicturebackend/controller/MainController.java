package com.hgl.hglpicturebackend.controller;

import com.hgl.hglpicturebackend.common.BaseResponse;
import com.hgl.hglpicturebackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: MainController
 * Package: com.hgl.hglpicturebackend.controller
 * Description:
 *
 * @Author HGL
 * @Create: 2024/12/8 11:45
 */
@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public BaseResponse<String> health(){
        return ResultUtils.success("healthy");
    }
}
