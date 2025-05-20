package com.hgl.hglpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hgl.hglpicturebackend.api.aliyunai.AliYunAiApi;
import com.hgl.hglpicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.hgl.hglpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.hgl.hglpicturebackend.exception.BusinessException;
import com.hgl.hglpicturebackend.exception.ErrorCode;
import com.hgl.hglpicturebackend.exception.ThrowUtils;
import com.hgl.hglpicturebackend.manager.CosManager;
import com.hgl.hglpicturebackend.manager.upload.FilePictureUpload;
import com.hgl.hglpicturebackend.manager.upload.PictureUploadTemplate;
import com.hgl.hglpicturebackend.manager.upload.UrlPictureUpload;
import com.hgl.hglpicturebackend.mapper.PictureMapper;
import com.hgl.hglpicturebackend.model.dto.file.UploadPictureResult;
import com.hgl.hglpicturebackend.model.dto.picture.*;
import com.hgl.hglpicturebackend.model.entity.Picture;
import com.hgl.hglpicturebackend.model.entity.Space;
import com.hgl.hglpicturebackend.model.entity.User;
import com.hgl.hglpicturebackend.model.enums.PictureReviewStatusEnum;
import com.hgl.hglpicturebackend.model.vo.picture.PictureVO;
import com.hgl.hglpicturebackend.model.vo.user.UserVO;
import com.hgl.hglpicturebackend.service.PictureService;
import com.hgl.hglpicturebackend.service.SpaceService;
import com.hgl.hglpicturebackend.service.UserService;
import com.hgl.hglpicturebackend.utils.ColorSimilarUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: PictureServiceImpl
 * Package: com.hgl.hglpicturebackend.service.impl
 * Description:
 *
 * @Author HGL
 * @Create: 2024/12/28 16:49
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
/*
    @Resource
    private FileManager fileManager;
 */

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private SpaceService spaceService;

    @Resource
    private AliYunAiApi aliYunAiApi;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public <T> PictureVO uploadPicture(T inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "未登录");
        //校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            //校验是否有空间权限,仅空间管理员可以上传
//            ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH_ERROR, "无空间权限");
            //校验额度
            ThrowUtils.throwIf(space.getTotalCount() >= space.getMaxCount(), ErrorCode.OPERATION_ERROR, "空间条数不足");
            ThrowUtils.throwIf(space.getTotalSize() >= space.getMaxSize(), ErrorCode.OPERATION_ERROR, "空间大小不足");
        }
        //判断是新增还是修改(如果是更新的话还要判断原来图片是否存在)
        Long pictureId = null;
        if (pictureUploadRequest.getId() != null) {
            pictureId = pictureUploadRequest.getId();
        }
        //判断图片是否存在
        if (pictureId != null) {
            //判断图片是否存在
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //仅本人和管理员才能修改图片
/*            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
            }*/
            //校验空间是否一致
            //请求体没有传spaceId，则复用原有的图片的spaceId
            if (spaceId == null) {
                spaceId = oldPicture.getSpaceId();
            }
            //如果传来的spaceId和原来的不一致，则抛出异常
            ThrowUtils.throwIf(!ObjUtil.equals(oldPicture.getSpaceId(),spaceId), ErrorCode.PARAMS_ERROR, "空间不一致");
        }
        //上传图片,获取图片信息
        //按照用户id划分目录
        String uploadPathPrefix;
        if (spaceId == null) {
            //公共图库
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            //私有图库
            uploadPathPrefix = String.format("private/%s", spaceId);
        }
        //根据inputSource的类型区分上传方式（判断是文件上传还是通过url上传）
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);

        //构造要入库的图片信息
        Picture picture = BeanUtil.copyProperties(uploadPictureResult, Picture.class);
        picture.setSpaceId(spaceId);
        picture.setUserId(loginUser.getId());
        String picName = uploadPictureResult.getPicName();
        if (StrUtil.isNotBlank(pictureUploadRequest.getPictureName())) {
            picName = pictureUploadRequest.getPictureName();
        }
        picture.setName(picName);
        // 补充审核参数
        this.fillReviewParams(picture, loginUser);
        if (pictureId != null) {
            //更新,需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setUpdateTime(new Date());
        }
        //保存到数据库中
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "保存图片失败");
            //成功后更新空间的额度
            if (finalSpaceId != null) {
                Space space = spaceService.getById(finalSpaceId);
                space.setTotalCount(space.getTotalCount() + 1);
                space.setTotalSize(space.getTotalSize() + picture.getPicSize());
                boolean update = spaceService.updateById(space);
                ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "更新空间额度失败");
            }
            return null;
        });
        //TODO可自行实现，如果是更新，则删除原来的图片，在判断空间是否为私人空间，若是则需要释放私人空间额度
        return PictureVO.objToVo(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            //and(tags like "%\"java\"%" and tags like "%\"python\"%")
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }


    /**
     * 分页获取图片封装
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }


    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public Boolean doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //校验参数
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getPictureReviewStatusEnumByValue(reviewStatus);
        ThrowUtils.throwIf(id == null || reviewStatusEnum == null || reviewStatusEnum.equals(PictureReviewStatusEnum.REVIEWING), ErrorCode.PARAMS_ERROR);
        //判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //判断老图片状态是否已经为待审核
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片状态已经为待审核");
        }
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, picture);
        picture.setReviewerId(loginUser.getId());
        picture.setReviewTime(new Date());

        return this.updateById(picture);
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            //管理员自动审核通过
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
            picture.setReviewMessage("管理员自动审核通过");
        } else {
            //非管理员，创建或编辑都要改为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest 图片批量上传请求体
     * @param loginUser                   登录用户
     * @return 创建成功的图片数量
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        //1、校验参数
        // 格式化数量
        Integer count = pictureUploadByBatchRequest.getCount();
        String searchText = pictureUploadByBatchRequest.getSearchText();
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多30条");
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        //2、抓取内容
        Document document;
        // 要抓取的地址
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("页面获取失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "页面获取失败");
        }
        //3、解析内容
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            // 处理图片上传地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            //4、上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setPictureName(namePrefix + (uploadCount + 1));
            pictureUploadRequest.setFileUrl(fileUrl);
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断该图片是否被多条记录使用
        //有不止一条记录用到了该图片，不清理
        if (this.lambdaQuery().eq(Picture::getUrl, oldPicture.getUrl()).count() > 1) {
            return;
        }
        try {
            // FIXME 注意，这里的 url 包含了域名，实际上只要传 key 值（存储路径）就够了
            URL url = new URL(oldPicture.getUrl());
            String path = url.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            cosManager.deleteObject(path);
            // 清理缩略图
            if (StrUtil.isNotBlank(oldPicture.getThumbnailUrl())) {
                URL thumbnailUrl = new URL(oldPicture.getThumbnailUrl());
                String thumbnailPath = thumbnailUrl.getPath();
                if (thumbnailPath.startsWith("/")) {
                    thumbnailPath = thumbnailPath.substring(1);
                }
                cosManager.deleteObject(thumbnailPath);
            }
        } catch (MalformedURLException e) {
            log.error("处理图片删除时遇到格式错误的 URL。图片 URL: {}", oldPicture.getUrl(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "格式错误的 URL");
        }
    }

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        Long loginUserId = loginUser.getId();
        if (spaceId == null) {
            //公共图库，仅本人和管理员可访问
            if (!userService.isAdmin(loginUser) && !loginUserId.equals(picture.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有图库，仅本人可访问
            if (!loginUserId.equals(picture.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        // 判断是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //校验权限
//        this.checkPictureAuth(loginUser, oldPicture);
        // 操作数据库
        transactionTemplate.execute(status -> {
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            //更新使用的空间数量
            Space space = spaceService.getById(oldPicture.getSpaceId());
            if (space != null) {
                space.setTotalCount(space.getTotalCount() - 1);
                space.setTotalSize(space.getTotalSize() - oldPicture.getPicSize());
                boolean update = spaceService.updateById(space);
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新空间数量失败");
            }
            return true;
        });
        // 删除cos端文件
        this.clearPictureFile(oldPicture);
    }

    @Override
    public List<PictureVO> searchPictureByColor(long spaceId, String picColor, User loginUser) {
        //1、校验参数
        ThrowUtils.throwIf(spaceId <= 0, ErrorCode.PARAMS_ERROR, "空间id不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR, "颜色不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //2、校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH_ERROR, "无空间权限");
        //3、查询该空间下的所有图片(必须要有主色调)
        List<Picture> pictureList = this.lambdaQuery().eq(Picture::getSpaceId, spaceId).isNotNull(Picture::getPicColor).list();
        //没有图片直接返回空数组
        if (CollUtil.isEmpty(pictureList)) {
            return new ArrayList<>();
        }
        //将颜色字符串转换为主色调
        Color targetColor = Color.decode(picColor);
        //4、计算相识度并排序
        List<Picture> pictures = pictureList.stream().sorted(Comparator.comparingDouble(picture -> {
            String hexColor = picture.getPicColor();
            if (StrUtil.isBlank(hexColor)) {
                //没有主色调的默认排序为最后
                return Double.MAX_VALUE;
            }
            Color color = Color.decode(hexColor);
            return -ColorSimilarUtils.calculateSimilarity(color, targetColor);
        })).limit(10).collect(Collectors.toList());
        //5、返回结果
        return pictures.stream().map(PictureVO::objToVo).collect(Collectors.toList());

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editPicutreByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();
        String nameRule = pictureEditByBatchRequest.getNameRule();
        //1、校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(CollUtil.isEmpty(pictureIdList) || spaceId == null, ErrorCode.PARAMS_ERROR);
        //2、校验权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH_ERROR, "无空间权限");
        // 3. 查询指定图片，仅选择需要的字段
        List<Picture> pictureList = this.lambdaQuery().select(Picture::getId, Picture::getSpaceId).eq(Picture::getSpaceId, spaceId).in(Picture::getId, pictureIdList).list();
        ThrowUtils.throwIf(CollUtil.isEmpty(pictureList), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        for (Picture picture : pictureList) {
            if (StrUtil.isNotBlank(category)) {
                picture.setCategory(category);
            }
            if (CollUtil.isNotEmpty(tags)) {
                picture.setTags(JSONUtil.toJsonStr(tags));
            }
            if (StrUtil.isNotBlank(nameRule)) {
                fillPictureWithNameRule(pictureList, nameRule);
            }
        }
        boolean update = this.updateBatchById(pictureList);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "修改失败");
    }

    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        // 获取图片信息
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        Picture picture = Optional.ofNullable(this.getById(pictureId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));
        // 权限校验
//        checkPictureAuth(loginUser, picture);
        // 构造请求参数
        CreateOutPaintingTaskRequest taskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getUrl());
        taskRequest.setInput(input);
        BeanUtil.copyProperties(createPictureOutPaintingTaskRequest, taskRequest);
        // 创建任务
        return aliYunAiApi.createOutPaintingTask(taskRequest);
    }


    /**
     * 根据名称规则修改图片名称
     * @param pictureList 图片列表
     * @param nameRule 名称规则
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        } catch (Exception e) {
            log.error("名称解析错误", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
    }


}
