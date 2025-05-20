package com.hgl.hglpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hgl.hglpicturebackend.model.dto.user.UserAddRequest;
import com.hgl.hglpicturebackend.model.dto.user.UserQueryRequest;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.vo.user.LoginUserVO;
import com.hgl.hglpicturebackend.model.vo.user.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * ClassName: UserService
 * Package: com.hgl.hglpicturebackend.service
 * Description:
 *
 * @Author HGL
 * @Create: 2024/12/10 22:45
 */

public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 对用户明文密码进行加密
     * @param userPassword 用户明文密码
     * @return 含加盐的加密密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      httpServlet请求
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 将用户信息脱敏
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    LoginUserVO getLoginUserVO(User user);
    /**
     * 获取当前登录用户
     *
     * @param request httpServlet请求
     * @return 用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     * @param request httpServlet请求
     * @return 是否注销成功
     */
    Boolean userLogout(HttpServletRequest request);

    Long addUser(UserAddRequest userAddRequest);

    /**
     * 将用户信息脱敏
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取查询包装类
     * @param userQueryRequest 查询请求
     * @return 查询包装类
     */
    Wrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 获取用户列表
     * @param records 用户列表
     * @return 脱敏后的用户列表
     */
    List<UserVO> getUserVOList(List<User> records);

    /**
     * 判断用户是否为管理员
     * @param user 用户信息
     * @return 是否为管理员
     */
    boolean isAdmin(User user);
}
