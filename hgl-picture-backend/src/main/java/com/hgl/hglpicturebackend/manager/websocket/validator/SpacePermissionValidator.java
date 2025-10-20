package com.hgl.hglpicturebackend.manager.websocket.validator;

import cn.hutool.core.util.ObjUtil;
import com.hgl.hglpicturebackend.manager.auth.SpaceUserAuthManager;
import com.hgl.hglpicturebackend.manager.auth.constant.SpaceUserPermissionConstant;
import com.hgl.hglpicturebackend.model.entity.Picture;
import com.hgl.hglpicturebackend.model.entity.Space;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.enums.SpaceTypeEnum;
import com.hgl.hglpicturebackend.service.SpaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SpacePermissionValidator
 * @Package: com.hgl.hglpicturebackend.manager.websocket.validator
 * @Description: 空间权限校验器
 * @Author HGL
 * @Create: 2025/10/20 10:33
 */
@Slf4j
@Component
public class SpacePermissionValidator extends AbstractHandshakeValidator {

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;


    @Override
    public int getOrder() {
        // 最低优先级
        return 4;
    }

    @Override
    protected boolean doValidate(HttpServletRequest request, Map<String, Object> attributes) {
        log.info("进入空间权限校验器:开始校验空间权限");
        // 从上下文获取
        Picture picture = (Picture) attributes.get("picture");

        if (ObjUtil.isEmpty(picture)) {
            // 这是一个防御性编程，理论上不应该发生，因为顺序是固定的
            log.error("上下文中未找到图片信息，校验顺序可能异常");
            return false;
        }

        Long spaceId = picture.getSpaceId();

        // 如果没有空间ID，则跳过空间校验
        if (ObjUtil.isEmpty(spaceId)) {
            return true;
        }

        Space space = spaceService.getById(spaceId);
        if (ObjUtil.isEmpty(space)) {
            log.error("空间不存在,拒绝握手");
            return false;
        }

        if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
            log.error("图片所在空间不是团队空间,拒绝握手");
            return false;
        }

        User loginUser = (User) attributes.get("user");
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        if (ObjUtil.isEmpty(permissionList) || !permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
            log.error("用户没有编辑当前图片的权限,拒绝握手");
            return false;
        }

        return true;
    }
}