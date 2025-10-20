package com.hgl.hglpicturebackend.manager.websocket.validator;

import cn.hutool.core.util.ObjUtil;
import com.hgl.hglpicturebackend.model.entity.Picture;
import com.hgl.hglpicturebackend.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @ClassName: PictureExistenceValidator
 * @Package: com.hgl.hglpicturebackend.manager.websocket.validator
 * @Description: 图片存在性校验器
 * @Author HGL
 * @Create: 2025/10/20 10:32
 */
@Slf4j
@Component
public class PictureExistenceValidator extends AbstractHandshakeValidator {

    @Resource
    private PictureService pictureService;

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    protected boolean doValidate(HttpServletRequest request, Map<String, Object> attributes) {
        log.info("进入图片存在性校验器：开始校验图片是否存在");
        String pictureId = request.getParameter("pictureId");
        Picture picture = pictureService.getById(pictureId);
        if (ObjUtil.isEmpty(picture)) {
            log.error("图片不存在,拒绝握手");
            return false;
        }
        attributes.put("picture", picture);
        attributes.put("pictureId", Long.valueOf(pictureId));
        return true;
    }
}