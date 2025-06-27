package com.hgl.hglpicturebackend.manager.websocket.strategy;


import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.hgl.hglpicturebackend.manager.websocket.pictureEditHandler;
import com.hgl.hglpicturebackend.model.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

/**
 * @ClassName ExitEditMessageStrategy
 * @Author 请别把我整破防
 * @Description 退出编辑状态的策略实现类
 * @Date 2025/6/27 11:03
 */
@Component("EXIT_EDIT")
public class ExitEditMessageStrategy implements MessageHandleStrategy {

    @Resource
    private pictureEditHandler pictureEditHandler;

    @Override
    public void handle(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) {
        try {
            pictureEditHandler.handleExitEditMessage(pictureEditRequestMessage, session, user, pictureId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出编辑状态时发生异常：" + e.getMessage());
        }
    }
}
