package com.hgl.hglpicturebackend.interceptor;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hgl.hglpicturebackend.common.AnsiColor;
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
import java.util.*;
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

    // 定义需要脱敏的字段名
    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList("userPassword", "password", "passwd"));
    // 定义脱敏后的替换字符串
    private static final String DESENSITIZED_VALUE = "******";

    @Pointcut("execution(public * com.hgl.hglpicturebackend.controller.*.*(..))")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object requestLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        log.info("{}↓↓↓↓↓↓↓↓↓↓ 请求日志 ↓↓↓↓↓↓↓↓↓↓{}", AnsiColor.BLUE, AnsiColor.RESET);
        log.info("{}请求接口: [{}] {}{}", AnsiColor.GREEN.getCode(), request.getMethod(), request.getRequestURI(), AnsiColor.RESET.getCode());
        log.info("{}请求方法: {}.{}{}", AnsiColor.YELLOW.getCode(), joinPoint.getSignature().getDeclaringType().getSimpleName(), joinPoint.getSignature().getName(), AnsiColor.RESET.getCode());
        log.info("{}请求参数: {}{}", AnsiColor.RED.getCode(), JSONUtil.toJsonStr(processAndFilterArgs(joinPoint.getArgs())), AnsiColor.RESET.getCode());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result = joinPoint.proceed();
        stopWatch.stop();
        long elapsedTime = stopWatch.getTotalTimeMillis();

        log.info("{}响应结果: {}{}", AnsiColor.GREEN.getCode(), JSONUtil.toJsonStr(result), AnsiColor.RESET.getCode());
        log.info("{}请求耗时: {} ms{}", AnsiColor.YELLOW.getCode(), elapsedTime, AnsiColor.RESET.getCode());
        log.info("{}↑↑↑↑↑↑↑↑↑↑ 请求日志 ↑↑↑↑↑↑↑↑↑↑{}", AnsiColor.BLUE.getCode(), AnsiColor.RESET.getCode());

        return result;
    }

    /**
     * 处理并过滤请求参数，进行脱敏
     *
     * @param args 原始参数数组
     * @return 处理后的参数数组
     */
    private Object processAndFilterArgs(Object[] args) {
        return Arrays.stream(args).filter(arg ->
                        !(arg instanceof MultipartFile)
                                && !(arg instanceof HttpServletResponse)
                )
                // 在过滤后，对每个参数进行脱敏处理
                .map(this::desensitizeObject)
                .collect(Collectors.toList());
    }

    /**
     * 递归地对对象进行脱敏处理
     *
     * @param obj 待处理的对象
     * @return 脱敏后的对象
     */
    private Object desensitizeObject(Object obj) {
        if (obj == null) {
            return null;
        }

        // 根据对象类型分发到不同的处理方法
        if (obj instanceof Map) {
            return desensitizeMap((Map<?, ?>) obj);
        }

        if (obj instanceof String) {
            return desensitizeString((String) obj);
        }

/*        if (obj instanceof JSONObject) {
            return desensitizeJsonObject((JSONObject) obj);
        }*/

        // 处理其他类型对象
        return desensitizeOtherObject(obj);
    }

    /**
     * 处理 Map 类型对象的脱敏
     */
    private Object desensitizeMap(Map<?, ?> map) {
        // 使用LinkedHashMap保持顺序
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, value) -> {
            String keyStr = String.valueOf(key);
            // 检查 key 是否为敏感字段
            if (SENSITIVE_FIELDS.contains(keyStr)) {
                result.put(keyStr, DESENSITIZED_VALUE);
            } else {
                // 如果不是，递归处理 value，以处理嵌套结构
                result.put(keyStr, desensitizeObject(value));
            }
        });
        return result;
    }

    /**
     * 处理 JSON 字符串类型的脱敏
     */
    private Object desensitizeString(String str) {
        try {
            // 尝试将字符串解析为 JSONObject
            JSONObject jsonObject = JSONUtil.parseObj(str);
            return desensitizeObject(jsonObject);
        } catch (Exception e) {
            return str;
        }
    }

    /**
     * 处理 JSONObject 类型的脱敏
     */
    private Object desensitizeJsonObject(JSONObject jsonObject) {
        Set<String> keys = jsonObject.keySet();
        keys.forEach(key -> {
            if (SENSITIVE_FIELDS.contains(key)) {
                jsonObject.set(key, DESENSITIZED_VALUE);
            } else {
                Object value = jsonObject.get(key);
                jsonObject.set(key, desensitizeObject(value));
            }
        });
        return jsonObject;
    }

    /**
     * 处理其他类型对象的脱敏
     */
    private Object desensitizeOtherObject(Object obj) {
        try {
            String jsonStr = JSONUtil.toJsonStr(obj);
            Map<String, Object> map = JSONUtil.toBean(jsonStr, Map.class, false);
            return desensitizeObject(map);
        } catch (Exception e) {
            return obj;
        }
    }

}
