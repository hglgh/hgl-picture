package com.hgl.hglpicturebackend.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.manager.auth.constant.SpaceUserPermissionConstant;
import com.hgl.hglpicturebackend.manager.auth.context.SpaceUserAuthContext;
import com.hgl.hglpicturebackend.model.entity.Picture;
import com.hgl.hglpicturebackend.model.entity.Space;
import com.hgl.hglpicturebackend.model.entity.SpaceUser;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.enums.SpaceRoleEnum;
import com.hgl.hglpicturebackend.model.enums.SpaceTypeEnum;
import com.hgl.hglpicturebackend.service.PictureService;
import com.hgl.hglpicturebackend.service.SpaceService;
import com.hgl.hglpicturebackend.service.SpaceUserService;
import com.hgl.hglpicturebackend.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.hgl.hglpicturebackend.model.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 自定义权限加载接口实现类(优化版)
 *
 * @author 请别把我整破防
 */
@Deprecated
//@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class TuneStpInterfaceImpl implements StpInterface {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 返回一个账号所拥有的权限码集合
     * <p>
     * 这段代码的主要功能是根据用户登录信息和上下文环境，动态计算并返回用户的权限列表。以下是具体逻辑的通俗解释：
     * <ol>
     *     <li>判断登录类型：如果 loginType 不是 "space"（空间类型），直接返回空权限列表。</li>
     *     <li>默认管理员权限：获取管理员的所有权限作为默认通过权限。</li>
     *     <li>解析上下文：从请求中提取上下文信息（如图片、空间、用户等信息）。如果上下文中所有字段都为空，则认为是公共图库访问，直接返回管理员权限。</li>
     *     <li>验证用户登录：检查当前用户是否已登录，若未登录则抛出异常。</li>
     *     <li>优先级处理：
     *         <ul>
     *             <li>如果上下文中存在 SpaceUser 对象，直接根据其角色返回权限。</li>
     *             <li>如果存在 spaceUserId，查询对应的 SpaceUser 并进一步判断当前用户是否有权限。</li>
     *         </ul>
     *     </li>
     *     <li>空间或图片判断：
     *         <ul>
     *             <li>如果没有 spaceId，尝试通过 pictureId 获取图片信息，并根据图片所属空间或用户角色返回权限。</li>
     *             <li>如果图片属于公共图库，仅本人或管理员可操作；否则仅允许查看。</li>
     *         </ul>
     *     </li>
     *     <li>空间类型判断：
     *         <ul>
     *             <li>如果是私有空间，仅本人或管理员有权限。</li>
     *             <li>如果是团队空间，查询当前用户在该空间的角色并返回对应权限。</li>
     *         </ul>
     *     </li>
     * </ol>
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 1. 判断是否是空间类型的登录请求，如果不是直接返回空权限列表
        if (!isSpaceLoginType(loginType)) {
            return new ArrayList<>();
        }

        // 2. 获取管理员权限作为默认权限（用于公共图库等场景）
        List<String> ADMIN_PERMISSIONS = getDefaultPermissions();

        // 3. 获取当前请求的上下文信息（可能包含图片、空间、用户等信息）
        SpaceUserAuthContext authContext = getAuthContextByRequest();

        // 4. 如果上下文中所有字段都为空，表示访问的是公共图库，直接返回管理员权限
        if (isAllFieldsNull(authContext)) {
            return ADMIN_PERMISSIONS;
        }

        // 5. 获取当前用户的 ID（如果未登录会抛出异常）
        Long userId = getCurrentUserId(loginId);

        // 6. 尝试从上下文中获取 SpaceUser 对象
        SpaceUser spaceUser = getSpaceUserFromContext(authContext);

        // 7. 如果存在 SpaceUser，则根据其角色直接返回权限
        if (spaceUser != null) {
            return handleSpaceUserInContext(spaceUser);
        }

        // 8. 如果上下文中存在 spaceUserId，则查询该用户在对应空间的角色并返回权限
        Long spaceUserId = authContext.getSpaceUserId();
        if (spaceUserId != null) {
            return handleSpaceUserIdInContext(spaceUserId, userId);
        }

        // 9. 如果没有 spaceUserId，尝试通过 pictureId 或 spaceId 获取空间信息并判断权限
        return handleSpaceIdOrPictureId(authContext, userId, ADMIN_PERMISSIONS);
    }

    /**
     * 判断登录类型是否为 "space"。
     */
    private boolean isSpaceLoginType(String loginType) {
        return StpKit.SPACE_TYPE.equals(loginType);
    }

    /**
     * 获取管理员权限列表，作为默认通过权限。
     */
    private List<String> getDefaultPermissions() {
        return spaceUserAuthManager.getPermissionListByRole(SpaceRoleEnum.ADMIN.getValue());
    }

    /**
     * 获取当前登录用户的 ID。
     * 如果用户未登录，抛出业务异常。
     */
    @NotNull
    private Long getCurrentUserId(Object loginId) {
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户未登录");
        }
        return loginUser.getId();
    }

    /**
     * 从上下文中获取 SpaceUser 对象。
     */
    private SpaceUser getSpaceUserFromContext(SpaceUserAuthContext authContext) {
        return authContext.getSpaceUser();
    }

    /**
     * 处理上下文中的 SpaceUser，根据其角色返回对应的权限。
     */
    private List<String> handleSpaceUserInContext(SpaceUser spaceUser) {
        return spaceUserAuthManager.getPermissionListByRole(spaceUser.getSpaceRole());
    }

    /**
     * 处理上下文中的 spaceUserId，查询该用户在对应空间的角色并返回权限。
     */
    private List<String> handleSpaceUserIdInContext(Long spaceUserId, Long userId) {
        // 如果有 spaceUserId，必然是团队空间，通过数据库查询 SpaceUser 对象
        SpaceUser spaceUser = spaceUserService.getById(spaceUserId);
        if (spaceUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间用户信息");
        }

        // 取出当前登录用户对应的 spaceUser
        SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                .eq(SpaceUser::getUserId, userId)
                .one();

        if (loginSpaceUser == null) {
            return new ArrayList<>();
        }

        // 这里会导致管理员在私有空间没有权限，可以再查一次库处理
        return spaceUserAuthManager.getPermissionListByRole(loginSpaceUser.getSpaceRole());
    }

    /**
     * 处理 spaceId 或 pictureId，获取空间信息并判断权限。
     */
    private List<String> handleSpaceIdOrPictureId(SpaceUserAuthContext authContext, Long userId, List<String> defaultPermissions) {
        Long spaceId = authContext.getSpaceId();
        if (spaceId == null) {
            // 如果没有 spaceId，通过 pictureId 获取 Picture 对象和 Space 对象
            Long pictureId = authContext.getPictureId();
            // 图片 id 也没有，则默认通过权限校验
            if (pictureId == null) {
                return defaultPermissions;
            }

            Picture picture = pictureService.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .select(Picture::getId, Picture::getSpaceId, Picture::getUserId)
                    .one();

            if (picture == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到图片信息");
            }

            spaceId = picture.getSpaceId();
            // 公共图库，仅本人或管理员可操作
            if (spaceId == null) {
                User loginUser = userService.getById(userId);
                if (picture.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
                    return defaultPermissions;
                } else {
                    // 不是自己的图片，仅可查看
                    return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
                }
            }
        }

        // 获取 Space 对象
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间信息");
        }

        return determinePermissionsBySpaceType(space, userId);
    }

    /**
     * 根据空间类型判断权限：
     * - 私有空间：仅本人或管理员有全部权限
     * - 团队空间：根据用户在该空间的角色返回权限
     */
    private List<String> determinePermissionsBySpaceType(Space space, Long userId) {
        if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()) {
            // 私有空间，仅本人或管理员有权限
            User loginUser = userService.getById(userId);
            if (space.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
                return spaceUserAuthManager.getPermissionListByRole(SpaceRoleEnum.ADMIN.getValue());
            } else {
                return new ArrayList<>();
            }
        } else {
            // 团队空间，查询 SpaceUser 并获取角色和权限
            SpaceUser spaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, space.getId())
                    .eq(SpaceUser::getUserId, userId)
                    .one();

            if (spaceUser == null) {
                return new ArrayList<>();
            }

            return spaceUserAuthManager.getPermissionListByRole(spaceUser.getSpaceRole());
        }
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
//            String prefix = requestURI.split("/")[0];
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

    /**
     * 判断对象的所有字段是否为空
     */
    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            // 对象本身为空
            return true;
        }
        // 获取所有字段并判断是否所有字段都为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                // 获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                // 检查是否所有字段都为空
                .allMatch(ObjectUtil::isEmpty);
    }

}
