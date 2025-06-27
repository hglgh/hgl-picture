package com.hgl.hglpicturebackend.manager.websocket.strategy;


import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ClassName MessageStrategyFactory
 * @Author 请别把我整破防
 * @Description 策略工厂类，基于 Map 自动装配不同策略
 * @Date 2025/6/27 11:15
 */
@Component
public class MessageStrategyFactory {
    @Resource
    private Map<String, MessageHandleStrategy> strategyMap;

    public MessageHandleStrategy getStrategy(String type) {
        if (type == null) {
            return strategyMap.get("ERROR");
        }
        if (strategyMap.get(type) == null) {
            throw new RuntimeException("未找到对应的策略");
        }
        return strategyMap.get(type);
    }
}
