package com.hgl.hglpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.exception.ThrowUtils;
import com.hgl.hglpicturebackend.manager.sharding.DynamicShardingManager;
import com.hgl.hglpicturebackend.model.dto.space.SpaceAddRequest;
import com.hgl.hglpicturebackend.model.dto.space.SpaceQueryRequest;
import com.hgl.hglpicturebackend.model.entity.Picture;
import com.hgl.hglpicturebackend.model.entity.Space;
import com.hgl.hglpicturebackend.model.entity.SpaceUser;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.enums.SpaceLevelEnum;
import com.hgl.hglpicturebackend.model.enums.SpaceRoleEnum;
import com.hgl.hglpicturebackend.model.enums.SpaceTypeEnum;
import com.hgl.hglpicturebackend.model.vo.space.SpaceVO;
import com.hgl.hglpicturebackend.model.vo.user.UserVO;
import com.hgl.hglpicturebackend.service.PictureService;
import com.hgl.hglpicturebackend.service.SpaceService;
import com.hgl.hglpicturebackend.mapper.SpaceMapper;
import com.hgl.hglpicturebackend.service.SpaceUserService;
import com.hgl.hglpicturebackend.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author 请别把我整破防
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-04-03 15:26:31
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    private final Map<Long, Object> lockMap = new ConcurrentHashMap<>();
    @Resource
    private UserService userService;

    @Lazy
    @Resource
    private PictureService pictureService;

    @Lazy
    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private TransactionTemplate transactionTemplate;

    //为了方便部署，注释调分表
/*    @Lazy
    @Resource
    private DynamicShardingManager dynamicShardingManager;*/

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 将请求参数转换为 Space 对象
        Space space = BeanUtil.copyProperties(spaceAddRequest, Space.class);
        // 填充默认值
        fillDefaultValues(space);
        // 数据校验
        this.validSpace(space, true);
        // 设置用户 ID
        Long loginUserId = loginUser.getId();
        space.setUserId(loginUserId);
        // 权限校验
        checkUserPermission(space, loginUser);
        // 控制同一用户只能创建一个私有空间、以及一个团队空间
        return createSpaceWithLock(space, loginUserId);
    }

    @Override
    public Wrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        //拼接查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        Integer spaceType = space.getSpaceType();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        // 要创建时判断
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            ThrowUtils.throwIf(spaceType == null, ErrorCode.PARAMS_ERROR, "空间类型不能为空");
        }
        // 修改数据时，如果要改空间级别
        ThrowUtils.throwIf(spaceLevel != null && spaceLevelEnum == null, ErrorCode.PARAMS_ERROR, "空间级别不存在");
        ThrowUtils.throwIf(spaceName != null && spaceName.length() > 30, ErrorCode.PARAMS_ERROR, "空间名称过长");
        ThrowUtils.throwIf(spaceType != null && spaceTypeEnum == null, ErrorCode.PARAMS_ERROR, "空间类型不存在");
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpace(long spaceId, User loginUser) {
        // 判断是否存在
        Space oldSpace = this.getById(spaceId);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        this.checkSpaceAuth(loginUser, oldSpace);
        // 操作数据库
        boolean result = this.removeById(spaceId);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        List<Picture> pictureList = pictureService.list(new LambdaQueryWrapper<Picture>().eq(Picture::getSpaceId, spaceId));
        if (CollUtil.isNotEmpty(pictureList)) {
            pictureService.removeBatchByIds(pictureList);
        }
    }

    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }

    private void fillDefaultValues(Space space) {
        if (StringUtils.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        // 默认空间类型为私有
        if (space.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        // 自动填充限额
        this.fillSpaceBySpaceLevel(space);
    }

    private void checkUserPermission(Space space, User loginUser) {
        if (SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户无权限创建指定级别的空间");
        }
    }

    /**
     * 创建空间 这里是使用了transactionTemplate 来保证事务的原子性
     *
     * @param space  空间实体
     * @param userId 用户Id
     * @return 新创建空间的Id
     */
    private long createSpaceWithLock(Space space, Long userId) {
        Lock lock = (Lock) lockMap.computeIfAbsent(userId, key -> new ReentrantLock());
        lock.lock();
        try {
            Long newSpaceId = transactionTemplate.execute(status -> {
                //判断是否已经创建过空间了
                boolean isExist = this.lambdaQuery().eq(Space::getUserId, userId).eq(Space::getSpaceType, space.getSpaceType()).exists();
                ThrowUtils.throwIf(isExist, ErrorCode.OPERATION_ERROR, "每个用户每类空间仅能创建一个");
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建空间失败");
                // 如果是团队空间，关联新增团队成员记录
                if (SpaceTypeEnum.TEAM.getValue() == space.getSpaceType()) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(userId);
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    result = spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
                }
                // 创建分表(仅对团队空间生效)为了方便部署，暂时不使用
//                dynamicShardingManager.createSpacePictureTable(space);
                // 返回新写入的数据 id
                return space.getId();
            });
            return Optional.ofNullable(newSpaceId).orElseThrow(() -> new RuntimeException("创建空间失败"));
        } finally {
            lock.unlock();
            lockMap.remove(userId);
        }
    }
}




