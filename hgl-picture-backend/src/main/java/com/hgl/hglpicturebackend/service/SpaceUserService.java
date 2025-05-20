package com.hgl.hglpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hgl.hglpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.hgl.hglpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.hgl.hglpicturebackend.model.entity.SpaceUser;
import com.hgl.hglpicturebackend.model.vo.spaceuser.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * @author 请别把我整破防
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间用户
     *
     * @param spaceUserAddRequest 请求体
     * @return id
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验
     *
     * @param spaceUser           实体对象
     * @param add                 是否为创建
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取查询条件
     *
     * @param spaceUserQueryRequest 请求体
     * @return 查询条件
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取空间用户视图对象
     *
     * @param spaceUser           实体对象
     * @param request              http请求
     * @return 视图对象
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间用户视图对象列表
     *
     * @param spaceUserList      实体对象列表
     * @return 视图对象列表
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
