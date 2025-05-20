package com.hgl.hglpicturebackend.interceptor;

import com.hgl.hglpicturebackend.annotation.AuthCheck;
import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.model.constant.UserConstant;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.enums.UserRoleEnum;
import com.hgl.hglpicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * ClassName: AuthInterceptor
 * Package: com.hgl.hglpicturebackend.interceptor
 * Description:
 *
 * @Author HGL
 * @Create: 2024/12/16 22:08
 */
@Component
@Aspect
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取注解的值
        String mustRole = authCheck.mustRole();

        // 获取当前请求
        //获取当前线程绑定的 RequestAttributes 对象，这个对象包含了当前请求的所有属性。
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        //将 RequestAttributes 强转为 ServletRequestAttributes，然后调用 getRequest() 方法获取当前请求的 HttpServletRequest 对象。
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustUserRoleEnum = UserRoleEnum.getUserRoleEnumByValue(mustRole);
        //不需要权限
        if (mustUserRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 以下开始：必须有该权限才通过
        //获取当前登录用户的角色
        UserRoleEnum userRoleEnum = UserRoleEnum.getUserRoleEnumByValue(loginUser.getUserRole());
        //没有权限，抛出异常
        if(userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 要求必须有管理员权限，但用户没有管理员权限，拒绝
        if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}
