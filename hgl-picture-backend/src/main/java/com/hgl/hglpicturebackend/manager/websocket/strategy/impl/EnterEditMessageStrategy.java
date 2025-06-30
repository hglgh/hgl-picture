package com.hgl.hglpicturebackend.manager.websocket.strategy.impl;


import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.hgl.hglpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.hgl.hglpicturebackend.manager.websocket.pictureEditHandler;
import com.hgl.hglpicturebackend.manager.websocket.strategy.MessageHandleStrategy;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.vo.user.UserVO;
import com.hgl.hglpicturebackend.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ClassName HandleEnterEditMessageStrategy
 * @Author 请别把我整破防
 * @Description 进入编辑状态的策略实现类
 * @Date 2025/6/27 11:01
 */
@Component("ENTER_EDIT")
public class EnterEditMessageStrategy implements MessageHandleStrategy {

    @Resource
    private pictureEditHandler pictureEditHandler;

    @Resource
    private UserService userService;

    /**
     * 处理进入编辑状态的消息
     *
     * @param pictureEditRequestMessage 消息
     * @param session                   会话
     * @param user                      用户
     * @param pictureId                 图片 ID
     */
    @Override
    public void handle(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) {
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        try {
            Map<Long, Long> pictureEditingUsers = pictureEditHandler.getPictureEditingUsers();
            //没有用户正在编辑该图片，才能进入编辑
            if (!pictureEditingUsers.containsKey(pictureId)) {
                // 设置当前用户为编辑用户
                pictureEditingUsers.putIfAbsent(pictureId, user.getId());
                //构造响应,发送到编辑的消息通知
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
                String message = String.format("用户 %s 进入编辑状态", user.getUserName());
                pictureEditResponseMessage.setMessage(message);
                pictureEditResponseMessage.setUser(userService.getUserVO(user));
                // 广播给图片的所有用户
                pictureEditHandler.broadcastToPicture(pictureId, pictureEditResponseMessage);
            } else {
                // 如果已经有用户在编辑了，那么就将该用户正在编程的消息广播给请求进行编辑的用户
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
                Long currentEditUserId = pictureEditingUsers.get(pictureId);
                UserVO currentEditUserVO = userService.getUserVO(userService.getById(currentEditUserId));
                String message = String.format("用户 %s 正在编辑该图片", currentEditUserVO.getUserName());
                pictureEditResponseMessage.setMessage(message);
                pictureEditResponseMessage.setUser(currentEditUserVO);
                // 单独发送消息给当前请求编辑的session
                TextMessage textMessage = pictureEditHandler.getTextMessage(pictureEditResponseMessage);
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "进入编辑状态时发生异常：" + e.getMessage());
        }
    }

}
