package com.hgl.hglpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.exception.ThrowUtils;
import com.hgl.hglpicturebackend.manager.auth.StpKit;
import com.hgl.hglpicturebackend.mapper.UserMapper;
import com.hgl.hglpicturebackend.model.constant.UserConstant;
import com.hgl.hglpicturebackend.model.dto.user.UserAddRequest;
import com.hgl.hglpicturebackend.model.dto.user.UserQueryRequest;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.enums.UserRoleEnum;
import com.hgl.hglpicturebackend.model.vo.user.LoginUserVO;
import com.hgl.hglpicturebackend.model.vo.user.UserVO;
import com.hgl.hglpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


/**
 * ClassName: UserServiceImpl
 * Package: com.hgl.hglpicturebackend.service.impl
 * Description:
 *
 * @Author HGL
 * @Create: 2024/12/10 23:03
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        //校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() <= 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8 || userAccount.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        //校验账户不能重复
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUserAccount, userAccount));
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号重复");
        //对用户密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        //返回用户id
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        //盐值,混淆密码
        final String salt = "hgl";
        return DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() <= 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        //加密
        String encryptPassword = getEncryptPassword(userPassword);
        //查询用户
//        User user = this.lambdaQuery().eq(User::getUserAccount, userAccount).eq(User::getUserPassword, encryptPassword).one();
//        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUserAccount, userAccount).eq(User::getUserPassword, encryptPassword));
        User user = this.baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserAccount, userAccount).eq(User::getUserPassword, encryptPassword));
        if (user == null){
            log.error("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //保存用户的登录态到session中
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        //记录用户登录态到sa-token，便于空间鉴权时使用，但要保证该用户信息和 springSession 中的信息过期时间保持一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null){
            return null;
        }
        return BeanUtil.copyProperties(user, LoginUserVO.class);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object object = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser  = (User) object;
        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        // 从数据库查询（追求性能的话可以注释，直接走缓存）,这是防止数据不一致的情况
        currentUser = this.getById(currentUser.getId());
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return currentUser;
    }

    @Override
    public Boolean userLogout(HttpServletRequest request) {
        //先判断是否已登录
        Object object = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user  = (User) object;
        ThrowUtils.throwIf(user == null, ErrorCode.OPERATION_ERROR,"未登录");
        //清除缓存（清除登录状态）
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public Long addUser(UserAddRequest userAddRequest) {
        User user = BeanUtil.copyProperties(userAddRequest, User.class);
        final String DEFAULT_PASSWORD = "12345678";
        user.setUserPassword(getEncryptPassword(DEFAULT_PASSWORD));
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "添加用户失败");
        return user.getId();
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null){
            return null;
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    /**
     * 获取查询包装类
     * @param userQueryRequest 查询请求
     * @return 查询包装类
     */
    @Override
    public Wrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null){
            return null;
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> records) {
        return records.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

}
