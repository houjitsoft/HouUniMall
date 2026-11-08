package com.houjiang.mall.service.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.mall.entity.content.Mood;
import com.houjiang.mall.entity.content.Review;
import com.houjiang.mall.entity.content.Selection;
import com.houjiang.mall.entity.content.TopicCategory;
import com.houjiang.mall.mapper.content.MoodMapper;
import com.houjiang.mall.mapper.content.ReviewMapper;
import com.houjiang.mall.mapper.content.SelectionMapper;
import com.houjiang.mall.mapper.content.TopicCategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 发现内容聚合服务
 * 包含话题分类、选题、心得、评论的CRUD
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final TopicCategoryMapper topicCategoryMapper;
    private final SelectionMapper selectionMapper;
    private final MoodMapper moodMapper;
    private final ReviewMapper reviewMapper;

    // ========== 话题分类 ==========

    /**
     * 分页查询话题分类列表
     *
     * @param params 查询参数：page, size, name, status
     * @return 分页结果
     */
    public IPage<TopicCategory> topicCategoryPageList(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        LambdaQueryWrapper<TopicCategory> w = new LambdaQueryWrapper<>();
        if (params.get("name") != null && StringUtils.hasText(params.get("name").toString())) {
            w.like(TopicCategory::getName, params.get("name").toString());
        }
        if (params.get("status") != null) {
            w.eq(TopicCategory::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        w.orderByAsc(TopicCategory::getSort).orderByDesc(TopicCategory::getCreateTime);
        return topicCategoryMapper.selectPage(new Page<>(page, size), w);
    }

    /**
     * 获取话题分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    public TopicCategory getTopicCategory(Long id) {
        return topicCategoryMapper.selectById(id);
    }

    /**
     * 新增或更新话题分类
     *
     * @param category 分类实体
     * @return 是否成功
     */
    public boolean saveOrUpdateTopicCategory(TopicCategory category) {
        if (category.getId() != null) {
            return topicCategoryMapper.updateById(category) > 0;
        }
        return topicCategoryMapper.insert(category) > 0;
    }

    /**
     * 删除话题分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    public boolean deleteTopicCategory(Long id) {
        return topicCategoryMapper.deleteById(id) > 0;
    }

    /**
     * 切换话题分类状态
     *
     * @param id 分类ID
     * @return 是否成功
     */
    public boolean toggleTopicCategoryStatus(Long id) {
        TopicCategory c = topicCategoryMapper.selectById(id);
        if (c == null) return false;
        c.setStatus(c.getStatus() == 1 ? 0 : 1);
        return topicCategoryMapper.updateById(c) > 0;
    }

    // ========== 选题 ==========

    /**
     * 分页查询选题列表
     *
     * @param params 查询参数：page, size, title, categoryId, status
     * @return 分页结果
     */
    public IPage<Selection> selectionPageList(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        LambdaQueryWrapper<Selection> w = new LambdaQueryWrapper<>();
        if (params.get("title") != null && StringUtils.hasText(params.get("title").toString())) {
            w.like(Selection::getTitle, params.get("title").toString());
        }
        if (params.get("categoryId") != null) {
            w.eq(Selection::getCategoryId, Long.parseLong(params.get("categoryId").toString()));
        }
        if (params.get("status") != null) {
            w.eq(Selection::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        w.orderByDesc(Selection::getCreateTime);
        return selectionMapper.selectPage(new Page<>(page, size), w);
    }

    /**
     * 获取选题详情
     *
     * @param id 选题ID
     * @return 选题详情
     */
    public Selection getSelection(Long id) {
        return selectionMapper.selectById(id);
    }

    /**
     * 新增或更新选题
     *
     * @param selection 选题实体
     * @return 是否成功
     */
    public boolean saveOrUpdateSelection(Selection selection) {
        if (selection.getId() != null) {
            return selectionMapper.updateById(selection) > 0;
        }
        return selectionMapper.insert(selection) > 0;
    }

    /**
     * 删除选题
     *
     * @param id 选题ID
     * @return 是否成功
     */
    public boolean deleteSelection(Long id) {
        return selectionMapper.deleteById(id) > 0;
    }

    /**
     * 发布选题（状态改为已发布）
     *
     * @param id 选题ID
     * @return 是否成功
     */
    public boolean publishSelection(Long id) {
        Selection s = selectionMapper.selectById(id);
        if (s == null) return false;
        s.setStatus(1);
        s.setPublishTime(LocalDateTime.now());
        return selectionMapper.updateById(s) > 0;
    }

    /**
     * 下线选题（状态改为已下线）
     *
     * @param id 选题ID
     * @return 是否成功
     */
    public boolean offlineSelection(Long id) {
        Selection s = selectionMapper.selectById(id);
        if (s == null) return false;
        s.setStatus(2);
        return selectionMapper.updateById(s) > 0;
    }

    // ========== 心得 ==========

    /**
     * 分页查询心得列表
     *
     * @param params 查询参数：page, size, userName, status
     * @return 分页结果
     */
    public IPage<Mood> moodPageList(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        LambdaQueryWrapper<Mood> w = new LambdaQueryWrapper<>();
        if (params.get("userName") != null && StringUtils.hasText(params.get("userName").toString())) {
            w.like(Mood::getUserName, params.get("userName").toString());
        }
        if (params.get("status") != null) {
            w.eq(Mood::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        w.orderByDesc(Mood::getCreateTime);
        return moodMapper.selectPage(new Page<>(page, size), w);
    }

    /**
     * 获取心得详情
     *
     * @param id 心得ID
     * @return 心得详情
     */
    public Mood getMood(Long id) {
        return moodMapper.selectById(id);
    }

    /**
     * 审核心得
     *
     * @param id 心得ID
     * @param status 审核状态: 1-通过 2-拒绝
     * @return 是否成功
     */
    public boolean auditMood(Long id, Integer status) {
        Mood m = moodMapper.selectById(id);
        if (m == null) return false;
        m.setStatus(status);
        return moodMapper.updateById(m) > 0;
    }

    /**
     * 删除心得
     *
     * @param id 心得ID
     * @return 是否成功
     */
    public boolean deleteMood(Long id) {
        return moodMapper.deleteById(id) > 0;
    }

    /**
     * 下线心得
     *
     * @param id 心得ID
     * @return 是否成功
     */
    public boolean offlineMood(Long id) {
        Mood m = moodMapper.selectById(id);
        if (m == null) return false;
        m.setStatus(3);
        return moodMapper.updateById(m) > 0;
    }

    // ========== 评论 ==========

    /**
     * 分页查询评论列表
     *
     * @param params 查询参数：page, size, targetType, targetId, status
     * @return 分页结果
     */
    public IPage<Review> reviewPageList(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        LambdaQueryWrapper<Review> w = new LambdaQueryWrapper<>();
        if (params.get("targetType") != null && StringUtils.hasText(params.get("targetType").toString())) {
            w.eq(Review::getTargetType, params.get("targetType").toString());
        }
        if (params.get("targetId") != null) {
            w.eq(Review::getTargetId, Long.parseLong(params.get("targetId").toString()));
        }
        if (params.get("status") != null) {
            w.eq(Review::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        w.orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(new Page<>(page, size), w);
    }

    /**
     * 获取评论详情
     *
     * @param id 评论ID
     * @return 评论详情
     */
    public Review getReview(Long id) {
        return reviewMapper.selectById(id);
    }

    /**
     * 审核评论
     *
     * @param id 评论ID
     * @param status 审核状态: 1-通过 2-拒绝
     * @return 是否成功
     */
    public boolean auditReview(Long id, Integer status) {
        Review r = reviewMapper.selectById(id);
        if (r == null) return false;
        r.setStatus(status);
        return reviewMapper.updateById(r) > 0;
    }

    /**
     * 删除评论
     *
     * @param id 评论ID
     * @return 是否成功
     */
    public boolean deleteReview(Long id) {
        return reviewMapper.deleteById(id) > 0;
    }
}
