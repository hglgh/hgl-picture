package com.hgl.hglpicturebackend.manager.strategy;

import com.hgl.hglpicturebackend.manager.auth.context.SpaceUserAuthContext;

import java.util.List;

/**
 * @ClassName: PermissionStrategy
 * @Package: com.hgl.hglpicturebackend.manager.strategy
 * @Description:
 * @Author HGL
 * @Create: 2026/4/7 9:57
 */
public interface PermissionStrategy {
    String getType(); // 返回对应的 loginType，如 "space"
    List<String> getPermissionList(Object loginId, SpaceUserAuthContext context);
}

