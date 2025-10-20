package com.hgl.hglpicturebackend.manager.websocket.validator;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @ClassName: PictureIdValidator
 * @Package: com.hgl.hglpicturebackend.manager.websocket.validator
 * @Description: 图片ID校验器
 * @Author HGL
 * @Create: 2025/10/20 10:30
 */
@Slf4j
@Component
public class PictureIdValidator extends AbstractHandshakeValidator {

    @Override
    public int getOrder() {
        // 最高优先级
        return 1;
    }

    @Override
    protected boolean doValidate(HttpServletRequest request, Map<String, Object> attributes) {
        log.info("进入图片ID校验器： 开始校验图片ID参数");
        String pictureId = request.getParameter("pictureId");
        if (StrUtil.isBlank(pictureId)) {
            log.error("缺失图片参数,拒绝握手");
            return false;
        }
        return true;
    }
}