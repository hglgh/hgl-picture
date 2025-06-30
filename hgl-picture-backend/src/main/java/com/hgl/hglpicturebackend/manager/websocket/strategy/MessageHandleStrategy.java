package com.hgl.hglpicturebackend.manager.websocket.strategy;

import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.hgl.hglpicturebackend.model.entity.User;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author 请别把我整破防
 */
public interface MessageHandleStrategy {
    void handle(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId);
}
