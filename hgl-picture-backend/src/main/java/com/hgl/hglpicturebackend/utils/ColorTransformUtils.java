package com.hgl.hglpicturebackend.utils;

import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;

/**
 * 工具类：颜色转换工具类
 * 功能：将非标准的色值字符串（如 0xFF00、#abc）转换为标准 6 位 RGB 格式（如 0x00FF00）
 * 支持格式：0x、#、纯 hex 字符串
 * 返回格式：0xRRGGBB（全小写）
 *
 * @author 请别把我整破防
 */
public class ColorTransformUtils {

    private ColorTransformUtils() {
        // 工具类构造器私有化
    }

    /**
     * 标准化颜色值字符串，补足为 6 位十六进制 RGB 格式
     *
     * @param color 原始颜色字符串（可带 0x 或 # 前缀）
     * @return 标准颜色格式（如 0x00FF00）
     */
    public static String normalizeHexColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "颜色值不能为空");
        }

        // 去除前缀并转小写
        String cleanHex = color.trim().toLowerCase();
        if (cleanHex.startsWith("0x")) {
            cleanHex = cleanHex.substring(2);
        } else if (cleanHex.startsWith("#")) {
            cleanHex = cleanHex.substring(1);
        }

        // 校验是否为合法的十六进制字符
        if (!cleanHex.matches("^[0-9a-f]+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "颜色值包含非法字符，仅支持 0-9 和 a-f");
        }

        if (cleanHex.length() > 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "颜色值长度超过6位，无法识别");
        }

        // 补齐前导0到6位
        String paddedHex = String.format("%6s", cleanHex).replace(' ', '0');

        return "0x" + paddedHex;
    }

    public static void main(String[] args) {
        String[] testCases = {
                "0xFF00",
                "FF00",
                "#123",
                "0x80E0",
                "#abcdef",
                "abc",
                "000",
                "0x000000",
                "0x123456"
        };

        for (String test : testCases) {
            try {
                System.out.printf("输入: %-10s -> 输出: %s%n", test, normalizeHexColor(test));
            } catch (Exception e) {
                System.out.printf("输入: %-10s -> 异常: %s%n", test, e.getMessage());
            }
        }
    }
}
