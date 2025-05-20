package com.hgl.hglpicturebackend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.exception.ThrowUtils;
import com.hgl.hglpicturebackend.mapper.SpaceMapper;
import com.hgl.hglpicturebackend.model.dto.space.analyze.*;
import com.hgl.hglpicturebackend.model.entity.Picture;
import com.hgl.hglpicturebackend.model.entity.Space;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.vo.space.analyze.*;
import com.hgl.hglpicturebackend.service.PictureService;
import com.hgl.hglpicturebackend.service.SpaceAnalyzeService;
import com.hgl.hglpicturebackend.service.SpaceService;
import com.hgl.hglpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ClassName: SpaceAnalyzeServiceImpl
 * Package: com.hgl.hglpicturebackend.service.impl
 * Description:
 *
 * @Author HGL
 * @Create: 2025/4/14 14:05
 */
@Slf4j
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceAnalyzeService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private PictureService pictureService;


    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        if (spaceUsageAnalyzeRequest.isQueryPublic() || spaceUsageAnalyzeRequest.isQueryAll()) {
            // 查询全部或公共图库逻辑
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, queryWrapper);
            List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long usedSize = pictureObjList.stream().mapToLong(obj -> Long.parseLong(obj.toString())).sum();
            long usedCount = pictureObjList.size();
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(usedSize);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);
            // 公共图库无上限、无比例
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);
            return spaceUsageAnalyzeResponse;
        } else {
            // 查询指定空间
            Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            //检查空间分析权限
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);
            Space space = spaceService.getById(spaceId);
            // 构造返回结果
            SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
            response.setUsedSize(space.getTotalSize());
            response.setMaxSize(space.getMaxSize());
            // 后端直接算好百分比，这样前端可以直接展示
            double sizeUsageRatio = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
            response.setSizeUsageRatio(sizeUsageRatio);
            response.setUsedCount(space.getTotalCount());
            response.setMaxCount(space.getMaxCount());
            double countUsageRatio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
            response.setCountUsageRatio(countUsageRatio);
            return response;
        }

    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //校验权限
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("category", "count(*) as count", "sum(picSize) as totalSize");
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
        queryWrapper.groupBy("category");
        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(map -> {
            String category = map.get("category") == null ? "未分类" : map.get("category").toString();
            Long count = Long.parseLong(map.get("count").toString());
            Long totalSize = Long.parseLong(map.get("totalSize").toString());
            return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("tags");
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
        List<String> tagsJsonList = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(Objects::toString).collect(Collectors.toList());

        Map<String, Long> tagCountMap = tagsJsonList
                .stream()
                .flatMap(tagsJson -> JSONUtil.toList(tagsJson, String.class).stream())
                .collect(Collectors.groupingBy(String::toString, Collectors.counting()));
        return tagCountMap.entrySet().stream()
                // 按数量倒序
                .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))
                .map(entry -> {
                    String tag = entry.getKey();
                    Long count = entry.getValue();
                    return new SpaceTagAnalyzeResponse(tag, count);
                }).collect(Collectors.toList());
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("picSize");
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);
        List<Long> picSizes = pictureService.getBaseMapper().selectObjs(queryWrapper).stream().map(size -> Long.parseLong(size.toString())).collect(Collectors.toList());
        //定义分段范围，注意使用有序 Map
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
/*        sizeRanges.put("<100KB", picSizes.stream().filter(size -> size < 100 * 1024).count());
        sizeRanges.put("100KB-500KB", picSizes.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        sizeRanges.put("500KB-1MB", picSizes.stream().filter(size -> size >= 500 * 1024 && size < 1024 * 1024).count());
        sizeRanges.put(">1MB", picSizes.stream().filter(size -> size >= 1024 * 1024).count());*/

        //进一步优化，只需要遍历一次 picSizes 列表就可以按大小分别统计了。
        sizeRanges.put("<100KB", 0L);
        sizeRanges.put("100KB-500KB", 0L);
        sizeRanges.put("500KB-1MB", 0L);
        sizeRanges.put(">1MB", 0L);

        for (Long size : picSizes) {
            if (size < 100 * 1024) {
                sizeRanges.put("<100KB", sizeRanges.get("<100KB") + 1);
            } else if (size < 500 * 1024) {
                sizeRanges.put("100KB-500KB", sizeRanges.get("100KB-500KB") + 1);
            } else if (size < 1024 * 1024) {
                sizeRanges.put("500KB-1MB", sizeRanges.get("500KB-1MB") + 1);
            } else {
                sizeRanges.put(">1MB", sizeRanges.get(">1MB") + 1);
            }
        }
        // 返回结果
        return sizeRanges.entrySet().stream().map(entry -> {
            String sizeRange = entry.getKey();
            Long count = entry.getValue();
            return new SpaceSizeAnalyzeResponse(sizeRange, count);
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("date_format(editTime, '%Y-%m-%d') as period", "count(*) as count");
                break;
            case "week":
//                queryWrapper.select("date_format(editTime, '%Y-%u') as period", "count(*) as count");
                queryWrapper.select("YEARWEEK(createTime) AS period", "COUNT(*) AS count");
                break;
            case "month":
                queryWrapper.select("date_format(editTime, '%Y-%m') as period", "count(*) as count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
        }
        //分组和排序
        queryWrapper.groupBy("period").orderByAsc("period");
        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(entrySet -> {
            String period = entrySet.get("period").toString();
            Long count = Long.parseLong(entrySet.get("count").toString());
            return new SpaceUserAnalyzeResponse(period, count);
        }).collect(Collectors.toList());
    }

    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //仅管理员可以查看空间排行
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权查看空间排行");
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "userId", "totalSize")
                .orderByDesc("totalSize")
                .last("limit " + spaceRankAnalyzeRequest.getTopN());
        return spaceService.list(queryWrapper);
    }

    /**
     * 检查空间分析权限
     *
     * @param spaceAnalyzeRequest 请求体
     * @param loginUser           登录用户
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        // 检查权限
        if (spaceAnalyzeRequest.isQueryAll() || spaceAnalyzeRequest.isQueryPublic()) {
            // 全空间分析或者公共图库权限校验：仅管理员可访问
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问");
        } else {
            // 私有空间权限校验
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            spaceService.checkSpaceAuth(loginUser, space);
        }
    }

    /**
     * 填充查询条件
     *
     * @param spaceAnalyzeRequest 请求体
     * @param queryWrapper        查询条件
     */
    private void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        if (spaceAnalyzeRequest.isQueryAll()) {
            // 查询所有空间
            return;
        }
        if (spaceAnalyzeRequest.isQueryPublic()) {
            // 查询公共空间
            queryWrapper.isNull("spaceId");
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }
}
