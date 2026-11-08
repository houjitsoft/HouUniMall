package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CMS内容管理服务Feign客户�?
/**
 * 
 * @description 调用paas中台CMS内容管理服务(cms)的接�?
 * @author Amos
 * @date 2024-12-20
 */
@FeignClient(name = "cms", url = "${feign.cms.url:}", path = "/plugin/cms", fallback = CmsServiceFeignFallback.class, contextId = "cms")
public interface CmsServiceFeign {

    // ================= 文章管理接口 =================
    
    /**
     * 查询文章列表
     */
    @PostMapping("/article/query")
    ResponseResult queryArticleList(@RequestBody Map<String, Object> param);
    
    /**
     * 查询文章详情
     */
    @GetMapping("/article/{id}")
    ResponseResult getArticleDetail(@PathVariable("id") Long id);
    
    /**
     * 创建文章
     */
    @PostMapping("/article/add")
    ResponseResult createArticle(@RequestBody Map<String, Object> articleData);
    
    /**
     * 更新文章
     */
    @PostMapping("/article/update")
    ResponseResult updateArticle(@RequestBody Map<String, Object> articleData);
    
    /**
     * 删除文章
     */
    @PostMapping("/article/delete")
    ResponseResult deleteArticle(@RequestBody Map<String, Object> param);
    
    /**
     * 更新文章状态
     */
    @PostMapping("/article/status")
    ResponseResult updateArticleStatus(@RequestBody Map<String, Object> param);
    
    /**
     * 文章点赞
     */
    @PostMapping("/article/like")
    ResponseResult likeArticle(@RequestBody Map<String, Object> param);
    
    /**
     * 文章收藏
     */
    @PostMapping("/article/collect")
    ResponseResult collectArticle(@RequestBody Map<String, Object> param);

    // ================= 前台文章接口（mall-pc 店面） =================

    /**
     * 前台文章分页列表（匿名可读）
     */
    @GetMapping("/front/article/list")
    ResponseResult frontArticleList(@RequestParam("page") Integer page,
                                    @RequestParam("limit") Integer limit,
                                    @RequestParam(value = "categoryId", required = false) Long categoryId,
                                    @RequestParam(value = "keyword", required = false) String keyword);

    /**
     * 前台文章详情（匿名可读）
     */
    @GetMapping("/front/article/detail/{id}")
    ResponseResult frontArticleDetail(@PathVariable("id") Long id);

    /**
     * 前台热门文章（匿名可读）
     */
    @GetMapping("/front/article/hot")
    ResponseResult frontHotArticles(@RequestParam("limit") Integer limit);

    /**
     * 前台文章分类列表（匿名可读）
     */
    @GetMapping("/front/category/list")
    ResponseResult frontCategoryList();

    /**
     * 收藏文章（需登录）
     */
    @PostMapping("/article/collect/add")
    ResponseResult articleCollectAdd(@RequestBody Map<String, Object> param);

    /**
     * 取消收藏文章（需登录）
     */
    @PostMapping("/article/collect/cancel")
    ResponseResult articleCollectCancel(@RequestBody Map<String, Object> param);

    /**
     * 我的收藏列表（需登录）
     */
    @GetMapping("/article/collect/list")
    ResponseResult articleCollectList(@RequestParam("page") Integer page,
                                      @RequestParam("limit") Integer limit);
    
    // ================= 栏目管理接口 =================
    
    /**
     * 查询栏目列表
     */
    @PostMapping("/category/query")
    ResponseResult queryCategoryList(@RequestBody Map<String, Object> param);
    
    /**
     * 创建栏目
     */
    @PostMapping("/category/add")
    ResponseResult createCategory(@RequestBody Map<String, Object> categoryData);
    
    /**
     * 更新栏目
     */
    @PostMapping("/category/update")
    ResponseResult updateCategory(@RequestBody Map<String, Object> categoryData);
    
    /**
     * 删除栏目
     */
    @PostMapping("/category/delete")
    ResponseResult deleteCategory(@RequestBody Map<String, Object> param);
    
    // ================= 标签管理接口 =================
    
    /**
     * 查询标签列表
     */
    @PostMapping("/tag/query")
    ResponseResult queryTagList(@RequestBody Map<String, Object> param);
    
    /**
     * 创建标签
     */
    @PostMapping("/tag/add")
    ResponseResult createTag(@RequestBody Map<String, Object> tagData);
    
    /**
     * 更新标签
     */
    @PostMapping("/tag/update")
    ResponseResult updateTag(@RequestBody Map<String, Object> tagData);
    
    /**
     * 删除标签
     */
    @PostMapping("/tag/delete")
    ResponseResult deleteTag(@RequestBody Map<String, Object> param);
    
    // ================= 评论管理接口 =================
    
    /**
     * 查询评论列表
     */
    @PostMapping("/comment/query")
    ResponseResult queryCommentList(@RequestBody Map<String, Object> param);
    
    /**
     * 添加评论
     */
    @PostMapping("/comment/add")
    ResponseResult addComment(@RequestBody Map<String, Object> commentData);
    
    /**
     * 审核评论
     */
    @PostMapping("/comment/audit")
    ResponseResult auditComment(@RequestBody Map<String, Object> param);
    
    /**
     * 删除评论
     */
    @PostMapping("/comment/delete")
    ResponseResult deleteComment(@RequestBody Map<String, Object> param);
    
    /**
     * 批量审核评论
     */
    @PostMapping("/comment/batch-audit")
    ResponseResult batchAuditComment(@RequestBody Map<String, Object> params);
    
    /**
     * 批量删除评论
     */
    @PostMapping("/comment/batch-delete")
    ResponseResult batchDeleteComment(@RequestBody Map<String, Object> params);
    
    // ================= 站点配置接口 =================
    
    /**
     * 获取站点配置
     */
    @PostMapping("/site/config/get")
    ResponseResult getSiteConfig(@RequestBody Map<String, Object> params);
    
    /**
     * 保存站点配置
     */
    @PostMapping("/site/config/save")
    ResponseResult saveSiteConfig(@RequestBody Map<String, Object> params);
    
    /**
     * 清除CMS缓存
     */
    @PostMapping("/site/config/clear-cache")
    ResponseResult clearCache();
    
    // ================= 统计控制台接口 =================
    
    /**
     * 获取统计控制台数据
     */
    @PostMapping("/statistics/dashboard")
    ResponseResult getDashboardStatistics();
    
    // ================= 栏目管理接口（新增） =================
    
    /**
     * 获取栏目列表
     */
    @PostMapping("/channel/list")
    ResponseResult getChannelList(@RequestBody Map<String, Object> params);
    
    /**
     * 获取栏目详情
     */
    @PostMapping("/channel/detail")
    ResponseResult getChannelDetail(@RequestBody Map<String, Object> params);
    
    /**
     * 创建栏目
     */
    @PostMapping("/channel/create")
    ResponseResult createChannel(@RequestBody Map<String, Object> params);
    
    /**
     * 更新栏目
     */
    @PostMapping("/channel/update")
    ResponseResult updateChannel(@RequestBody Map<String, Object> params);
    
    /**
     * 删除栏目
     */
    @PostMapping("/channel/delete")
    ResponseResult deleteChannel(@RequestBody Map<String, Object> params);
    
    /**
     * 批量删除栏目
     */
    @PostMapping("/channel/batch-delete")
    ResponseResult batchDeleteChannel(@RequestBody Map<String, Object> params);
    
    /**
     * 获取栏目树
     */
    @PostMapping("/channel/tree")
    ResponseResult getChannelTree();
    
    /**
     * 更新栏目排序
     */
    @PostMapping("/channel/update-weigh")
    ResponseResult updateChannelWeigh(@RequestBody Map<String, Object> params);
    
    /**
     * 更新栏目状态
     */
    @PostMapping("/channel/update-status")
    ResponseResult updateChannelStatus(@RequestBody Map<String, Object> params);
    
    // ================= 内容管理接口（新增） =================
    
    /**
     * 获取内容列表
     */
    @PostMapping("/archives/list")
    ResponseResult getArchivesList(@RequestBody Map<String, Object> params);
    
    /**
     * 获取内容详情
     */
    @PostMapping("/archives/detail")
    ResponseResult getArchivesDetail(@RequestBody Map<String, Object> params);
    
    /**
     * 创建内容
     */
    @PostMapping("/archives/create")
    ResponseResult createArchives(@RequestBody Map<String, Object> params);
    
    /**
     * 更新内容
     */
    @PostMapping("/archives/update")
    ResponseResult updateArchives(@RequestBody Map<String, Object> params);
    
    /**
     * 删除内容
     */
    @PostMapping("/archives/delete")
    ResponseResult deleteArchives(@RequestBody Map<String, Object> params);
    
    /**
     * 批量删除内容
     */
    @PostMapping("/archives/batch-delete")
    ResponseResult batchDeleteArchives(@RequestBody Map<String, Object> params);
    
    /**
     * 移动内容
     */
    @PostMapping("/archives/move")
    ResponseResult moveArchives(@RequestBody Map<String, Object> params);
    
    /**
     * 更新内容状态
     */
    @PostMapping("/archives/update-status")
    ResponseResult updateArchivesStatus(@RequestBody Map<String, Object> params);
    
    /**
     * 批量更新内容状态
     */
    @PostMapping("/archives/batch-update-status")
    ResponseResult batchUpdateArchivesStatus(@RequestBody Map<String, Object> params);
    
    // ================= 模型管理接口 =================
    
    /**
     * 获取模型列表
     */
    @PostMapping("/model/list")
    ResponseResult getModelList(@RequestBody Map<String, Object> params);
    
    /**
     * 获取模型详情
     */
    @PostMapping("/model/detail")
    ResponseResult getModelDetail(@RequestBody Map<String, Object> params);
    
    /**
     * 创建模型
     */
    @PostMapping("/model/create")
    ResponseResult createModel(@RequestBody Map<String, Object> params);
    
    /**
     * 更新模型
     */
    @PostMapping("/model/update")
    ResponseResult updateModel(@RequestBody Map<String, Object> params);
    
    /**
     * 删除模型
     */
    @PostMapping("/model/delete")
    ResponseResult deleteModel(@RequestBody Map<String, Object> params);
    
    /**
     * 批量删除模型
     */
    @PostMapping("/model/batch-delete")
    ResponseResult batchDeleteModel(@RequestBody Map<String, Object> params);
    
    /**
     * 复制模型
     */
    @PostMapping("/model/copy")
    ResponseResult copyModel(@RequestBody Map<String, Object> params);
    
    // ================= 单页管理接口 =================
    
    /**
     * 获取单页列表
     */
    @PostMapping("/page/list")
    ResponseResult getPageList(@RequestBody Map<String, Object> params);
    
    /**
     * 获取单页详情
     */
    @PostMapping("/page/detail")
    ResponseResult getPageDetail(@RequestBody Map<String, Object> params);
    
    /**
     * 创建单页
     */
    @PostMapping("/page/create")
    ResponseResult createPage(@RequestBody Map<String, Object> params);
    
    /**
     * 更新单页
     */
    @PostMapping("/page/update")
    ResponseResult updatePage(@RequestBody Map<String, Object> params);
    
    /**
     * 删除单页
     */
    @PostMapping("/page/delete")
    ResponseResult deletePage(@RequestBody Map<String, Object> params);
    
    /**
     * 批量删除单页
     */
    @PostMapping("/page/batch-delete")
    ResponseResult batchDeletePage(@RequestBody Map<String, Object> params);
    
    /**
     * 恢复单页
     */
    @PostMapping("/page/restore")
    ResponseResult restorePage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取回收站列表
     */
    @PostMapping("/page/recycle-bin")
    ResponseResult getRecycleBinList(@RequestBody Map<String, Object> params);
    
    // ================= 评论管理接口 =================
    
    /**
     * 获取评论列表
     */
    @PostMapping("/comment/list")
    ResponseResult getCommentList(@RequestBody Map<String, Object> params);
    
    /**
     * 获取评论详情
     */
    @PostMapping("/comment/detail")
    ResponseResult getCommentDetail(@RequestBody Map<String, Object> params);
    
    
    /**
     * 恢复评论
     */
    @PostMapping("/comment/restore")
    ResponseResult restoreComment(@RequestBody Map<String, Object> params);
    
    /**
     * 获取回收站列表
     */
    @PostMapping("/comment/recycle-bin")
    ResponseResult getCommentRecycleBinList(@RequestBody Map<String, Object> params);
    
    // ================= 自动链接管理接口 =================
    
    /**
     * 获取自动链接列表
     */
    @PostMapping("/autolink/list")
    ResponseResult getAutolinkList(@RequestBody Map<String, Object> params);
    
    /**
     * 获取自动链接详情
     */
    @PostMapping("/autolink/detail")
    ResponseResult getAutolinkDetail(@RequestBody Map<String, Object> params);
    
    /**
     * 创建自动链接
     */
    @PostMapping("/autolink/create")
    ResponseResult createAutolink(@RequestBody Map<String, Object> params);
    
    /**
     * 更新自动链接
     */
    @PostMapping("/autolink/update")
    ResponseResult updateAutolink(@RequestBody Map<String, Object> params);
    
    /**
     * 删除自动链接
     */
    @PostMapping("/autolink/delete")
    ResponseResult deleteAutolink(@RequestBody Map<String, Object> params);
    
    /**
     * 批量删除自动链接
     */
    @PostMapping("/autolink/batch-delete")
    ResponseResult batchDeleteAutolink(@RequestBody Map<String, Object> params);
    
    // ================= 区块管理接口 =================
    
    /**
     * 获取区块列表
     */
    @PostMapping("/block/list")
    ResponseResult getBlockList(@RequestBody Map<String, Object> params);
    
    /**
     * 获取区块详情
     */
    @PostMapping("/block/detail")
    ResponseResult getBlockDetail(@RequestBody Map<String, Object> params);
    
    /**
     * 创建区块
     */
    @PostMapping("/block/create")
    ResponseResult createBlock(@RequestBody Map<String, Object> params);
    
    /**
     * 更新区块
     */
    @PostMapping("/block/update")
    ResponseResult updateBlock(@RequestBody Map<String, Object> params);
    
    /**
     * 删除区块
     */
    @PostMapping("/block/delete")
    ResponseResult deleteBlock(@RequestBody Map<String, Object> params);
    
    /**
     * 批量删除区块
     */
    @PostMapping("/block/batch-delete")
    ResponseResult batchDeleteBlock(@RequestBody Map<String, Object> params);
    
    // ================= 订单管理接口 =================
    
    /**
     * 获取订单列表
     */
    @PostMapping("/order/list")
    ResponseResult getOrderList(@RequestBody Map<String, Object> params);
    
    /**
     * 获取订单详情
     */
    @PostMapping("/order/detail")
    ResponseResult getOrderDetail(@RequestBody Map<String, Object> params);
    
    /**
     * 更新订单状态
     */
    @PostMapping("/order/update-status")
    ResponseResult updateOrderStatus(@RequestBody Map<String, Object> params);
    
    /**
     * 删除订单
     */
    @PostMapping("/order/delete")
    ResponseResult deleteOrder(@RequestBody Map<String, Object> params);
    
    /**
     * 批量删除订单
     */
    @PostMapping("/order/batch-delete")
    ResponseResult batchDeleteOrder(@RequestBody Map<String, Object> params);
}
