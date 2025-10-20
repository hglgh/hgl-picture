package com.hgl.hglpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hgl.hglpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.hgl.hglpicturebackend.model.dto.picture.*;
import com.hgl.hglpicturebackend.model.dto.user.UserQueryRequest;
import com.hgl.hglpicturebackend.model.entity.Picture;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.vo.picture.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * ClassName: PictureService
 * Package: com.hgl.hglpicturebackend.service
 * Description:
 *
 * @Author HGL
 * @Create: 2024/12/28 15:02
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource 文件
     * @param pictureUploadRequest 上传图片请求体
     * @param loginUser 登录用户
     * @return 图片包装类
     */
    <T> PictureVO uploadPicture(T inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取查询包装类
     * @param pictureQueryRequest 查询请求
     * @return 查询包装类
     */
    Wrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 校验图片
     * @param picture 图片
     */
    void validPicture(Picture picture);

    /**
     * 获取图片包装类（脱敏后的图片信息）分页
     * @param picturePage 图片
     * @param request 请求
     * @return 图片包装类
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 获取图片包装类（脱敏后的图片信息）单条
     * @param picture 图片
     * @param request 请求
     * @return 图片包装类
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 图片审核
     * @param pictureReviewRequest 图片审核请求体
     * @param loginUser 登录用户
     */
    Boolean doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     * @param picture 图片
     * @param loginUser 登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     * @param pictureUploadByBatchRequest 图片批量上传请求体
     * @param loginUser 登录用户
     * @return 成功创建的图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 清理图片文件
     * @param oldPicture 旧图片
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 检查空间图片权限
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 删除图片
     * @param pictureId 图片id
     * @param loginUser 登录用户
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 根据颜色搜索图片
     * @param spaceId 空间id
     * @param picColor 图片主色调
     * @param loginUser 登录用户
     * @return 图片列表
     */
    List<PictureVO> searchPictureByColor(long spaceId, String picColor, User loginUser);

    /**
     * 批量修改图片信息
     * @param pictureEditByBatchRequest 图片批量修改请求体
     * @param loginUser 登录用户
     */
    void editPicutreByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * 创建 AI 扩图任务
     * @param createPictureOutPaintingTaskRequest 创建 AI 扩图任务请求体
     * @param loginUser 登录用户
     * @return 创建 AI 扩图任务响应体
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}
