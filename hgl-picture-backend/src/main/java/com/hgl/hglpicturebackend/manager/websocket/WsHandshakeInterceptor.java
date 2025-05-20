package com.hgl.hglpicturebackend.manager.websocket;


import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.hgl.hglpicturebackend.manager.auth.SpaceUserAuthManager;
import com.hgl.hglpicturebackend.manager.auth.constant.SpaceUserPermissionConstant;
import com.hgl.hglpicturebackend.model.entity.Picture;
import com.hgl.hglpicturebackend.model.entity.Space;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.enums.SpaceTypeEnum;
import com.hgl.hglpicturebackend.service.PictureService;
import com.hgl.hglpicturebackend.service.SpaceService;
import com.hgl.hglpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * ClassName: WsHandshakeInterceptor
 * Package: com.hgl.hglpicturebackend.manager.websocket
 * Description:
 *
 * @Author HGL
 * @Create: 2025/4/22 15:01
 * @description websocket拦截器, 建立连接前要校验权限
 */
@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 握手前校验权限
     * @param request 请求
     * @param response 响应
     * @param wsHandler 处理器
     * @param attributes 给WebSocketSession会话设置属性
     * @return boolean
     * @throws Exception 抛出异常
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        //获取到当前用户
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            //从请求中获取参数
            String pictureId = httpServletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.error("缺失图片参数,拒绝握手");
                return false;
            }
            User loginUser = userService.getLoginUser(httpServletRequest);
            if (ObjUtil.isEmpty(loginUser)) {
                log.error("用户未登录,拒绝握手");
                return false;
            }
            //校验用户是否有编辑当前图片的权限
            Picture picture = pictureService.getById(pictureId);
            if (ObjUtil.isEmpty(picture)) {
                log.error("图片不存在,拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space = null;
            //如果是团队空间,并且有编辑者权限,才能建立连接
            if (ObjUtil.isNotEmpty(spaceId)) {
                space = spaceService.getById(spaceId);
                if (ObjUtil.isEmpty(space)) {
                    log.error("空间不存在,拒绝握手");
                    return false;
                }
                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
                    log.error("图片所在空间不是团队空间,拒绝握手");
                    return false;
                }
            }
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            if (ObjUtil.isEmpty(permissionList) || !permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                log.error("用户没有编辑当前图片的权限,拒绝握手");
                return false;
            }
            //设置用户登录信息等属性到websocketSession中
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId));
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
