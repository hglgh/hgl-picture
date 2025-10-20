package com.hgl.hglpicturebackend.manager.websocket.validator;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @ClassName: AbstractHandshakeValidator
 * @Package: com.hgl.hglpicturebackend.manager.websocket.validator
 * @Description: 抽象校验器基类
 * @Author HGL
 * @Create: 2025/10/20 10:25
 */
public abstract class AbstractHandshakeValidator implements HandshakeValidator {

    protected HandshakeValidator nextValidator;

    @Override
    public void setNext(HandshakeValidator next) {
        this.nextValidator = next;
    }

    @Override
    public boolean validate(HttpServletRequest request, Map<String, Object> attributes) {
        boolean result = doValidate(request, attributes);
        if (!result) {
            return false;
        }

        if (nextValidator != null) {
            return nextValidator.validate(request, attributes);
        }

        return true;
    }

    /**
     * 执行当前校验器的校验逻辑
     *
     * @param request    HTTP请求
     * @param attributes WebSocket会话属性
     * @return 是否通过校验
     */
    protected abstract boolean doValidate(HttpServletRequest request, Map<String, Object> attributes);
}
