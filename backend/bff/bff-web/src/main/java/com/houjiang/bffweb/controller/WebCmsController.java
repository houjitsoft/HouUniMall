package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.util.ResponseResult;
import com.houjiang.bffweb.feign.CmsServiceFeign;
import com.houjiang.bffweb.feign.UcmsServiceFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Cms服务BFF适配器
/**
 * 只做请求转发，不包含业务逻辑
 * 
 * @author Amos
 * @since 2024-12-29
 */
@Slf4j
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/cms")
public class WebCmsController {

    @Autowired
    private UcmsServiceFeign ucmsServiceFeign;
    
    @Autowired
    private CmsServiceFeign cmsServiceFeign;

    // ================= Cms管理接口 =================

    /**
     * 获取cms统计数据
     */
    @PostMapping("/manage/statistics")
    public ResponseResult getStatistics() {
        return ucmsServiceFeign.getUcmsStatistics(null);
    }

    /**
     * 分页查询cms列表
     */
    @PostMapping("/manage/list")
    public ResponseResult getCmsList(@RequestBody Map<String, Object> queryParams) {
        return ucmsServiceFeign.queryUcmsList(queryParams);
    }

    /**
     * 查询cms详情
     */
    @PostMapping("/manage/detail")
    public ResponseResult getCmsDetail(@RequestBody Map<String, Object> param) {
        return ucmsServiceFeign.getUcmsDetail(param);
    }

    /**
     * 创建cms
     */
    @PostMapping("/manage/create")
    public ResponseResult createCms(@RequestBody Map<String, Object> cmsData) {
        return ucmsServiceFeign.createUcms(cmsData);
    }

    /**
     * 更新cms
     */
    @PostMapping("/manage/update")
    public ResponseResult updateCms(@RequestBody Map<String, Object> cmsData) {
        return ucmsServiceFeign.updateUcms(cmsData);
    }

    /**
     * 删除cms
     */
    @PostMapping("/manage/delete")
    public ResponseResult deleteCms(@RequestBody Map<String, Object> param) {
        return ucmsServiceFeign.deleteUcms(param);
    }

    /**
     * 暂停cms
     */
    @PostMapping("/manage/pause")
    public ResponseResult pauseCms(@RequestBody Map<String, Object> param) {
        return ucmsServiceFeign.pauseUcms(param);
    }

    /**
     * 恢复cms
     */
    @PostMapping("/manage/resume")
    public ResponseResult resumeCms(@RequestBody Map<String, Object> param) {
        return ucmsServiceFeign.resumeUcms(param);
    }

    /**
     * 复制cms
     */
    @PostMapping("/manage/copy")
    public ResponseResult copyCms(@RequestBody Map<String, Object> param) {
        return ucmsServiceFeign.copyUcms(param);
    }

    /**
     * 获取cms类型列表
     */
    @GetMapping("/manage/types")
    public ResponseResult getCmsTypes() {
        return ucmsServiceFeign.getUcmsTypes(null);
    }

    /**
     * 批量操作cms状态
     */
    @PostMapping("/manage/batch/status")
    public ResponseResult batchUpdateStatus(@RequestBody Map<String, Object> param) {
        return ucmsServiceFeign.batchUpdateStatus(param);
    }
    
    // ================= 站点配置接口 =================
    
    /**
     * 获取站点配置
     */
    @PostMapping("/site/config/get")
    public ResponseResult getSiteConfig(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取站点配置，参数: {}", params);
            return cmsServiceFeign.getSiteConfig(params);
        } catch (Exception e) {
            log.error("获取站点配置失败", e);
            return ResponseResult.error("获取站点配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存站点配置
     */
    @PostMapping("/site/config/save")
    public ResponseResult saveSiteConfig(@RequestBody Map<String, Object> params) {
        try {
            log.info("保存站点配置，参数: {}", params);
            return cmsServiceFeign.saveSiteConfig(params);
        } catch (Exception e) {
            log.error("保存站点配置失败", e);
            return ResponseResult.error("保存站点配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除CMS缓存
     */
    @PostMapping("/site/config/clear-cache")
    public ResponseResult clearCache() {
        try {
            return cmsServiceFeign.clearCache();
        } catch (Exception e) {
            log.error("清除缓存失败", e);
            return ResponseResult.error("清除缓存失败: " + e.getMessage());
        }
    }
    
    // ================= 统计控制台接口 =================
    
    /**
     * 获取统计控制台数据
     */
    @PostMapping("/statistics/dashboard")
    public ResponseResult getDashboardStatistics() {
        try {
            log.info("获取统计控制台数据");
            return cmsServiceFeign.getDashboardStatistics();
        } catch (Exception e) {
            log.error("获取统计控制台数据失败", e);
            return ResponseResult.error("获取统计控制台数据失败: " + e.getMessage());
        }
    }
    
    // ================= 栏目管理接口 =================
    
    /**
     * 获取栏目列表
     */
    @PostMapping("/channel/list")
    public ResponseResult getChannelList(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取栏目列表，参数: {}", params);
            return cmsServiceFeign.getChannelList(params);
        } catch (Exception e) {
            log.error("获取栏目列表失败", e);
            return ResponseResult.error("获取栏目列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取栏目详情
     */
    @PostMapping("/channel/detail")
    public ResponseResult getChannelDetail(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.getChannelDetail(params);
        } catch (Exception e) {
            log.error("获取栏目详情失败", e);
            return ResponseResult.error("获取栏目详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建栏目
     */
    @PostMapping("/channel/create")
    public ResponseResult createChannel(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.createChannel(params);
        } catch (Exception e) {
            log.error("创建栏目失败", e);
            return ResponseResult.error("创建栏目失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新栏目
     */
    @PostMapping("/channel/update")
    public ResponseResult updateChannel(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.updateChannel(params);
        } catch (Exception e) {
            log.error("更新栏目失败", e);
            return ResponseResult.error("更新栏目失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除栏目
     */
    @PostMapping("/channel/delete")
    public ResponseResult deleteChannel(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.deleteChannel(params);
        } catch (Exception e) {
            log.error("删除栏目失败", e);
            return ResponseResult.error("删除栏目失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除栏目
     */
    @PostMapping("/channel/batch-delete")
    public ResponseResult batchDeleteChannel(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.batchDeleteChannel(params);
        } catch (Exception e) {
            log.error("批量删除栏目失败", e);
            return ResponseResult.error("批量删除栏目失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取栏目树
     */
    @PostMapping("/channel/tree")
    public ResponseResult getChannelTree() {
        try {
            return cmsServiceFeign.getChannelTree();
        } catch (Exception e) {
            log.error("获取栏目树失败", e);
            return ResponseResult.error("获取栏目树失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新栏目排序
     */
    @PostMapping("/channel/update-weigh")
    public ResponseResult updateChannelWeigh(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.updateChannelWeigh(params);
        } catch (Exception e) {
            log.error("更新栏目排序失败", e);
            return ResponseResult.error("更新栏目排序失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新栏目状态
     */
    @PostMapping("/channel/update-status")
    public ResponseResult updateChannelStatus(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.updateChannelStatus(params);
        } catch (Exception e) {
            log.error("更新栏目状态失败", e);
            return ResponseResult.error("更新栏目状态失败: " + e.getMessage());
        }
    }
    
    // ================= 内容管理接口 =================
    
    /**
     * 获取内容列表
     */
    @PostMapping("/archives/list")
    public ResponseResult getArchivesList(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取内容列表，参数: {}", params);
            return cmsServiceFeign.getArchivesList(params);
        } catch (Exception e) {
            log.error("获取内容列表失败", e);
            return ResponseResult.error("获取内容列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取内容详情
     */
    @PostMapping("/archives/detail")
    public ResponseResult getArchivesDetail(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.getArchivesDetail(params);
        } catch (Exception e) {
            log.error("获取内容详情失败", e);
            return ResponseResult.error("获取内容详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建内容
     */
    @PostMapping("/archives/create")
    public ResponseResult createArchives(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.createArchives(params);
        } catch (Exception e) {
            log.error("创建内容失败", e);
            return ResponseResult.error("创建内容失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新内容
     */
    @PostMapping("/archives/update")
    public ResponseResult updateArchives(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.updateArchives(params);
        } catch (Exception e) {
            log.error("更新内容失败", e);
            return ResponseResult.error("更新内容失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除内容
     */
    @PostMapping("/archives/delete")
    public ResponseResult deleteArchives(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.deleteArchives(params);
        } catch (Exception e) {
            log.error("删除内容失败", e);
            return ResponseResult.error("删除内容失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除内容
     */
    @PostMapping("/archives/batch-delete")
    public ResponseResult batchDeleteArchives(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.batchDeleteArchives(params);
        } catch (Exception e) {
            log.error("批量删除内容失败", e);
            return ResponseResult.error("批量删除内容失败: " + e.getMessage());
        }
    }
    
    /**
     * 移动内容
     */
    @PostMapping("/archives/move")
    public ResponseResult moveArchives(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.moveArchives(params);
        } catch (Exception e) {
            log.error("移动内容失败", e);
            return ResponseResult.error("移动内容失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新内容状态
     */
    @PostMapping("/archives/update-status")
    public ResponseResult updateArchivesStatus(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.updateArchivesStatus(params);
        } catch (Exception e) {
            log.error("更新内容状态失败", e);
            return ResponseResult.error("更新内容状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新内容状态
     */
    @PostMapping("/archives/batch-update-status")
    public ResponseResult batchUpdateArchivesStatus(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.batchUpdateArchivesStatus(params);
        } catch (Exception e) {
            log.error("批量更新内容状态失败", e);
            return ResponseResult.error("批量更新内容状态失败: " + e.getMessage());
        }
    }
    
    // ================= 模型管理接口 =================
    
    /**
     * 获取模型列表
     */
    @PostMapping("/model/list")
    public ResponseResult getModelList(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.getModelList(params);
        } catch (Exception e) {
            log.error("获取模型列表失败", e);
            return ResponseResult.error("获取模型列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取模型详情
     */
    @PostMapping("/model/detail")
    public ResponseResult getModelDetail(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.getModelDetail(params);
        } catch (Exception e) {
            log.error("获取模型详情失败", e);
            return ResponseResult.error("获取模型详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建模型
     */
    @PostMapping("/model/create")
    public ResponseResult createModel(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.createModel(params);
        } catch (Exception e) {
            log.error("创建模型失败", e);
            return ResponseResult.error("创建模型失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新模型
     */
    @PostMapping("/model/update")
    public ResponseResult updateModel(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.updateModel(params);
        } catch (Exception e) {
            log.error("更新模型失败", e);
            return ResponseResult.error("更新模型失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除模型
     */
    @PostMapping("/model/delete")
    public ResponseResult deleteModel(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.deleteModel(params);
        } catch (Exception e) {
            log.error("删除模型失败", e);
            return ResponseResult.error("删除模型失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除模型
     */
    @PostMapping("/model/batch-delete")
    public ResponseResult batchDeleteModel(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.batchDeleteModel(params);
        } catch (Exception e) {
            log.error("批量删除模型失败", e);
            return ResponseResult.error("批量删除模型失败: " + e.getMessage());
        }
    }
    
    /**
     * 复制模型
     */
    @PostMapping("/model/copy")
    public ResponseResult copyModel(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.copyModel(params);
        } catch (Exception e) {
            log.error("复制模型失败", e);
            return ResponseResult.error("复制模型失败: " + e.getMessage());
        }
    }
    
    // ================= 单页管理接口 =================
    
    /**
     * 获取单页列表
     */
    @PostMapping("/page/list")
    public ResponseResult getPageList(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取单页列表，参数: {}", params);
            return cmsServiceFeign.getPageList(params);
        } catch (Exception e) {
            log.error("获取单页列表失败", e);
            return ResponseResult.error("获取单页列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取单页详情
     */
    @PostMapping("/page/detail")
    public ResponseResult getPageDetail(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.getPageDetail(params);
        } catch (Exception e) {
            log.error("获取单页详情失败", e);
            return ResponseResult.error("获取单页详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建单页
     */
    @PostMapping("/page/create")
    public ResponseResult createPage(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.createPage(params);
        } catch (Exception e) {
            log.error("创建单页失败", e);
            return ResponseResult.error("创建单页失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新单页
     */
    @PostMapping("/page/update")
    public ResponseResult updatePage(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.updatePage(params);
        } catch (Exception e) {
            log.error("更新单页失败", e);
            return ResponseResult.error("更新单页失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除单页
     */
    @PostMapping("/page/delete")
    public ResponseResult deletePage(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.deletePage(params);
        } catch (Exception e) {
            log.error("删除单页失败", e);
            return ResponseResult.error("删除单页失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除单页
     */
    @PostMapping("/page/batch-delete")
    public ResponseResult batchDeletePage(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.batchDeletePage(params);
        } catch (Exception e) {
            log.error("批量删除单页失败", e);
            return ResponseResult.error("批量删除单页失败: " + e.getMessage());
        }
    }
    
    /**
     * 恢复单页
     */
    @PostMapping("/page/restore")
    public ResponseResult restorePage(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.restorePage(params);
        } catch (Exception e) {
            log.error("恢复单页失败", e);
            return ResponseResult.error("恢复单页失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取回收站列表
     */
    @PostMapping("/page/recycle-bin")
    public ResponseResult getRecycleBinList(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.getRecycleBinList(params);
        } catch (Exception e) {
            log.error("获取回收站列表失败", e);
            return ResponseResult.error("获取回收站列表失败: " + e.getMessage());
        }
    }
    
    // ================= 评论管理接口 =================
    
    /**
     * 获取评论列表
     */
    @PostMapping("/comment/list")
    public ResponseResult getCommentList(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取评论列表，参数: {}", params);
            return cmsServiceFeign.getCommentList(params);
        } catch (Exception e) {
            log.error("获取评论列表失败", e);
            return ResponseResult.error("获取评论列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取评论详情
     */
    @PostMapping("/comment/detail")
    public ResponseResult getCommentDetail(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.getCommentDetail(params);
        } catch (Exception e) {
            log.error("获取评论详情失败", e);
            return ResponseResult.error("获取评论详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 审核评论
     */
    @PostMapping("/comment/audit")
    public ResponseResult auditComment(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.auditComment(params);
        } catch (Exception e) {
            log.error("审核评论失败", e);
            return ResponseResult.error("审核评论失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量审核评论
     */
    @PostMapping("/comment/batch-audit")
    public ResponseResult batchAuditComment(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.batchAuditComment(params);
        } catch (Exception e) {
            log.error("批量审核评论失败", e);
            return ResponseResult.error("批量审核评论失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除评论
     */
    @PostMapping("/comment/delete")
    public ResponseResult deleteComment(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.deleteComment(params);
        } catch (Exception e) {
            log.error("删除评论失败", e);
            return ResponseResult.error("删除评论失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除评论
     */
    @PostMapping("/comment/batch-delete")
    public ResponseResult batchDeleteComment(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.batchDeleteComment(params);
        } catch (Exception e) {
            log.error("批量删除评论失败", e);
            return ResponseResult.error("批量删除评论失败: " + e.getMessage());
        }
    }
    
    /**
     * 恢复评论
     */
    @PostMapping("/comment/restore")
    public ResponseResult restoreComment(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.restoreComment(params);
        } catch (Exception e) {
            log.error("恢复评论失败", e);
            return ResponseResult.error("恢复评论失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取回收站列表
     */
    @PostMapping("/comment/recycle-bin")
    public ResponseResult getCommentRecycleBinList(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.getCommentRecycleBinList(params);
        } catch (Exception e) {
            log.error("获取回收站列表失败", e);
            return ResponseResult.error("获取回收站列表失败: " + e.getMessage());
        }
    }
    
    // ================= 自动链接管理接口 =================
    
    /**
     * 获取自动链接列表
     */
    @PostMapping("/autolink/list")
    public ResponseResult getAutolinkList(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取自动链接列表，参数: {}", params);
            return cmsServiceFeign.getAutolinkList(params);
        } catch (Exception e) {
            log.error("获取自动链接列表失败", e);
            return ResponseResult.error("获取自动链接列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取自动链接详情
     */
    @PostMapping("/autolink/detail")
    public ResponseResult getAutolinkDetail(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.getAutolinkDetail(params);
        } catch (Exception e) {
            log.error("获取自动链接详情失败", e);
            return ResponseResult.error("获取自动链接详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建自动链接
     */
    @PostMapping("/autolink/create")
    public ResponseResult createAutolink(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.createAutolink(params);
        } catch (Exception e) {
            log.error("创建自动链接失败", e);
            return ResponseResult.error("创建自动链接失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新自动链接
     */
    @PostMapping("/autolink/update")
    public ResponseResult updateAutolink(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.updateAutolink(params);
        } catch (Exception e) {
            log.error("更新自动链接失败", e);
            return ResponseResult.error("更新自动链接失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除自动链接
     */
    @PostMapping("/autolink/delete")
    public ResponseResult deleteAutolink(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.deleteAutolink(params);
        } catch (Exception e) {
            log.error("删除自动链接失败", e);
            return ResponseResult.error("删除自动链接失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除自动链接
     */
    @PostMapping("/autolink/batch-delete")
    public ResponseResult batchDeleteAutolink(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.batchDeleteAutolink(params);
        } catch (Exception e) {
            log.error("批量删除自动链接失败", e);
            return ResponseResult.error("批量删除自动链接失败: " + e.getMessage());
        }
    }
    
    // ================= 区块管理接口 =================
    
    /**
     * 获取区块列表
     */
    @PostMapping("/block/list")
    public ResponseResult getBlockList(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取区块列表，参数: {}", params);
            return cmsServiceFeign.getBlockList(params);
        } catch (Exception e) {
            log.error("获取区块列表失败", e);
            return ResponseResult.error("获取区块列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取区块详情
     */
    @PostMapping("/block/detail")
    public ResponseResult getBlockDetail(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.getBlockDetail(params);
        } catch (Exception e) {
            log.error("获取区块详情失败", e);
            return ResponseResult.error("获取区块详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建区块
     */
    @PostMapping("/block/create")
    public ResponseResult createBlock(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.createBlock(params);
        } catch (Exception e) {
            log.error("创建区块失败", e);
            return ResponseResult.error("创建区块失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新区块
     */
    @PostMapping("/block/update")
    public ResponseResult updateBlock(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.updateBlock(params);
        } catch (Exception e) {
            log.error("更新区块失败", e);
            return ResponseResult.error("更新区块失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除区块
     */
    @PostMapping("/block/delete")
    public ResponseResult deleteBlock(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.deleteBlock(params);
        } catch (Exception e) {
            log.error("删除区块失败", e);
            return ResponseResult.error("删除区块失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除区块
     */
    @PostMapping("/block/batch-delete")
    public ResponseResult batchDeleteBlock(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.batchDeleteBlock(params);
        } catch (Exception e) {
            log.error("批量删除区块失败", e);
            return ResponseResult.error("批量删除区块失败: " + e.getMessage());
        }
    }
    
    // ================= 订单管理接口 =================
    
    /**
     * 获取订单列表
     */
    @PostMapping("/order/list")
    public ResponseResult getOrderList(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取订单列表，参数: {}", params);
            return cmsServiceFeign.getOrderList(params);
        } catch (Exception e) {
            log.error("获取订单列表失败", e);
            return ResponseResult.error("获取订单列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     */
    @PostMapping("/order/detail")
    public ResponseResult getOrderDetail(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.getOrderDetail(params);
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return ResponseResult.error("获取订单详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新订单状态
     */
    @PostMapping("/order/update-status")
    public ResponseResult updateOrderStatus(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.updateOrderStatus(params);
        } catch (Exception e) {
            log.error("更新订单状态失败", e);
            return ResponseResult.error("更新订单状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除订单
     */
    @PostMapping("/order/delete")
    public ResponseResult deleteOrder(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.deleteOrder(params);
        } catch (Exception e) {
            log.error("删除订单失败", e);
            return ResponseResult.error("删除订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除订单
     */
    @PostMapping("/order/batch-delete")
    public ResponseResult batchDeleteOrder(@RequestBody Map<String, Object> params) {
        try {
            return cmsServiceFeign.batchDeleteOrder(params);
        } catch (Exception e) {
            log.error("批量删除订单失败", e);
            return ResponseResult.error("批量删除订单失败: " + e.getMessage());
        }
    }
}
