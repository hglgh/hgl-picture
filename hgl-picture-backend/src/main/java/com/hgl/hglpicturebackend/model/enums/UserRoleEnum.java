package com.hgl.hglpicturebackend.model.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: UserRoleEnum
 * Package: com.hgl.hglpicturebackend.model.enums
 * Description: 用户角色枚举
 *
 * @Author HGL
 * @Create: 2024/12/10 21:48
 */
@Getter
public enum UserRoleEnum {
    USER("用户", "user"),
    VIP("VIP", "vip"),
    ADMIN("管理员", "admin");

    private final String text;
    private final String value;
    private static final Map<String, UserRoleEnum> MAP = new HashMap<>();

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    static {
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            MAP.put(userRoleEnum.getValue(), userRoleEnum);
        }
    }

    /**
     * 根据 value 获取枚举
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static UserRoleEnum getUserRoleEnumByValue(String value) {
        return MAP.get(value);
    }
}
