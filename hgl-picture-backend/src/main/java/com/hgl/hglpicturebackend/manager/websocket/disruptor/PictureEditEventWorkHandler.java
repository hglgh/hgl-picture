package com.hgl.hglpicturebackend.manager.websocket.disruptor;

import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.hgl.hglpicturebackend.manager.websocket.strategy.MessageHandleStrategy;
import com.hgl.hglpicturebackend.manager.websocket.strategy.MessageStrategyFactory;
import com.hgl.hglpicturebackend.model.entity.User;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

/**
 * @author 请别把我整破防
 * @Description: 事件处理器（消费者）
 */
@Slf4j
@Component
public class PictureEditEventWorkHandler implements WorkHandler<PictureEditEvent> {

    @Lazy
    @Resource
    private MessageStrategyFactory messageStrategyFactory;

    @Override
    public void onEvent(@NotNull PictureEditEvent event) {
        PictureEditRequestMessage pictureEditRequestMessage = event.getPictureEditRequestMessage();
        WebSocketSession session = event.getSession();
        User user = event.getUser();
        Long pictureId = event.getPictureId();
        // 获取到消息类别
        String type = pictureEditRequestMessage.getType();
        MessageHandleStrategy strategy = messageStrategyFactory.getStrategy(type);
        strategy.handle(pictureEditRequestMessage, session, user, pictureId);
    }
}
