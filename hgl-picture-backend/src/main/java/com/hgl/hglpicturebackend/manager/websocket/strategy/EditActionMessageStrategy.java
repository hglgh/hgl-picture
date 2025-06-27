package com.hgl.hglpicturebackend.manager.websocket.strategy;


import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.hgl.hglpicturebackend.manager.websocket.pictureEditHandler;
import com.hgl.hglpicturebackend.model.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

/**
 * @ClassName EditActionMessageStrategy
 * @Author 请别把我整破防
 * @Description 编辑动作消息处理策略类
 * @Date 2025/6/27 11:02
 */
@Component("EDIT_ACTION")
public class EditActionMessageStrategy implements MessageHandleStrategy {

    @Resource
    private pictureEditHandler pictureEditHandler;

    @Override
    public void handle(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) {
        try {
            pictureEditHandler.handleEditActionMessage(pictureEditRequestMessage, session, user, pictureId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "处理编辑动作时发生异常：" + e.getMessage());
        }
    }
}
