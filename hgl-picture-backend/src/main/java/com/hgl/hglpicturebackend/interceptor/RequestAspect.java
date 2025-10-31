package com.hgl.hglpicturebackend.interceptor;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName: RequestAspect
 * @Package: com.hgl.hglpicturebackend.interceptor
 * @Description:
 * @Author HGL
 * @Create: 2025/10/30 16:23
 */
@Slf4j
@Order(1)
@Aspect
@Component
public class RequestAspect {

    // ANSI 颜色代码定义（可在类中定义为常量）
    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";

    @Pointcut("execution(public * com.hgl.hglpicturebackend.controller.*.*(..))")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object requestLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        log.info(BLUE + "↓↓↓↓↓↓↓↓↓↓ 请求日志 ↓↓↓↓↓↓↓↓↓↓" + RESET);
        log.info(GREEN + "请求接口: [{}] {}" + RESET, request.getMethod(), request.getRequestURI());
        log.info(YELLOW + "请求方法: {}.{}" + RESET, joinPoint.getSignature().getDeclaringType().getSimpleName(), joinPoint.getSignature().getName());
        log.info(RED + "请求参数: {}" + RESET, JSONUtil.toJsonStr(filterArgs(joinPoint.getArgs())));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result = joinPoint.proceed();
        stopWatch.stop();
        long elapsedTime = stopWatch.getTotalTimeMillis();

        log.info(GREEN + "响应结果: {}" + RESET, JSONUtil.toJsonStr(result));
        log.info(YELLOW + "请求耗时: {} ms" + RESET, elapsedTime);
        log.info(BLUE + "↑↑↑↑↑↑↑↑↑↑ 请求日志 ↑↑↑↑↑↑↑↑↑↑" + RESET);

        return result;
    }

    private Object filterArgs(Object[] args) {
        return Arrays.stream(args).filter(arg ->
                !(arg instanceof MultipartFile)
                        && !(arg instanceof HttpServletRequest)
                        && (arg instanceof HttpServletResponse)
        ).collect(Collectors.toList());
    }
}
