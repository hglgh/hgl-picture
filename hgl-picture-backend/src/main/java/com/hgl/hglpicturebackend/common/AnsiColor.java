package com.hgl.hglpicturebackend.common;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @ClassName: AnsiColor
 * @Package: com.hgl.hglpicturebackend.common
 * @Description: ANSI 颜色代码枚举，用于在支持 ANSI 的控制台（如 IntelliJ IDEA, VS Code）中显示彩色日志
 * @Author HGL
 * @Create: 2025/12/10 14:29
 */

@Getter
@RequiredArgsConstructor
public enum AnsiColor {
    RESET("\u001B[0m", "重置"),
    BLUE_BOLD("\u001B[34;1m", "蓝色加粗"),
    GREEN("\u001B[32m", "绿色"),
    YELLOW("\u001B[33m", "黄色"),
    CYAN("\u001B[36m", "青色"),
    RED("\u001B[31m", "红色"),
    BLUE("\u001B[34m", "蓝色"),
    MAGENTA("\u001B[35m", "品红色"),
    BLACK("\u001B[30m", "黑色"),
    WHITE("\u001B[37m", "白色");

    private final String code;
    private final String desc;
}
