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
public enum PictureReviewStatusEnum {

    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;
    private final Integer value;
    private static final Map<Integer, PictureReviewStatusEnum> MAP = new HashMap<>();

    PictureReviewStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    static {
        for (PictureReviewStatusEnum pictureReviewStatusEnum : PictureReviewStatusEnum.values()) {
            MAP.put(pictureReviewStatusEnum.getValue(), pictureReviewStatusEnum);
        }
    }

    /**
     * 根据 value 获取枚举
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static PictureReviewStatusEnum getPictureReviewStatusEnumByValue(Integer value) {
        return MAP.get(value);
    }
}
