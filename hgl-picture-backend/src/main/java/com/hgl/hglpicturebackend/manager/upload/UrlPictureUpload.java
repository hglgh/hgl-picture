package com.hgl.hglpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * ClassName: UrlPictureUpload
 * Package: com.hgl.hglpicturebackend.manager.upload
 * Description: URL图片上传
 *
 * @Author HGL
 * @Create: 2025/1/4 21:59
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected <T> void processFile(T inputSource, File file) {
        String fileUrl = (String) inputSource;
        //下载文件到临时目录
        HttpUtil.downloadFile(fileUrl, file);
    }

    @Override
    protected <T> String getOriginalFilename(T inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected <T> void validPicture(T inputSource) {
        String fileUrl = (String) inputSource;
        //校验非空
        ThrowUtils.throwIf(StringUtils.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        //校验URL格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不支持");
        }
        //校验URL的协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "仅支持http://或https://协议的文件地址");
        try (HttpResponse response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute()) {
            //发送HEAD请求，验证文件是否存在
            /*
            if (!response.isOk()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不存在");
            }
            */
            //未正常返回，无需执行其它判断
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            } else {
                //文件存在，校验文件类型(文件后缀)
                String contentType = response.header("Content-Type");
                //不为空才校验是否合法,使得校验规则相对宽松
                if (StringUtils.isBlank(contentType)){
                    ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType), ErrorCode.PARAMS_ERROR, "文件类型不合法");
                }
                //文件存在，校验文件大小
                String contentLengthStr = response.header("Content-Length");
                if (StringUtils.isNotBlank(contentLengthStr)) {
                    try {
                        long contentLength = Long.parseLong(contentLengthStr);
                        ThrowUtils.throwIf(contentLength > ONE_M * 5, ErrorCode.PARAMS_ERROR, "文件大小不能超过5M");
                    } catch (NumberFormatException e) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件大小格式异常");
                    }
                }
            }
        }
    }
}
