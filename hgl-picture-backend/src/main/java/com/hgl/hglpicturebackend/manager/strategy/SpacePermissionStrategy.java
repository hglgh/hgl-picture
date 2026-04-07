package com.hgl.hglpicturebackend.manager.strategy;

import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.manager.auth.StpKit;
import com.hgl.hglpicturebackend.manager.auth.context.SpaceUserAuthContext;
import com.hgl.hglpicturebackend.manager.auth.permission.PermissionHandlerManager;
import com.hgl.hglpicturebackend.model.entity.User;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.hgl.hglpicturebackend.model.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @ClassName: SpacePermissionStrategy
 * @Package: com.hgl.hglpicturebackend.manager.strategy
 * @Description:
 * @Author HGL
 * @Create: 2026/4/7 9:58
 */
@Component
public class SpacePermissionStrategy implements PermissionStrategy {
    @Resource
    private PermissionHandlerManager handlerManager;

    @Override
    public String getType() {
        return StpKit.SPACE_TYPE;
    }

    @Override
    public List<String> getPermissionList(Object loginId, SpaceUserAuthContext context) {
        // 这里放原本写 in StpInterfaceByChainImpl 的核心逻辑
        User loginUser = getCurrentUser(loginId);
        return handlerManager.handlePermission(loginId, loginUser, context);
    }

    private User getCurrentUser(Object loginId) {
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户未登录");
        }
        return loginUser;
    }
}
