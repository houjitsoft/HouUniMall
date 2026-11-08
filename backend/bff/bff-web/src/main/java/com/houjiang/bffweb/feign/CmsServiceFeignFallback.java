package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CMS内容管理服务Feign降级处理类
/**
 * 
 * @description 当CMS服务不可用时提供降级响应
 * @author Amos
 * @date 2024-12-20
 */
@Component
public class CmsServiceFeignFallback implements CmsServiceFeign {

    @Override
    public ResponseResult queryArticleList(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getArticleDetail(Long id) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult frontArticleList(Integer page, Integer limit, Long categoryId, String keyword) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult frontArticleDetail(Long id) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult frontHotArticles(Integer limit) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult frontCategoryList() {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult articleCollectAdd(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult articleCollectCancel(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult articleCollectList(Integer page, Integer limit) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createArticle(Map<String, Object> articleData) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateArticle(Map<String, Object> articleData) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteArticle(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateArticleStatus(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult likeArticle(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult collectArticle(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryCategoryList(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createCategory(Map<String, Object> categoryData) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateCategory(Map<String, Object> categoryData) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteCategory(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryTagList(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createTag(Map<String, Object> tagData) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateTag(Map<String, Object> tagData) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteTag(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryCommentList(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addComment(Map<String, Object> commentData) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult auditComment(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteComment(Map<String, Object> param) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult batchAuditComment(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult batchDeleteComment(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    /** ================= 站点配置接口降级处理 ================= */
    
    @Override
    public ResponseResult getSiteConfig(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult saveSiteConfig(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult clearCache() {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    /** ================= 统计控制台接口降级处理 ================= */
    
    @Override
    public ResponseResult getDashboardStatistics() {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    /** ================= 栏目管理接口降级处理 ================= */
    
    @Override
    public ResponseResult getChannelList(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getChannelDetail(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult createChannel(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updateChannel(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deleteChannel(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult batchDeleteChannel(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getChannelTree() {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updateChannelWeigh(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updateChannelStatus(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    /** ================= 内容管理接口降级处理 ================= */
    
    @Override
    public ResponseResult getArchivesList(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getArchivesDetail(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult createArchives(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updateArchives(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deleteArchives(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult batchDeleteArchives(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult moveArchives(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updateArchivesStatus(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult batchUpdateArchivesStatus(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    /** ================= 模型管理接口降级处理 ================= */
    
    @Override
    public ResponseResult getModelList(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getModelDetail(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult createModel(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updateModel(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deleteModel(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult batchDeleteModel(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult copyModel(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    /** ================= 单页管理接口降级处理 ================= */
    
    @Override
    public ResponseResult getPageList(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getPageDetail(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult createPage(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updatePage(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deletePage(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult batchDeletePage(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult restorePage(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getRecycleBinList(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    /** ================= 评论管理接口降级处理 ================= */
    
    @Override
    public ResponseResult getCommentList(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getCommentDetail(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    
    @Override
    public ResponseResult restoreComment(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getCommentRecycleBinList(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    /** ================= 自动链接管理接口降级处理 ================= */
    
    @Override
    public ResponseResult getAutolinkList(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getAutolinkDetail(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult createAutolink(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updateAutolink(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deleteAutolink(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult batchDeleteAutolink(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    /** ================= 区块管理接口降级处理 ================= */
    
    @Override
    public ResponseResult getBlockList(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getBlockDetail(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult createBlock(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updateBlock(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deleteBlock(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult batchDeleteBlock(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    /** ================= 订单管理接口降级处理 ================= */
    
    @Override
    public ResponseResult getOrderList(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getOrderDetail(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updateOrderStatus(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deleteOrder(Map<String, Object> params) {
        return ResponseResult.fail("CMS服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult batchDeleteOrder(Map<String, Object> params) {
        return ResponseResult.fail("服务暂时不可用，请稍后重试");
    }
}
