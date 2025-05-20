package com.hgl.hglpicturebackend.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.hgl.hglpicturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: CosManager
 * Package: com.hgl.hglpicturebackend.manager
 * Description: 和业务层无直接关联关系的，通用的
 *
 * @Author HGL
 * @Create: 2024/12/28 11:37
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传文件对象： 将本地文件上传到 COS
     * @param key 唯一键
     * @param file 本地文件
     *
     */
    public PutObjectResult putObject(String key, File file){
        /// 创建 putObjectRequest 请求
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        // 执行 putObjectRequest 请求
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     * @param key 唯一键
     *
     */
    public COSObject getObject(String key){
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传对象（附带图片信息）： 将本地文件上传到 COS
     * @param key 唯一键
     * @param file 本地文件
     *
     */
    public PutObjectResult putPictureObject(String key, File file){
        /// 创建 putObjectRequest 请求
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        //对图片进行处理（获取图片的基本信息也被视为对图片的一种处理）
        // 设置图片处理参数
        PicOperations picOperations = new PicOperations();
        // 设置是否返回图片信息
        picOperations.setIsPicInfo(1);
        // 设置图片处理规则
        List<PicOperations.Rule> rules = new ArrayList<>();
        //1、图片压缩(转成webp格式)
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setRule("imageMogr2/format/webp");
        compressRule.setBucket(cosClientConfig.getBucket());
        // 设置处理后的文件名
        String webpKey = FileUtil.mainName(key) + ".webp";
        compressRule.setFileId(webpKey);
        rules.add(compressRule);
        //2、设置缩略图，仅对大于20kb的图片有效
        //FileUtil.size(file) > 20 * 1024
        if (file.length() > 20 * 1024){
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 256, 256));
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            String suffix = FileUtil.getSuffix(key);
            // 如果文件后缀为空，则默认为jpg
            if (StrUtil.isBlank(suffix)){
                suffix = "jpg";
            }
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + suffix;
            thumbnailRule.setFileId(thumbnailKey);
            rules.add(thumbnailRule);
        }
        //构造处理参数
        putObjectRequest.setPicOperations(picOperations);
        picOperations.setRules(rules);
        // 执行 putObjectRequest 请求
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param key 文件 key
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }
}
