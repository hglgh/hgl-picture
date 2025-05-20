package com.hgl.hglpicturebackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hgl.hglpicturebackend.manager.auth.constant.SpaceUserPermissionConstant;
import com.hgl.hglpicturebackend.manager.auth.model.SpaceUserAuthConfig;
import com.hgl.hglpicturebackend.manager.auth.model.SpaceUserRole;
import com.hgl.hglpicturebackend.model.entity.Space;
import com.hgl.hglpicturebackend.model.entity.SpaceUser;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.enums.SpaceRoleEnum;
import com.hgl.hglpicturebackend.model.enums.SpaceTypeEnum;
import com.hgl.hglpicturebackend.service.SpaceUserService;
import com.hgl.hglpicturebackend.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ClassName: SpaceUserAuthManager
 * Package: com.hgl.hglpicturebackend.manager.auth
 * Description:
 *
 * @Author HGL
 * @Create: 2025/4/17 15:50
 * @description 加载配置文件到对象，并提供根据角色获取权限列表的方法。
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        // 加载配置文件
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     */
    public List<String> getPermissionListByRole(String spaceUserRole) {
        if (StrUtil.isBlank(spaceUserRole)) {
            return new ArrayList<>();
        }

        // 找到匹配的角色
        SpaceUserRole userRole = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                .filter(role -> spaceUserRole.equals(role.getKey()))
                .findFirst().orElse(null);
        if (userRole == null) {
            return new ArrayList<>();
        }
        return userRole.getPermissions();
    }

    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSIONS = getPermissionListByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查询 SpaceUser 并获取角色和权限
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPermissionListByRole(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }

}
