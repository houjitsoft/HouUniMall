package com.houjiang.mall.controller.content;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.content.Selection;
import com.houjiang.mall.entity.content.TopicCategory;
import com.houjiang.mall.service.content.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 发现内容Controller
 * 包含话题分类(6接口) + 选题(7接口) + 心得(5接口) + 评论(4接口) = 共22个端点（前端用17个）
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    // ========== 话题分类 ==========

    /**
     * 分页查询话题分类列表
     *
     * @param params 查询参数
     * @return ResponseResult
     */
    @PostMapping("/topic/category/list")
    public ResponseResult topicCategoryList(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(contentService.topicCategoryPageList(params));
        } catch (Exception e) {
            log.error("查询话题分类列表失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取话题分类详情
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/topic/category/detail")
    public ResponseResult topicCategoryDetail(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            TopicCategory c = contentService.getTopicCategory(id);
            return c != null ? ResponseResult.success(c) : ResponseResult.fail(404, "分类不存在");
        } catch (Exception e) {
            log.error("查询话题分类详情失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建话题分类
     *
     * @param category 分类实体
     * @return ResponseResult
     */
    @PostMapping("/topic/category/create")
    public ResponseResult topicCategoryCreate(@RequestBody TopicCategory category) {
        try {
            return contentService.saveOrUpdateTopicCategory(category) ? ResponseResult.success("创建成功") : ResponseResult.fail(500, "创建失败");
        } catch (Exception e) {
            log.error("创建话题分类失败", e);
            return ResponseResult.fail(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新话题分类
     *
     * @param category 分类实体
     * @return ResponseResult
     */
    @PostMapping("/topic/category/update")
    public ResponseResult topicCategoryUpdate(@RequestBody TopicCategory category) {
        try {
            return contentService.saveOrUpdateTopicCategory(category) ? ResponseResult.success("更新成功") : ResponseResult.fail(500, "更新失败");
        } catch (Exception e) {
            log.error("更新话题分类失败", e);
            return ResponseResult.fail(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除话题分类
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/topic/category/delete")
    public ResponseResult topicCategoryDelete(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return contentService.deleteTopicCategory(id) ? ResponseResult.success("删除成功") : ResponseResult.fail(500, "删除失败");
        } catch (Exception e) {
            log.error("删除话题分类失败", e);
            return ResponseResult.fail(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 切换话题分类状态
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/topic/category/toggle-status")
    public ResponseResult topicCategoryToggleStatus(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return contentService.toggleTopicCategoryStatus(id) ? ResponseResult.success("操作成功") : ResponseResult.fail(500, "操作失败");
        } catch (Exception e) {
            log.error("切换话题分类状态失败", e);
            return ResponseResult.fail(500, "操作失败: " + e.getMessage());
        }
    }

    // ========== 选题 ==========

    /**
     * 分页查询选题列表
     *
     * @param params 查询参数
     * @return ResponseResult
     */
    @PostMapping("/selection/list")
    public ResponseResult selectionList(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(contentService.selectionPageList(params));
        } catch (Exception e) {
            log.error("查询选题列表失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取选题详情
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/selection/detail")
    public ResponseResult selectionDetail(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            Selection s = contentService.getSelection(id);
            return s != null ? ResponseResult.success(s) : ResponseResult.fail(404, "选题不存在");
        } catch (Exception e) {
            log.error("查询选题详情失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建选题
     *
     * @param selection 选题实体
     * @return ResponseResult
     */
    @PostMapping("/selection/create")
    public ResponseResult selectionCreate(@RequestBody Selection selection) {
        try {
            return contentService.saveOrUpdateSelection(selection) ? ResponseResult.success("创建成功") : ResponseResult.fail(500, "创建失败");
        } catch (Exception e) {
            log.error("创建选题失败", e);
            return ResponseResult.fail(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新选题
     *
     * @param selection 选题实体
     * @return ResponseResult
     */
    @PostMapping("/selection/update")
    public ResponseResult selectionUpdate(@RequestBody Selection selection) {
        try {
            return contentService.saveOrUpdateSelection(selection) ? ResponseResult.success("更新成功") : ResponseResult.fail(500, "更新失败");
        } catch (Exception e) {
            log.error("更新选题失败", e);
            return ResponseResult.fail(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除选题
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/selection/delete")
    public ResponseResult selectionDelete(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return contentService.deleteSelection(id) ? ResponseResult.success("删除成功") : ResponseResult.fail(500, "删除失败");
        } catch (Exception e) {
            log.error("删除选题失败", e);
            return ResponseResult.fail(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 发布选题
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/selection/publish")
    public ResponseResult selectionPublish(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return contentService.publishSelection(id) ? ResponseResult.success("发布成功") : ResponseResult.fail(500, "发布失败");
        } catch (Exception e) {
            log.error("发布选题失败", e);
            return ResponseResult.fail(500, "发布失败: " + e.getMessage());
        }
    }

    /**
     * 下线选题
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/selection/offline")
    public ResponseResult selectionOffline(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return contentService.offlineSelection(id) ? ResponseResult.success("下线成功") : ResponseResult.fail(500, "下线失败");
        } catch (Exception e) {
            log.error("下线选题失败", e);
            return ResponseResult.fail(500, "下线失败: " + e.getMessage());
        }
    }

    // ========== 心得 ==========

    /**
     * 分页查询心得列表
     *
     * @param params 查询参数
     * @return ResponseResult
     */
    @PostMapping("/mood/list")
    public ResponseResult moodList(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(contentService.moodPageList(params));
        } catch (Exception e) {
            log.error("查询心得列表失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取心得详情
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/mood/detail")
    public ResponseResult moodDetail(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return ResponseResult.success(contentService.getMood(id));
        } catch (Exception e) {
            log.error("查询心得详情失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 审核心得
     *
     * @param params 包含id和status字段
     * @return ResponseResult
     */
    @PostMapping("/mood/audit")
    public ResponseResult moodAudit(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            Integer status = Integer.parseInt(params.get("status").toString());
            return contentService.auditMood(id, status) ? ResponseResult.success("审核成功") : ResponseResult.fail(500, "审核失败");
        } catch (Exception e) {
            log.error("审核心得失败", e);
            return ResponseResult.fail(500, "审核失败: " + e.getMessage());
        }
    }

    /**
     * 删除心得
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/mood/delete")
    public ResponseResult moodDelete(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return contentService.deleteMood(id) ? ResponseResult.success("删除成功") : ResponseResult.fail(500, "删除失败");
        } catch (Exception e) {
            log.error("删除心得失败", e);
            return ResponseResult.fail(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 下线心得
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/mood/offline")
    public ResponseResult moodOffline(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return contentService.offlineMood(id) ? ResponseResult.success("下线成功") : ResponseResult.fail(500, "下线失败");
        } catch (Exception e) {
            log.error("下线心得失败", e);
            return ResponseResult.fail(500, "下线失败: " + e.getMessage());
        }
    }

    // ========== 评论 ==========

    /**
     * 分页查询评论列表
     *
     * @param params 查询参数
     * @return ResponseResult
     */
    @PostMapping("/review/list")
    public ResponseResult reviewList(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(contentService.reviewPageList(params));
        } catch (Exception e) {
            log.error("查询评论列表失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取评论详情
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/review/detail")
    public ResponseResult reviewDetail(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return ResponseResult.success(contentService.getReview(id));
        } catch (Exception e) {
            log.error("查询评论详情失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 审核评论
     *
     * @param params 包含id和status字段
     * @return ResponseResult
     */
    @PostMapping("/review/audit")
    public ResponseResult reviewAudit(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            Integer status = Integer.parseInt(params.get("status").toString());
            return contentService.auditReview(id, status) ? ResponseResult.success("审核成功") : ResponseResult.fail(500, "审核失败");
        } catch (Exception e) {
            log.error("审核评论失败", e);
            return ResponseResult.fail(500, "审核失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/review/delete")
    public ResponseResult reviewDelete(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return contentService.deleteReview(id) ? ResponseResult.success("删除成功") : ResponseResult.fail(500, "删除失败");
        } catch (Exception e) {
            log.error("删除评论失败", e);
            return ResponseResult.fail(500, "删除失败: " + e.getMessage());
        }
    }
}
