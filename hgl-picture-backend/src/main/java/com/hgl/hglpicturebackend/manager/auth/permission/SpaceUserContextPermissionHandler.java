package com.hgl.hglpicturebackend.manager.auth.permission;

import com.hgl.hglpicturebackend.manager.auth.context.SpaceUserAuthContext;
import com.hgl.hglpicturebackend.model.entity.SpaceUser;
import com.hgl.hglpicturebackend.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName: SpaceUserContextPermissionHandler
 * @Package: com.hgl.hglpicturebackend.manager.auth
 * @Description: SpaceUser上下文处理器（高优先级）
 * @Author HGL
 * @Create: 2025/10/31 10:09
 */
@Component
public class SpaceUserContextPermissionHandler extends AbstractPermissionHandler {

    @Override
    public int getPriority() {
        // 高优先级
        return 2;
    }

    @Override
    public boolean supports(SpaceUserAuthContext authContext) {
        return authContext.getSpaceUser() != null;
    }

    @Override
    public List<String> handle(Object loginId, User loginUser, SpaceUserAuthContext authContext) {
        SpaceUser spaceUser = authContext.getSpaceUser();
        return spaceUserAuthManager.getPermissionListByRole(spaceUser.getSpaceRole());
    }
}
