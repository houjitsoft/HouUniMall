package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.CmsServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CMS 文章前台控制器（mall-pc 店面文章/资讯页）
 *
 * <p>读接口匿名可访问；收藏写接口依赖请求携带的用户 token，
 * 由 FeignRequestInterceptor 透传给 cms 服务统一认证。
 *
 * @author Amos
 */
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/cms/article")
public class WebCmsArticleController {

    @Autowired
    private CmsServiceFeign cmsServiceFeign;

    /**
     * 解析分页参数，防止前端未传参导致NPE
     */
    private Integer toInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 文章分页列表（兼容 page_no/page、page_size/limit、cate_id/categoryId 别名）
     */
    @GetMapping("/list")
    public ResponseResult list(@RequestParam(required = false) Map<String, Object> query) {
        Map<String, Object> params = query == null ? Map.of() : query;
        Integer page = toInt(params.get("page_no") != null ? params.get("page_no") : params.get("page"), 1);
        Integer limit = toInt(params.get("page_size") != null ? params.get("page_size") : params.get("limit"), 10);
        // 热门排序走独立热门接口
        if ("hot".equals(String.valueOf(params.getOrDefault("sort", "")))) {
            return cmsServiceFeign.frontHotArticles(limit);
        }
        Long categoryId = null;
        Object cateObj = params.get("cate_id") != null ? params.get("cate_id")
                : (params.get("category_id") != null ? params.get("category_id") : params.get("categoryId"));
        if (cateObj != null) {
            try {
                categoryId = Long.valueOf(String.valueOf(cateObj));
            } catch (NumberFormatException ignored) {
                // 分类ID非法时忽略过滤条件
            }
        }
        String keyword = params.get("keyword") != null ? String.valueOf(params.get("keyword")) : null;
        return cmsServiceFeign.frontArticleList(page, limit, categoryId, keyword);
    }

    /**
     * 文章详情
     */
    @GetMapping("/detail")
    public ResponseResult detail(@RequestParam(value = "id", required = false) Long id) {
        if (id == null) {
            return ResponseResult.fail("文章ID不能为空");
        }
        return cmsServiceFeign.frontArticleDetail(id);
    }

    /**
     * 文章分类列表
     */
    @GetMapping("/category")
    public ResponseResult category() {
        return cmsServiceFeign.frontCategoryList();
    }

    /**
     * 热门文章
     */
    @GetMapping("/hot")
    public ResponseResult hot(@RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        return cmsServiceFeign.frontHotArticles(limit);
    }

    /**
     * 收藏文章（需登录）
     */
    @PostMapping("/addCollect")
    public ResponseResult addCollect(@RequestBody Map<String, Object> param) {
        return cmsServiceFeign.articleCollectAdd(param);
    }

    /**
     * 取消收藏文章（需登录）
     */
    @PostMapping("/cancelCollect")
    public ResponseResult cancelCollect(@RequestBody Map<String, Object> param) {
        return cmsServiceFeign.articleCollectCancel(param);
    }

    /**
     * 我的收藏列表（需登录）
     */
    @GetMapping("/collect")
    public ResponseResult collect(@RequestParam(required = false) Map<String, Object> query) {
        Map<String, Object> params = query == null ? Map.of() : query;
        Integer page = toInt(params.get("page_no") != null ? params.get("page_no") : params.get("page"), 1);
        Integer limit = toInt(params.get("page_size") != null ? params.get("page_size") : params.get("limit"), 10);
        return cmsServiceFeign.articleCollectList(page, limit);
    }
}
