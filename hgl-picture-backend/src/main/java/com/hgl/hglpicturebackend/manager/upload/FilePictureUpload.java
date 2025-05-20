package com.hgl.hglpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * ClassName: FilePictureUpload
 * Package: com.hgl.hglpicturebackend.manager.upload
 * Description:文件图片上传
 *
 * @Author HGL
 * @Create: 2025/1/4 21:54
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected <T> void processFile(T inputSource, File file) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected <T> String getOriginalFilename(T inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected <T> void validPicture(T inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        // 校验文件大小
        long multipartFileSize = multipartFile.getSize();
        ThrowUtils.throwIf(multipartFileSize > ONE_M * 5, ErrorCode.PARAMS_ERROR, "上传文件大小不能超过5M");
        // 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!IMAGE_SUFFIX.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "上传文件格式不支持");
    }
}
