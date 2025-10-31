package com.hgl.hglpicturebackend.manager.auth.permission;

import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.manager.auth.context.SpaceUserAuthContext;
import com.hgl.hglpicturebackend.model.entity.Space;
import com.hgl.hglpicturebackend.model.entity.SpaceUser;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.enums.SpaceRoleEnum;
import com.hgl.hglpicturebackend.model.enums.SpaceTypeEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: SpaceTypePermissionHandler
 * @Package: com.hgl.hglpicturebackend.manager.auth
 * @Description: Space类型处理器（最低优先级）
 * @Author HGL
 * @Create: 2025/10/31 10:11
 */
@Component
public class SpaceTypePermissionHandler extends AbstractPermissionHandler {

    @Override
    public int getPriority() {
        // 最低优先级
        return 5;
    }

    @Override
    public List<String> handle(Object loginId, User loginUser, SpaceUserAuthContext authContext) {
        Long spaceId = authContext.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            if (space == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间信息");
            }

            if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()) {
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return spaceUserAuthManager.getPermissionListByRole(SpaceRoleEnum.ADMIN.getValue());
                } else {
                    return new ArrayList<>();
                }
            } else {
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();

                if (spaceUser == null) {
                    return new ArrayList<>();
                }

                return spaceUserAuthManager.getPermissionListByRole(spaceUser.getSpaceRole());
            }
        }
        // spaceId为null时，当前处理器无法处理，返回null
        return null;
    }
}

