package com.hgl.hglpicturebackend.manager.auth.permission;

import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.manager.auth.context.SpaceUserAuthContext;
import com.hgl.hglpicturebackend.model.entity.SpaceUser;
import com.hgl.hglpicturebackend.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: SpaceUserIdPermissionHandler
 * @Package: com.hgl.hglpicturebackend.manager.auth
 * @Description: SpaceUserId处理器（中等优先级）
 * @Author HGL
 * @Create: 2025/10/31 10:09
 */
@Component
public class SpaceUserIdPermissionHandler extends AbstractPermissionHandler {

    @Override
    public int getPriority() {
        // 中等优先级
        return 3;
    }

    @Override
    public boolean supports(SpaceUserAuthContext authContext) {
        return authContext.getSpaceUserId() != null;
    }

    @Override
    public List<String> handle(Object loginId, User loginUser, SpaceUserAuthContext authContext) {
        Long spaceUserId = authContext.getSpaceUserId();
        SpaceUser spaceUser = spaceUserService.getById(spaceUserId);
        if (spaceUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间用户信息");
        }

        SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                .eq(SpaceUser::getUserId, loginUser.getId())
                .one();

        if (loginSpaceUser == null) {
            return new ArrayList<>();
        }

        return spaceUserAuthManager.getPermissionListByRole(loginSpaceUser.getSpaceRole());
    }
}
