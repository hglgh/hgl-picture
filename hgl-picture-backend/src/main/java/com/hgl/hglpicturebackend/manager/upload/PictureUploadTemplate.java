package com.hgl.hglpicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hgl.hglpicturebackend.config.CosClientConfig;
import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.exception.ThrowUtils;
import com.hgl.hglpicturebackend.manager.CosManager;
import com.hgl.hglpicturebackend.model.dto.file.UploadPictureResult;
import com.hgl.hglpicturebackend.utils.ColorTransformUtils;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
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
 */
@Slf4j
@Service
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    // 允许上传的文件最大大小单位
    final static long ONE_M = 1024 * 1024;

    //允许的图片后缀
    final static List<String> IMAGE_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    final static List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");

    /**
     * 上传图片
     *
     * @param inputSource      文件
     * @param uploadPathPrefix 上传路径前缀
     */
    public <T> UploadPictureResult uploadPicture(T inputSource, String uploadPathPrefix) {
        //1.校验图片
        validPicture(inputSource);
        //2.图片上传地址
        String originalFilename = getOriginalFilename(inputSource);
        String uuid = RandomUtil.randomString(16);
        //自己拼接文件上传文件名称，而不是使用原始文件名
        String uploadfileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadfileName);
        File file = null;
        try {
            //3、上传文件
            // 3.1创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 3.2处理文件来源
            processFile(inputSource, file);
            //3.3 上传文件到COS
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //4.解析结果并返回
            //获取图片原始信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //获取图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                CIObject compressedCiObject = objectList.get(0);
                //封装缩略图返回结果
                CIObject thumbnailCiObject = compressedCiObject;
                if (objectList.size() > 1) {
                    thumbnailCiObject = objectList.get(1);
                }
                return buildResult(originalFilename, compressedCiObject, thumbnailCiObject, imageInfo);
            }
            //封装原图返回结果
            return buildResult(imageInfo, uploadPath, originalFilename, file);

        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 5.删除临时文件
            deleteTempFile(file);
        }
    }

    /**
     * 处理输入源并生成本地文件
     */
    protected abstract <T> void processFile(T inputSource, File file);

    /**
     * 获取输入源的原始文件名
     */
    protected abstract <T> String getOriginalFilename(T inputSource);

    /**
     * 校验输入源（本地文件或者URL）
     */
    protected abstract <T> void validPicture(T inputSource);

    /**
     * 构建上传结果
     *
     * @param imageInfo        对象存储返回的图片信息
     * @param uploadPath       上传路径
     * @param originalFilename 原始文件名
     * @param file             本地文件
     * @return 上传结果
     */
    private UploadPictureResult buildResult(ImageInfo imageInfo, String uploadPath, String originalFilename, File file) {
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
        //获取图片主色调
//        uploadPictureResult.setPicColor(imageInfo.getAve());
        uploadPictureResult.setPicColor(ColorTransformUtils.normalizeHexColor(imageInfo.getAve()));
        return uploadPictureResult;
    }

    /**
     * 构建上传结果
     *
     * @param originalFilename   原始文件名
     * @param compressedCiObject 压缩后的图片对象
     * @param thumbnailCiObject  缩略图对象
     * @param imageInfo          图片信息
     * @return 上传结果
     */
    private UploadPictureResult buildResult(String originalFilename, CIObject compressedCiObject, CIObject thumbnailCiObject, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
//        uploadPictureResult.setPicColor(imageInfo.getAve());
        uploadPictureResult.setPicColor(ColorTransformUtils.normalizeHexColor(imageInfo.getAve()));
        //设置图片为压缩后的地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        //设置缩略图为缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        return uploadPictureResult;
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

}
