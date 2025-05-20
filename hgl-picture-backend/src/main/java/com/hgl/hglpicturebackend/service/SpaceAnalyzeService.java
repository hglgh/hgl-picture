package com.hgl.hglpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hgl.hglpicturebackend.model.dto.space.analyze.*;
import com.hgl.hglpicturebackend.model.entity.Space;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.vo.space.analyze.*;

import java.util.List;

/**
 * ClassName: SpaceAnalyzeService
 * Package: com.hgl.hglpicturebackend.service
 * Description:
 *
 * @Author HGL
 * @Create: 2025/4/14 14:04
 */
public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 获取空间使用情况分析
     *
     * @param spaceUsageAnalyzeRequest 请求体
     * @param loginUser                登录用户
     * @return 空间使用情况分析
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 获取空间分类分析
     *
     * @param spaceCategoryAnalyzeRequest 请求体
     * @param loginUser                   登录用户
     * @return 空间分类分析
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     * 获取空间标签分析
     * @param spaceTagAnalyzeRequest 请求体
     * @param loginUser 登录用户
     * @return 空间标签分析
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取空间大小分析
     * @param spaceSizeAnalyzeRequest 请求体
     * @param loginUser 登录用户
     * @return 空间大小分析
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 获取用户的上传行为
     * @param spaceUserAnalyzeRequest 请求体
     * @param loginUser 登录用户
     * @return 用户的上传行为
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 获取空间排行分析
     * @param spaceRankAnalyzeRequest 请求体
     * @param loginUser 登录用户
     * @return 空间排行分析
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
