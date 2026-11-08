package com.houjiang.thirdparty.service.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.thirdparty.entity.message.MessageTemplate;
import com.houjiang.thirdparty.mapper.message.MessageTemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息模板服务类
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Service
public class MessageTemplateService {

    @Autowired
    private MessageTemplateMapper templateMapper;

    /**
     * 获取模板列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param bizType 业务类型
     * @param keyword 关键词
     * @param channel 消息类型（渠道）
     * @param status 状态（可选）
     * @return 分页结果
     */
    public Map<String, Object> getTemplateList(Integer page, Integer size, String bizType, String keyword, String channel, Integer status) {
        Page<MessageTemplate> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<MessageTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(MessageTemplate::getDeleteTime);
        
        if (StringUtils.hasText(bizType)) {
            wrapper.eq(MessageTemplate::getBizType, bizType);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(MessageTemplate::getTemplateName, keyword)
                    .or().like(MessageTemplate::getTemplateCode, keyword));
        }
        // 消息类型筛选：根据channels字段筛选，支持包含指定渠道的模板
        if (StringUtils.hasText(channel)) {
            wrapper.like(MessageTemplate::getChannels, channel);
        }
        // 状态筛选
        if (status != null) {
            wrapper.eq(MessageTemplate::getStatus, status);
        }
        wrapper.orderByDesc(MessageTemplate::getCreateTime);
        
        Page<MessageTemplate> result = templateMapper.selectPage(pageInfo, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("size", size);
        return data;
    }

    /**
     * 获取所有启用的模板
     * 
     * @return 模板列表
     */
    public List<MessageTemplate> getEnabledTemplates() {
        LambdaQueryWrapper<MessageTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageTemplate::getStatus, 1)
               .isNull(MessageTemplate::getDeleteTime);
        return templateMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取模板
     * 
     * @param id 模板ID
     * @return 模板信息
     */
    public MessageTemplate getTemplateById(Integer id) {
        return templateMapper.selectById(id);
    }

    /**
     * 根据编码获取模板
     * 
     * @param templateCode 模板编码
     * @return 模板信息
     */
    public MessageTemplate getTemplateByCode(String templateCode) {
        LambdaQueryWrapper<MessageTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageTemplate::getTemplateCode, templateCode)
               .isNull(MessageTemplate::getDeleteTime);
        return templateMapper.selectOne(wrapper);
    }

    /**
     * 创建模板
     * 
     * @param template 模板信息
     * @return 是否成功
     */
    public boolean createTemplate(MessageTemplate template) {
        template.setCreateTime((int)(System.currentTimeMillis() / 1000));
        template.setStatus(1);
        return templateMapper.insert(template) > 0;
    }

    /**
     * 更新模板
     * 
     * @param template 模板信息
     * @return 是否成功
     */
    public boolean updateTemplate(MessageTemplate template) {
        template.setUpdateTime((int)(System.currentTimeMillis() / 1000));
        return templateMapper.updateById(template) > 0;
    }

    /**
     * 更新模板状态
     * 
     * @param id 模板ID
     * @param status 状态
     * @return 是否成功
     */
    public boolean updateStatus(Integer id, Integer status) {
        MessageTemplate template = new MessageTemplate();
        template.setId(id);
        template.setStatus(status);
        template.setUpdateTime((int)(System.currentTimeMillis() / 1000));
        return templateMapper.updateById(template) > 0;
    }

    /**
     * 批量更新模板状态
     * 
     * @param ids 模板ID列表
     * @param status 状态
     * @return 是否成功
     */
    public boolean batchUpdateStatus(List<Integer> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        int updateTime = (int)(System.currentTimeMillis() / 1000);
        for (Integer id : ids) {
            MessageTemplate template = new MessageTemplate();
            template.setId(id);
            template.setStatus(status);
            template.setUpdateTime(updateTime);
            templateMapper.updateById(template);
        }
        return true;
    }

    /**
     * 删除模板（软删除）
     * 
     * @param id 模板ID
     * @return 是否成功
     */
    public boolean deleteTemplate(Integer id) {
        MessageTemplate template = new MessageTemplate();
        template.setId(id);
        template.setDeleteTime((int)(System.currentTimeMillis() / 1000));
        return templateMapper.updateById(template) > 0;
    }
}
