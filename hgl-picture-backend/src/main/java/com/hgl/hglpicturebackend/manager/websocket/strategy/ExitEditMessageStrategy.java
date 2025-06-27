package com.hgl.hglpicturebackend.manager.websocket.strategy;


import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.hgl.hglpicturebackend.manager.websocket.pictureEditHandler;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.util.Map;

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

    @Resource
    private UserService userService;

    /**
     * 处理退出编辑状态的消息
     *
     * @param pictureEditRequestMessage 消息
     * @param session                   会话
     * @param user                      用户
     * @param pictureId                 图片 ID
     */
    @Override
    public void handle(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) {
        try {
            Map<Long, Long> pictureEditingUsers = pictureEditHandler.getPictureEditingUsers();
            Long editingUserId = pictureEditingUsers.get(pictureId);
            if (editingUserId != null && editingUserId.equals(user.getId())) {
                // 移除当前用户的编辑状态
                pictureEditingUsers.remove(pictureId);
                // 构造响应，发送退出编辑的消息通知
                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
                String message = String.format("%s退出编辑图片", user.getUserName());
                pictureEditResponseMessage.setMessage(message);
                pictureEditResponseMessage.setUser(userService.getUserVO(user));
                pictureEditHandler.broadcastToPicture(pictureId, pictureEditResponseMessage);
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出编辑状态时发生异常：" + e.getMessage());
        }
    }
}
