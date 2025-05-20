package com.hgl.hglpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hgl.hglpicturebackend.annotation.AuthCheck;
import com.hgl.hglpicturebackend.common.BaseResponse;
import com.hgl.hglpicturebackend.common.DeleteRequest;
import com.hgl.hglpicturebackend.common.ResultUtils;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.exception.ThrowUtils;
import com.hgl.hglpicturebackend.model.constant.UserConstant;
import com.hgl.hglpicturebackend.model.dto.user.*;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.vo.user.LoginUserVO;
import com.hgl.hglpicturebackend.model.vo.user.UserVO;
import com.hgl.hglpicturebackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * ClassName: UserController
 * Package: com.hgl.hglpicturebackend.controller
 * Description:
 *
 * @Author HGL
 * @Create: 2024/12/10 22:43
 */
@Api(tags = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest 用户注册请求
     * @return 用户Id
     */

    @ApiOperation(value = "用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        Long result = userService.userRegister(userAccount,userPassword,checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     * @param userLoginRequest 用户登录请求
     * @return 脱敏后的用户信息
     */
    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount,userPassword,request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     * @param request servlet请求
     * @return 脱敏后的用户信息
     */
    @ApiOperation(value = "获取当前登录用户")
    @GetMapping("get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 用户注销
     * @param request servlet请求
     * @return 是否注销成功
     */
    @ApiOperation(value = "用户注销")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.userLogout(request));
    }

    /**
     * 新增用户
     * @param userAddRequest 新增用户请求
     * @return 新增用户Id
     */
    @ApiOperation(value = "新增用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.addUser(userAddRequest));
    }

    /**
     * 删除用户
     * @param deleteRequest 删除请求
     * @return 是否删除成功
     */
    @ApiOperation(value = "删除用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户
     * @param userUpdateRequest 更新用户请求
     * @return 是否更新成功
     */
    @ApiOperation(value = "更新用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User user = BeanUtil.copyProperties(userUpdateRequest, User.class);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取用户(仅管理员可以)
     * @param id 用户id
     * @return 用户信息
     */
    @ApiOperation(value = "根据id获取用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据id获取用户VO包装类
     * @param id 用户id
     * @return 脱敏后的用户信息
     */
    @ApiOperation(value = "根据id获取用户VO包装类")
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表
     * @param userQueryRequest 用户查询请求
     * @return 用户列表
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "分页获取用户列表")
    @PostMapping("/list/page")
    public BaseResponse<Page<UserVO>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        userVOPage.setRecords(userService.getUserVOList(userPage.getRecords()));
        return ResultUtils.success(userVOPage);
    }
}
