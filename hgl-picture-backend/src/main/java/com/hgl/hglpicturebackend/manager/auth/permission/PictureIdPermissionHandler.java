package com.hgl.hglpicturebackend.manager.auth.permission;

import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.manager.auth.constant.SpaceUserPermissionConstant;
import com.hgl.hglpicturebackend.manager.auth.context.SpaceUserAuthContext;
import com.hgl.hglpicturebackend.model.entity.Picture;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.enums.SpaceRoleEnum;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @ClassName: PictureIdPermissionHandler
 * @Package: com.hgl.hglpicturebackend.manager.auth
 * @Description: PictureId处理器（较低优先级）
 * @Author HGL
 * @Create: 2025/10/31 10:10
 */
@Component
public class PictureIdPermissionHandler extends AbstractPermissionHandler {

    @Override
    public int getPriority() {
        // 较低优先级
        return 4;
    }

    @Override
    public boolean supports(SpaceUserAuthContext authContext) {
        return authContext.getSpaceId() == null && authContext.getPictureId() != null;
    }

    @Override
    public List<String> handle(Object loginId, User loginUser, SpaceUserAuthContext authContext) {
        Long pictureId = authContext.getPictureId();
        if (pictureId == null) {
            return spaceUserAuthManager.getPermissionListByRole(SpaceRoleEnum.ADMIN.getValue());
        }

        Picture picture = pictureService.lambdaQuery()
                .eq(Picture::getId, pictureId)
                .select(Picture::getId, Picture::getSpaceId, Picture::getUserId)
                .one();

        if (picture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到图片信息");
        }

        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 图片属于公共图库的逻辑不变
            if (picture.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                return spaceUserAuthManager.getPermissionListByRole(SpaceRoleEnum.ADMIN.getValue());
            } else {
                return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
            }
        } else {
            // *** 关键修改：将找到的 spaceId 设置回上下文，供后续处理器使用 ***
            authContext.setSpaceId(spaceId);
            // 返回 null，表示让下一个处理器来处理这个（已被 enrich 的）上下文
            return null;
        }
    }
}
