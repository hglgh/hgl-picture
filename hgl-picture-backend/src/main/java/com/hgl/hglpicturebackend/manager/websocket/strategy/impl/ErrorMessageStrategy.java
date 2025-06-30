package com.hgl.hglpicturebackend.manager.websocket.strategy.impl;


import cn.hutool.json.JSONUtil;
import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.hgl.hglpicturebackend.manager.websocket.strategy.MessageHandleStrategy;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @ClassName ErrorMessageStrategy
 * @Author 请别把我整破防
 * @Description //TODO
 * @Date 2025/6/27 11:51
 */
@Component("ERROR")
public class ErrorMessageStrategy implements MessageHandleStrategy {

    @Resource
    private UserService userService;

    @Override
    public void handle(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) {
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
        pictureEditResponseMessage.setMessage("消息类型错误");
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        try {
            session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息发送失败:" + e.getMessage());
        }
    }
}
