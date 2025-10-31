package com.hgl.hglpicturebackend.manager.auth.permission;

import com.hgl.hglpicturebackend.manager.auth.SpaceUserAuthManager;
import com.hgl.hglpicturebackend.manager.auth.context.SpaceUserAuthContext;
import com.hgl.hglpicturebackend.service.PictureService;
import com.hgl.hglpicturebackend.service.SpaceService;
import com.hgl.hglpicturebackend.service.SpaceUserService;
import com.hgl.hglpicturebackend.service.UserService;

import javax.annotation.Resource;

/**
 * @ClassName: AbstractPermissionHandler
 * @Package: com.hgl.hglpicturebackend.manager.auth
 * @Description: 抽象权限处理器基类
 * @Author HGL
 * @Create: 2025/10/31 10:06
 */

public abstract class AbstractPermissionHandler implements PermissionHandler {
    @Resource
    protected SpaceUserAuthManager spaceUserAuthManager;

    @Resource
    protected SpaceUserService spaceUserService;

    @Resource
    protected UserService userService;

    @Resource
    protected SpaceService spaceService;

    @Resource
    protected PictureService pictureService;

    @Override
    public boolean supports(SpaceUserAuthContext authContext) {
        // 默认支持所有上下文
        return true;
    }
}

