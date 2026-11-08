package com.houjiang.aiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.aiagent.entity.AiModel;
import com.houjiang.aiagent.mapper.AiModelMapper;
import com.houjiang.aiagent.support.AiPageSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型池管理。对应后台「AI模型管理」「AI设置 - 模型配置」。
 */
@Service
@RequiredArgsConstructor
public class AiModelService {

    /** 与前端 AiModels.vue 的 typeOptions 一致 */
    private static final List<Map<String, String>> TYPES = List.of(
            type("chat", "对话模型"),
            type("text", "文本生成"),
            type("image", "图像生成"),
            type("code", "代码生成"));

    /**
     * GEO 迁入的「被分析平台」，不是可调用模型，默认不出现在中台模型管理列表里，
     * 需要时显式传 type=geo_target 查看。
     */
    private static final String TYPE_GEO_TARGET = "geo_target";

    private final AiModelMapper modelMapper;

    public Map<String, Object> list(Map<String, Object> params) {
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<>();
        String keyword = AiPageSupport.str(params, "keyword");
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AiModel::getName, keyword).or().like(AiModel::getDescription, keyword));
        }
        String type = AiPageSupport.str(params, "type");
        if (StringUtils.hasText(type)) {
            wrapper.eq(AiModel::getType, type);
        } else {
            wrapper.ne(AiModel::getType, TYPE_GEO_TARGET);
        }
        Integer status = AiPageSupport.toInt(params, "status");
        if (status != null) {
            wrapper.eq(AiModel::getStatus, status);
        }
        wrapper.orderByDesc(AiModel::getSort).orderByDesc(AiModel::getId);
        IPage<AiModel> page = modelMapper.selectPage(AiPageSupport.page(params), wrapper);
        return AiPageSupport.pageResult(page, page.getRecords());
    }

    public Map<String, Object> detail(Long id) {
        AiModel model = modelMapper.selectById(id);
        return model == null ? new LinkedHashMap<>() : toView(model);
    }

    public Long create(Map<String, Object> data) {
        AiModel model = new AiModel();
        apply(model, data);
        model.setId(null);
        model.setUsageCount(0L);
        if (model.getStatus() == null) {
            model.setStatus(1);
        }
        modelMapper.insert(model);
        return model.getId();
    }

    public void update(Map<String, Object> data) {
        Long id = AiPageSupport.toLong(data, "id");
        if (id == null) {
            return;
        }
        AiModel model = modelMapper.selectById(id);
        if (model == null) {
            return;
        }
        apply(model, data);
        modelMapper.updateById(model);
    }

    public void delete(Long id) {
        if (id != null) {
            modelMapper.deleteById(id);
        }
    }

    public void updateStatus(Long id, int status) {
        if (id == null) {
            return;
        }
        AiModel model = modelMapper.selectById(id);
        if (model == null) {
            return;
        }
        model.setStatus(status);
        modelMapper.updateById(model);
    }

    public Long copy(Long id) {
        if (id == null) {
            return null;
        }
        AiModel source = modelMapper.selectById(id);
        if (source == null) {
            return null;
        }
        AiModel copy = new AiModel();
        copy.setName(source.getName() + "-副本");
        // code 是唯一键，副本不继承，由运营手动填
        copy.setType(source.getType());
        copy.setVendor(source.getVendor());
        copy.setRegion(source.getRegion());
        copy.setApiUrl(source.getApiUrl());
        copy.setApiKey(source.getApiKey());
        copy.setTemperature(source.getTemperature());
        copy.setMaxTokens(source.getMaxTokens());
        copy.setStatus(0);
        copy.setDescription(source.getDescription());
        copy.setUsageCount(0L);
        copy.setSort(source.getSort());
        modelMapper.insert(copy);
        return copy.getId();
    }

    public List<Map<String, Object>> enabled() {
        List<AiModel> list = modelMapper.selectList(new LambdaQueryWrapper<AiModel>()
                .eq(AiModel::getStatus, 1)
                .ne(AiModel::getType, TYPE_GEO_TARGET)
                .orderByDesc(AiModel::getSort)
                .orderByAsc(AiModel::getId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiModel model : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", model.getId());
            item.put("code", model.getCode());
            item.put("name", model.getName());
            item.put("type", model.getType());
            item.put("vendor", model.getVendor());
            item.put("region", model.getRegion());
            result.add(item);
        }
        return result;
    }

    public List<Map<String, String>> types() {
        return TYPES;
    }

    public void incrUsage(String modelName) {
        if (!StringUtils.hasText(modelName)) {
            return;
        }
        AiModel model = modelMapper.selectOne(new LambdaQueryWrapper<AiModel>()
                .eq(AiModel::getName, modelName).last("limit 1"));
        if (model != null) {
            model.setUsageCount(model.getUsageCount() == null ? 1 : model.getUsageCount() + 1);
            modelMapper.updateById(model);
        }
    }

    private void apply(AiModel model, Map<String, Object> data) {
        String name = AiPageSupport.str(data, "name");
        if (StringUtils.hasText(name)) {
            model.setName(name);
        }
        String code = AiPageSupport.str(data, "code");
        if (code != null) {
            model.setCode(code);
        }
        String vendor = AiPageSupport.str(data, "vendor");
        if (vendor != null) {
            model.setVendor(vendor);
        }
        String region = AiPageSupport.str(data, "region");
        if (region != null) {
            model.setRegion(region);
        }
        String type = AiPageSupport.str(data, "type");
        if (StringUtils.hasText(type)) {
            model.setType(type);
        }
        String description = AiPageSupport.str(data, "description");
        if (description != null) {
            model.setDescription(description);
        }
        String apiUrl = AiPageSupport.str(data, "apiUrl");
        if (apiUrl == null) {
            apiUrl = AiPageSupport.str(data, "api_url");
        }
        if (apiUrl != null) {
            model.setApiUrl(apiUrl);
        }
        String apiKey = AiPageSupport.str(data, "apiKey");
        if (apiKey == null) {
            apiKey = AiPageSupport.str(data, "api_key");
        }
        if (apiKey != null) {
            model.setApiKey(apiKey);
        }
        Integer status = AiPageSupport.toInt(data, "status");
        if (status != null) {
            model.setStatus(status);
        }
        Object temperature = data.get("temperature");
        if (temperature == null) {
            temperature = data.get("temp");
        }
        if (temperature != null) {
            try {
                model.setTemperature(new java.math.BigDecimal(String.valueOf(temperature)));
            } catch (NumberFormatException ignored) {
                // 非法温度值保持原值
            }
        }
        Integer maxTokens = AiPageSupport.toInt(data, "maxTokens");
        if (maxTokens == null) {
            maxTokens = AiPageSupport.toInt(data, "max_tokens");
        }
        if (maxTokens != null) {
            model.setMaxTokens(maxTokens);
        }
        Integer sort = AiPageSupport.toInt(data, "sort");
        if (sort != null) {
            model.setSort(sort);
        }
    }

    private Map<String, Object> toView(AiModel model) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", model.getId());
        item.put("name", model.getName());
        item.put("code", model.getCode());
        item.put("vendor", model.getVendor());
        item.put("region", model.getRegion());
        item.put("type", model.getType());
        item.put("status", model.getStatus());
        item.put("description", model.getDescription());
        item.put("usageCount", model.getUsageCount());
        item.put("apiUrl", model.getApiUrl());
        item.put("apiKey", model.getApiKey());
        item.put("temperature", model.getTemperature());
        item.put("maxTokens", model.getMaxTokens());
        item.put("sort", model.getSort());
        item.put("createTime", model.getCreateTime());
        return item;
    }

    private static Map<String, String> type(String value, String label) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("value", value);
        item.put("label", label);
        return item;
    }
}
