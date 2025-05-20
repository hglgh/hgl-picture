package com.hgl.hglpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hgl.hglpicturebackend.common.DeleteRequest;
import com.hgl.hglpicturebackend.model.dto.space.SpaceQueryRequest;
import com.hgl.hglpicturebackend.model.dto.space.SpaceAddRequest;
import com.hgl.hglpicturebackend.model.entity.Space;
import com.hgl.hglpicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.vo.space.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 请别把我整破防
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-04-03 15:26:31
*/
public interface SpaceService extends IService<Space> {

    /**
     * 添加空间
     * @param spaceAddRequest 空间添加请求
     * @param loginUser 登录用户
     * @return 添加后的空间id
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);
    
    /**
     * 获取查询包装类
     * @param spaceQueryRequest 查询请求
     * @return 查询包装类
     */
    Wrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取空间包装类（脱敏后的空间信息）单条
     * @param space 空间
     * @param request 请求
     * @return Space封装实体
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间包装类（脱敏后的空间信息）分页
     * @param spacePage 空间
     * @param request 请求
     * @return Page<SpaceVO>
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 校验空间信息
     * @param space 空间对象
     * @param add 是否为创建时校验
     */
    void validSpace(Space space, boolean add);

    void fillSpaceBySpaceLevel(Space space);

    void deleteSpace(long spaceId, User loginUser);

    void checkSpaceAuth(User loginUser, Space space);
}
