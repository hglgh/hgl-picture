package com.hgl.hglpicturebackend.controller;

import com.hgl.hglpicturebackend.annotation.AuthCheck;
import com.hgl.hglpicturebackend.common.BaseResponse;
import com.hgl.hglpicturebackend.common.ResultUtils;
import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * ClassName: FileController
 * Package: com.hgl.hglpicturebackend.controller
 * Description:
 *
 * @Author HGL
 * @Create: 2024/12/28 11:47
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 测试上传文件
     *
     * @param multipartFile 文件
     * @return
     */
    @PostMapping("/test/upload")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<String> testUpload(@RequestPart("file") MultipartFile multipartFile) {
        //文件目录
        String originalFilename = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", originalFilename);
        File file = null;
        try {
            //1、上传文件
            // 1.1创建临时文件
            file = File.createTempFile(filePath, null);
            // 1.2转换MultipartFile为File
            multipartFile.transferTo(file);
            //1.3 上传文件到COS
            cosManager.putObject(filePath, file);
            return ResultUtils.success(filePath);
        } catch (Exception e) {
            log.error("file upload error, filePath = {}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filePath = {}", filePath);
                }
            }
        }
    }

    /**
     * 测试下载文件(腾讯COS -> 后端 -> 前端浏览器)
     *
     * @param filePath 文件路径
     * @param response 响应
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/test/download")
    public void testDownload(@RequestParam("filePath") String filePath, HttpServletResponse response) throws Exception {
        COSObjectInputStream inputStream = null;
        try {
            // 1. 从COS获取文件对象
            COSObject cosObject = cosManager.getObject(filePath);
            // 2. 获取文件输入流
            inputStream = cosObject.getObjectContent();
            // 将文件输入流转换为字节数组
            byte[] byteArray = IOUtils.toByteArray(inputStream);
            //设置响应头(告诉浏览器要下载文件)
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filePath);
            //写入响应
            response.getOutputStream().write(byteArray);
            //刷新缓冲区
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error("file download error, filePath = {}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (inputStream != null) {
                // 关闭输入流
                inputStream.close();
            }
        }

    }
}
