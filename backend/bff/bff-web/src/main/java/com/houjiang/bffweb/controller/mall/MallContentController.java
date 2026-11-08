package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 发现内容BFF Controller
/**
 * 通过MallServiceFeign调用mall服务发现内容相关22个接口
 * 包含话题分类(6) + 选题(7) + 心得(5) + 评论(4)
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/content")
@RequiredArgsConstructor
public class MallContentController {

    private final MallServiceFeign mallServiceFeign;

    /** ========== 话题分类 ========== */
    @PostMapping("/topic/category/list")
    public ResponseResult topicCategoryList(@RequestBody Map<String, Object> p) { return mallServiceFeign.getTopicCategoryList(p); }
    @PostMapping("/topic/category/detail")
    public ResponseResult topicCategoryDetail(@RequestBody Map<String, Object> p) { return mallServiceFeign.getTopicCategoryDetail(p); }
    @PostMapping("/topic/category/create")
    public ResponseResult topicCategoryCreate(@RequestBody Map<String, Object> p) { return mallServiceFeign.createTopicCategory(p); }
    @PostMapping("/topic/category/update")
    public ResponseResult topicCategoryUpdate(@RequestBody Map<String, Object> p) { return mallServiceFeign.updateTopicCategory(p); }
    @PostMapping("/topic/category/delete")
    public ResponseResult topicCategoryDelete(@RequestBody Map<String, Object> p) { return mallServiceFeign.deleteTopicCategory(p); }
    @PostMapping("/topic/category/toggle-status")
    public ResponseResult topicCategoryToggleStatus(@RequestBody Map<String, Object> p) { return mallServiceFeign.toggleTopicCategoryStatus(p); }

    /** ========== 选题 ========== */
    @PostMapping("/selection/list")
    public ResponseResult selectionList(@RequestBody Map<String, Object> p) { return mallServiceFeign.getSelectionList(p); }
    @PostMapping("/selection/detail")
    public ResponseResult selectionDetail(@RequestBody Map<String, Object> p) { return mallServiceFeign.getSelectionDetail(p); }
    @PostMapping("/selection/create")
    public ResponseResult selectionCreate(@RequestBody Map<String, Object> p) { return mallServiceFeign.createSelection(p); }
    @PostMapping("/selection/update")
    public ResponseResult selectionUpdate(@RequestBody Map<String, Object> p) { return mallServiceFeign.updateSelection(p); }
    @PostMapping("/selection/delete")
    public ResponseResult selectionDelete(@RequestBody Map<String, Object> p) { return mallServiceFeign.deleteSelection(p); }
    @PostMapping("/selection/publish")
    public ResponseResult selectionPublish(@RequestBody Map<String, Object> p) { return mallServiceFeign.publishSelection(p); }
    @PostMapping("/selection/offline")
    public ResponseResult selectionOffline(@RequestBody Map<String, Object> p) { return mallServiceFeign.offlineSelection(p); }

    /** ========== 心得 ========== */
    @PostMapping("/mood/list")
    public ResponseResult moodList(@RequestBody Map<String, Object> p) { return mallServiceFeign.getMoodList(p); }
    @PostMapping("/mood/detail")
    public ResponseResult moodDetail(@RequestBody Map<String, Object> p) { return mallServiceFeign.getMoodDetail(p); }
    @PostMapping("/mood/audit")
    public ResponseResult moodAudit(@RequestBody Map<String, Object> p) { return mallServiceFeign.auditMood(p); }
    @PostMapping("/mood/delete")
    public ResponseResult moodDelete(@RequestBody Map<String, Object> p) { return mallServiceFeign.deleteMood(p); }
    @PostMapping("/mood/offline")
    public ResponseResult moodOffline(@RequestBody Map<String, Object> p) { return mallServiceFeign.offlineMood(p); }

    /** ========== 评论 ========== */
    @PostMapping("/review/list")
    public ResponseResult reviewList(@RequestBody Map<String, Object> p) { return mallServiceFeign.getReviewList(p); }
    @PostMapping("/review/detail")
    public ResponseResult reviewDetail(@RequestBody Map<String, Object> p) { return mallServiceFeign.getReviewDetail(p); }
    @PostMapping("/review/audit")
    public ResponseResult reviewAudit(@RequestBody Map<String, Object> p) { return mallServiceFeign.auditReview(p); }
    @PostMapping("/review/delete")
    public ResponseResult reviewDelete(@RequestBody Map<String, Object> p) { return mallServiceFeign.deleteReview(p); }
}
