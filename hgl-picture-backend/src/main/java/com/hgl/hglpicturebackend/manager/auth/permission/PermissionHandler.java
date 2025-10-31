package com.hgl.hglpicturebackend.manager.auth.permission;

import com.hgl.hglpicturebackend.manager.auth.context.SpaceUserAuthContext;
import com.hgl.hglpicturebackend.model.entity.User;

import java.util.List;

/**
 * @ClassName: PermissionHandler
 * @Package: com.hgl.hglpicturebackend.manager.auth
 * @Description:
 * @Author HGL
 * @Create: 2025/10/31 10:05
 */
public interface PermissionHandler {
    /**
     * 处理权限验证
     * @param loginId 登录ID
     * @param loginUser 登录用户
     * @param authContext 权限上下文
     * @return 权限列表，如果无法处理则返回null
     */
    List<String> handle(Object loginId, User loginUser, SpaceUserAuthContext authContext);

    /**
     * 获取处理器优先级，数值越小优先级越高
     * @return 优先级
     */
    int getPriority();

    /**
     * 判断是否支持处理当前上下文
     * @param authContext 权限上下文
     * @return 是否支持
     */
    boolean supports(SpaceUserAuthContext authContext);
}
