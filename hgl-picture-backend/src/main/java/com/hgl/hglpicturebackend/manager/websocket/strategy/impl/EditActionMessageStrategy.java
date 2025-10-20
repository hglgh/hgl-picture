package com.hgl.hglpicturebackend.manager.websocket.strategy.impl;


import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditActionEnum;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.hgl.hglpicturebackend.manager.websocket.pictureEditHandler;
import com.hgl.hglpicturebackend.manager.websocket.strategy.MessageHandleStrategy;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.util.Map;

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

    @Resource
    private UserService userService;

    /**
     * 处理执行编辑操作的消息
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
            if (pictureEditingUsers.containsKey(pictureId)) {
                Long editingUserId = pictureEditingUsers.get(pictureId);
                String editAction = pictureEditRequestMessage.getEditAction();
                PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByValue(editAction);
                if (pictureEditActionEnum == null) {
                    return;
                }
                // 确认是当前编辑者
                if (editingUserId != null && editingUserId.equals(user.getId())) {
                    PictureEditResponseMessage pictureEditResponseMessage = buildPictureEditResponseMessage(user, pictureEditActionEnum, userService, editAction);
                    pictureEditHandler.broadcastToPicture(pictureId, pictureEditResponseMessage, session);
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "处理编辑动作时发生异常：" + e.getMessage());
        }
    }

    /**
     * 构建图片编辑动作消息
     *
     * @param user                  用户
     * @param pictureEditActionEnum 图片编辑动作枚举
     * @param userService           用户服务
     * @param editAction            编辑动作
     * @return 图片编辑动作消息
     */
    private PictureEditResponseMessage buildPictureEditResponseMessage(User user, PictureEditActionEnum pictureEditActionEnum, UserService userService, String editAction) {
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
        String message = String.format("用户 %s 执行了 %s 操作", user.getUserName(), pictureEditActionEnum.getText());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setEditAction(editAction);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        return pictureEditResponseMessage;
    }
}
