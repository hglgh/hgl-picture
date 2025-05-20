package com.hgl.hglpicturebackend.api.imageSearch.so.sub;

import cn.hutool.json.JSONUtil;
import com.hgl.hglpicturebackend.api.imageSearch.so.model.SoImageSearchResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * ClassName: SoImageSearchApiFacade
 * Package: com.hgl.hglpicturebackend.api.imageSearch.sub
 * Description: 360搜图图片搜索接口
 *<p></p>
 * 这里用了 门面模式
 * @Author HGL
 * @Create: 2025/4/8 18:27
 */
@Slf4j
public class SoImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl 需要以图搜图的图片地址
     * @param start    开始下表
     * @return 图片搜索结果列表
     */
    public static List<SoImageSearchResult> searchImage(String imageUrl, Integer start) {
        String soImageUrl = GetSoImageUrlApi.getSoImageUrl(imageUrl);
        List<SoImageSearchResult> imageList = GetSoImageListApi.getImageList(soImageUrl, start);
        return imageList;
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
//        String imageUrl = "https://www.codefather.cn/logo.png";
        String imageUrl = "https://hgl-picture-1329340076.cos.ap-guangzhou.myqcloud.com/public/1868668928427843585/2025-02-17_quO1EUpxSlJ6pdua_thumbnail.";
        List<SoImageSearchResult> resultList = searchImage(imageUrl, 0);
        System.out.println("结果列表" + JSONUtil.parse(resultList));
    }
}
