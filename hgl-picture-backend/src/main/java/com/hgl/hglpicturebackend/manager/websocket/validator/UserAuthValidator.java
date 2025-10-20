package com.hgl.hglpicturebackend.manager.websocket.validator;

import cn.hutool.core.util.ObjUtil;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @ClassName: UserAuthValidator
 * @Package: com.hgl.hglpicturebackend.manager.websocket.validator
 * @Description: 用户认证校验器
 * @Author HGL
 * @Create: 2025/10/20 10:31
 */
@Slf4j
@Component
public class UserAuthValidator extends AbstractHandshakeValidator {

    @Resource
    private UserService userService;

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    protected boolean doValidate(HttpServletRequest request, Map<String, Object> attributes) {
        log.info("进入用户认证校验器:开始校验用户是否登录");
        User loginUser = userService.getLoginUser(request);
        if (ObjUtil.isEmpty(loginUser)) {
            log.error("用户未登录,拒绝握手");
            return false;
        }
        attributes.put("user", loginUser);
        attributes.put("userId", loginUser.getId());
        return true;
    }
}