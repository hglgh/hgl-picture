package com.hgl.hglpicturebackend.manager.websocket.validator;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @ClassName: HandshakeValidator
 * @Package: com.hgl.hglpicturebackend.manager.websocket.validator
 * @Description: WebSocket握手校验器接口 - 责任链模式
 * @Author HGL
 * @Create: 2025/10/20 10:24
 */
public interface HandshakeValidator {

    /**
     * 获取校验器排序值，数值越小优先级越高
     *
     * @return 排序值
     */
    int getOrder();

    /**
     * 执行校验
     *
     * @param request    HTTP请求
     * @param attributes WebSocket会话属性
     * @return 是否通过校验
     */
    boolean validate(HttpServletRequest request, Map<String, Object> attributes);

    /**
     * 设置下一个校验器
     *
     * @param next 下一个校验器
     */
    void setNext(HandshakeValidator next);
}
