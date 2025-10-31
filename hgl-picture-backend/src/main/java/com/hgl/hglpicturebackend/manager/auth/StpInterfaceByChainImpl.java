package com.hgl.hglpicturebackend.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.manager.auth.context.SpaceUserAuthContext;
import com.hgl.hglpicturebackend.manager.auth.permission.PermissionHandlerManager;
import com.hgl.hglpicturebackend.model.entity.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hgl.hglpicturebackend.model.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @ClassName: StpInterfaceByChainImpl
 * @Package: com.hgl.hglpicturebackend.manager.auth
 * @Description: 自定义权限加载接口实现类(责任链优化版)
 * @Author HGL
 * @Create: 2025/10/31 10:39
 */

@Component
public class StpInterfaceByChainImpl implements StpInterface {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private PermissionHandlerManager permissionHandlerManager;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 1. 判断是否是空间类型的登录请求，如果不是直接返回空权限列表
        if (!isSpaceLoginType(loginType)) {
            return new ArrayList<>();
        }

        // 2. 获取当前请求的上下文信息
        SpaceUserAuthContext authContext = getAuthContextByRequest();

        // 3. 获取当前用户
        User loginUser = getCurrentUser(loginId);

        // 4. 通过责任链处理权限验证
        return permissionHandlerManager.handlePermission(loginId, loginUser, authContext);
    }

    /**
     * 判断登录类型是否为 "space"。
     */
    private boolean isSpaceLoginType(String loginType) {
        return StpKit.SPACE_TYPE.equals(loginType);
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser(Object loginId) {
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户未登录");
        }
        return loginUser;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验,本项目未使用)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 从请求中获取上下文对象
     */
    @NotNull
    private SpaceUserAuthContext getAuthContextByRequest() {
        SpaceUserAuthContext authRequest;
        // 从请求中获取上下文对象
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        // 将 RequestAttributes 强转为 ServletRequestAttributes，然后调用 getRequest() 方法获取当前请求的 HttpServletRequest 对象。
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        // 获取请求头中的 Content-Type
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        //获取请求参数
        // 判断请求头是否为 JSON, 如果是则从请求体中获取请求参数(post请求)
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = ServletUtil.getBody(request);
            authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {
            // 如果是 GET 请求，则从请求参数中获取请求参数
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }
        //根据请求路径区分 id 字段的含义
        Long id = authRequest.getId();
        if (ObjUtil.isNotNull(id)) {
            //获取到完整的请求路径
            String requestURI = request.getRequestURI();
            //将contextPath 替换为空
            requestURI = requestURI.replace(contextPath + "/", "");
            //获取前缀的第一个/前的字符串
            String moduleName = StrUtil.subBefore(requestURI, "/", false);
            switch (moduleName) {
                case "space":
                    authRequest.setSpaceId(id);
                    break;
                case "spaceUser":
                    authRequest.setSpaceUserId(id);
                    break;
                case "picture":
                    authRequest.setPictureId(id);
                    break;
            }
        }
        return authRequest;
    }
}
