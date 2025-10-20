package com.hgl.hglpicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.hgl.hglpicturebackend.config.CosClientConfig;
import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.exception.ThrowUtils;
import com.hgl.hglpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * ClassName: CosManager
 * Package: com.hgl.hglpicturebackend.manager
 * Description: 和业务层相关，但又不与数据库强绑定的文件管理类
 *
 * @Author HGL
 * @Create: 2024/12/28 11:37
 * @Deprecated 废弃, 改为使用 upload 包的模板方法优化
 */
@Slf4j
@Service
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    // 允许上传的文件最大大小单位
    final static long ONE_M = 1024 * 1024;

    //允许的图片后缀
    final static List<String> IMAGE_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传路径前缀
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        //校验图片
        validPicture(multipartFile);
        //图片上传地址
        String originalFilename = multipartFile.getOriginalFilename();
        String uuid = RandomUtil.randomString(16);
        //自己拼接文件上传文件名称，而不是使用原始文件名
        String uploadfileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadfileName);
        File file = null;
        try {
            //1、上传文件
            // 1.1创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 1.2转换MultipartFile为File
            multipartFile.transferTo(file);
            //1.3 上传文件到COS
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //解析结果并返回
            //获取图片原始信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            String format = imageInfo.getFormat();
            int pictureWidth = imageInfo.getWidth();
            int pictureHeight = imageInfo.getHeight();
            double pictureScale = NumberUtil.round((double) pictureWidth / pictureHeight, 2).doubleValue();

            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(pictureWidth);
            uploadPictureResult.setPicHeight(pictureHeight);
            uploadPictureResult.setPicScale(pictureScale);
            uploadPictureResult.setPicFormat(format);

            return uploadPictureResult;

        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 删除临时文件
            deleteTempFile(file);
        }
    }

    /**
     * 删除临时文件
     *
     * @param file 文件
     */
    public static void deleteTempFile(File file) {
        if (file != null) {
            boolean delete = file.delete();
            if (!delete) {
                log.error("file delete error, filePath = {}", file.getAbsolutePath());
            }
        }
    }

    /**
     * 校验图片
     *
     * @param multipartFile 文件
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        // 校验文件大小
        long multipartFileSize = multipartFile.getSize();
        ThrowUtils.throwIf(multipartFileSize > ONE_M * 5, ErrorCode.PARAMS_ERROR, "上传文件大小不能超过2M");
        // 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!IMAGE_SUFFIX.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "上传文件格式不支持");
    }
}
