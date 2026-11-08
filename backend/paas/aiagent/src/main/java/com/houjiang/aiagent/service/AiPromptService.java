package com.houjiang.aiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.aiagent.entity.AiPrompt;
import com.houjiang.aiagent.entity.AiPromptCategory;
import com.houjiang.aiagent.mapper.AiPromptCategoryMapper;
import com.houjiang.aiagent.mapper.AiPromptMapper;
import com.houjiang.aiagent.support.AiPageSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 提示词与分类管理。对应后台「提示词管理」。
 */
@Service
@RequiredArgsConstructor
public class AiPromptService {

    private final AiPromptMapper promptMapper;
    private final AiPromptCategoryMapper categoryMapper;

    public Map<String, Object> list(Map<String, Object> params) {
        LambdaQueryWrapper<AiPrompt> wrapper = new LambdaQueryWrapper<>();
        Long categoryId = AiPageSupport.toLong(params, "categoryId");
        if (categoryId == null) {
            categoryId = AiPageSupport.toLong(params, "category_id");
        }
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(AiPrompt::getCategoryId, categoryId);
        }
        String keyword = AiPageSupport.str(params, "keyword");
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AiPrompt::getName, keyword).or().like(AiPrompt::getDescription, keyword));
        }
        wrapper.orderByDesc(AiPrompt::getId);
        IPage<AiPrompt> page = promptMapper.selectPage(AiPageSupport.page(params), wrapper);
        Map<Long, String> categoryNames = categoryNames();
        List<Map<String, Object>> records = new ArrayList<>();
        for (AiPrompt prompt : page.getRecords()) {
            Map<String, Object> item = toView(prompt);
            item.put("category", categoryNames.getOrDefault(prompt.getCategoryId(), "-"));
            records.add(item);
        }
        return AiPageSupport.pageResult(page, records);
    }

    public Map<String, Object> detail(Long id) {
        AiPrompt prompt = promptMapper.selectById(id);
        return prompt == null ? new LinkedHashMap<>() : toView(prompt);
    }

    public Long create(Map<String, Object> data) {
        AiPrompt prompt = new AiPrompt();
        apply(prompt, data);
        prompt.setId(null);
        prompt.setUsageCount(0L);
        if (prompt.getStatus() == null) {
            prompt.setStatus(1);
        }
        promptMapper.insert(prompt);
        return prompt.getId();
    }

    public void update(Map<String, Object> data) {
        Long id = AiPageSupport.toLong(data, "id");
        if (id == null) {
            return;
        }
        AiPrompt prompt = promptMapper.selectById(id);
        if (prompt == null) {
            return;
        }
        apply(prompt, data);
        promptMapper.updateById(prompt);
    }

    public void delete(Long id) {
        if (id != null) {
            promptMapper.deleteById(id);
        }
    }

    public void incrUsage(Long id) {
        if (id == null) {
            return;
        }
        AiPrompt prompt = promptMapper.selectById(id);
        if (prompt != null) {
            prompt.setUsageCount(prompt.getUsageCount() == null ? 1 : prompt.getUsageCount() + 1);
            promptMapper.updateById(prompt);
        }
    }

    public List<Map<String, Object>> categories() {
        List<AiPromptCategory> list = categoryMapper.selectList(new LambdaQueryWrapper<AiPromptCategory>()
                .orderByDesc(AiPromptCategory::getSort).orderByAsc(AiPromptCategory::getId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiPromptCategory category : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", category.getId());
            item.put("name", category.getName());
            item.put("parentId", category.getParentId());
            item.put("sort", category.getSort());
            result.add(item);
        }
        return result;
    }

    public Long createCategory(Map<String, Object> data) {
        String name = AiPageSupport.str(data, "name");
        if (!StringUtils.hasText(name)) {
            return null;
        }
        AiPromptCategory category = new AiPromptCategory();
        category.setName(name);
        Long parentId = AiPageSupport.toLong(data, "parentId");
        category.setParentId(parentId == null ? 0L : parentId);
        Integer sort = AiPageSupport.toInt(data, "sort");
        category.setSort(sort == null ? 0 : sort);
        categoryMapper.insert(category);
        return category.getId();
    }

    public void updateCategory(Map<String, Object> data) {
        Long id = AiPageSupport.toLong(data, "id");
        if (id == null) {
            return;
        }
        AiPromptCategory category = categoryMapper.selectById(id);
        if (category == null) {
            return;
        }
        String name = AiPageSupport.str(data, "name");
        if (StringUtils.hasText(name)) {
            category.setName(name);
        }
        Integer sort = AiPageSupport.toInt(data, "sort");
        if (sort != null) {
            category.setSort(sort);
        }
        categoryMapper.updateById(category);
    }

    public void deleteCategory(Long id) {
        if (id != null) {
            categoryMapper.deleteById(id);
        }
    }

    private Map<Long, String> categoryNames() {
        Map<Long, String> names = new HashMap<>();
        for (AiPromptCategory category : categoryMapper.selectList(new LambdaQueryWrapper<>())) {
            names.put(category.getId(), category.getName());
        }
        return names;
    }

    private void apply(AiPrompt prompt, Map<String, Object> data) {
        String name = AiPageSupport.str(data, "name");
        if (StringUtils.hasText(name)) {
            prompt.setName(name);
        }
        String content = AiPageSupport.str(data, "content");
        if (content != null) {
            prompt.setContent(content);
        }
        String description = AiPageSupport.str(data, "description");
        if (description != null) {
            prompt.setDescription(description);
        }
        Object tags = data.get("tags");
        if (tags instanceof List<?> list) {
            prompt.setTags(String.join(",", list.stream().map(String::valueOf).toList()));
        } else if (tags != null) {
            prompt.setTags(String.valueOf(tags));
        }
        Long categoryId = AiPageSupport.toLong(data, "categoryId");
        if (categoryId == null) {
            categoryId = AiPageSupport.toLong(data, "category_id");
        }
        if (categoryId != null) {
            prompt.setCategoryId(categoryId);
        }
        Integer status = AiPageSupport.toInt(data, "status");
        if (status != null) {
            prompt.setStatus(status);
        }
    }

    private Map<String, Object> toView(AiPrompt prompt) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", prompt.getId());
        item.put("name", prompt.getName());
        item.put("categoryId", prompt.getCategoryId());
        item.put("content", prompt.getContent());
        item.put("description", prompt.getDescription());
        item.put("tags", prompt.getTags());
        item.put("usageCount", prompt.getUsageCount());
        item.put("status", prompt.getStatus());
        item.put("createTime", prompt.getCreateTime());
        return item;
    }
}
