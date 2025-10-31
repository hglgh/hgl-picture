package com.hgl.hglpicturebackend.manager.auth.permission;

import com.hgl.hglpicturebackend.manager.auth.context.SpaceUserAuthContext;
import com.hgl.hglpicturebackend.model.entity.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @ClassName: PermissionHandlerManager
 * @Package: com.hgl.hglpicturebackend.manager.auth
 * @Description:
 * @Author HGL
 * @Create: 2025/10/31 10:12
 */
@Component
public class PermissionHandlerManager {

    @Resource
    private List<PermissionHandler> permissionHandlers;

    @PostConstruct
    public void init() {
        // 按优先级排序
        permissionHandlers.sort(Comparator.comparingInt(PermissionHandler::getPriority));
    }

    public List<String> handlePermission(Object loginId, User loginUser, SpaceUserAuthContext authContext) {
        for (PermissionHandler handler : permissionHandlers) {
            if (handler.supports(authContext)) {
                List<String> permissions = handler.handle(loginId, loginUser, authContext);
                if (permissions != null) {
                    return permissions;
                }
            }
        }
        // 默认返回空权限列表
        return new ArrayList<>();
    }
}

