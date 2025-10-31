package com.hgl.hglpicturebackend.manager.websocket.validator;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName: HandshakeValidatorFactory
 * @Package: com.hgl.hglpicturebackend.manager.websocket.validator
 * @Description: WebSocket握手校验器工厂类;负责组装所有校验器形成责任链
 * @Author HGL
 * @Create: 2025/10/20 10:34
 */
@Slf4j
@Getter
@Component
public class HandshakeValidatorFactory {
    @Resource
    private List<HandshakeValidator> validators;

    /**
     * -- GETTER --
     * 获取校验器责任链头节点
     *
     */
    private HandshakeValidator validatorChain;

    @PostConstruct
    public void initValidatorChain() {
        if (CollectionUtil.isEmpty(validators)) {
            validatorChain = null;
            return;
        }
        // 1. 先排序
        List<HandshakeValidator> sortedValidators = validators.stream()
                .sorted(Comparator.comparingInt(HandshakeValidator::getOrder))
                .collect(Collectors.toList());

        // 2. 构建链条
        // 头节点
        this.validatorChain = sortedValidators.get(0);
        // 用于移动的指针，初始指向头
        HandshakeValidator current = this.validatorChain;
        for (int i = 1; i < sortedValidators.size(); i++) {
            HandshakeValidator next = sortedValidators.get(i);
            // 将当前节点的下一个设置为新节点
            current.setNext(next);
            // 指针向后移动
            current = next;
        }
        log.info("初始化WebSocket握手校验器责任链成功！链头: {}", validatorChain.getClass().getSimpleName());
    }

}
