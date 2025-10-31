package com.hgl.hglpicturebackend.manager.auth.permission;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.hgl.hglpicturebackend.manager.auth.context.SpaceUserAuthContext;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.enums.SpaceRoleEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: PublicGalleryPermissionHandler
 * @Package: com.hgl.hglpicturebackend.manager.auth
 * @Description: 公共图库处理器（最高优先级）
 * @Author HGL
 * @Create: 2025/10/31 10:07
 */
@Component
public class PublicGalleryPermissionHandler extends AbstractPermissionHandler {

    @Override
    public int getPriority() {
        // 最高优先级
        return 1;
    }

    @Override
    public boolean supports(SpaceUserAuthContext authContext) {
        return isAllFieldsNull(authContext);
    }

    @Override
    public List<String> handle(Object loginId, User loginUser, SpaceUserAuthContext authContext) {
        return spaceUserAuthManager.getPermissionListByRole(SpaceRoleEnum.ADMIN.getValue());
    }

    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            return true;
        }
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                .map(field -> ReflectUtil.getFieldValue(object, field))
                .allMatch(ObjectUtil::isEmpty);
    }
}
