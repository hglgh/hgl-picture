package com.hgl.hglpicturebackend.utils;

/**
 * @ClassName: ConsoleColorUtil
 * @Package: com.hgl.hglpicturebackend.utils
 * @Description: 控制台颜色工具类
 * @Author HGL
 * @Create: 2025/9/16 13:05
 */
public class ConsoleColorUtil {
    /*
        前景色:
            重置: \033[0m
            黑色: \033[30m
            红色: \033[31m
            绿色: \033[32m
            黄色: \033[33m
            蓝色: \033[34m
            洋红色: \033[35m
            青色: \033[36m
            白色: \033[37m
     */
    public static final String RESET = "\033[0m";
    public static final String BLACK = "\033[30m";
    public static final String RED = "\033[31m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String BLUE = "\033[34m";
    public static final String MAGENTA = "\033[35m";
    public static final String CYAN = "\033[36m";
    public static final String WHITE = "\033[37m";

    /*
        背景色:
            黑色背景: \033[40m
            红色背景: \033[41m
            绿色背景: \033[42m
            黄色背景: \033[43m
            蓝色背景: \033[44m
            洋红色背景: \033[45m
            青色背景: \033[46m
            白色背景: \033[47m
     */
    public static final String BLACK_BACKGROUND = "\033[40m";
    public static final String RED_BACKGROUND = "\033[41m";
    public static final String GREEN_BACKGROUND = "\033[42m";
    public static final String YELLOW_BACKGROUND = "\033[43m";
    public static final String BLUE_BACKGROUND = "\033[44m";
    public static final String MAGENTA_BACKGROUND = "\033[45m";
    public static final String CYAN_BACKGROUND = "\033[46m";
    public static final String WHITE_BACKGROUND = "\033[47m";

    /*
        格式:
            粗体: \033[1m
            下划线: \033[4m
            闪烁: \033[5m
            反显: \033[7m
     */
    public static final String BOLD = "\033[1m";
    public static final String UNDERLINE = "\033[4m";
    public static final String BLINK = "\033[5m";
    public static final String REVERSE = "\033[7m";

    public static void main(String[] args) {
        System.out.println(RED + "这是红色文字" + RESET);
        System.out.println(GREEN + "这是绿色文字" + RESET);
        System.out.println(BLUE + BOLD + "这是蓝色粗体文字" + RESET);
        System.out.println(YELLOW_BACKGROUND + BLACK + "黄底黑字" + RESET);
        System.out.println(RED + UNDERLINE + "红色下划线文字" + RESET);

    }
}
