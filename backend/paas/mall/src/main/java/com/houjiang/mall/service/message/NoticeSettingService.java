package com.houjiang.mall.service.message;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.message.NoticeSetting;
import com.houjiang.mall.mapper.message.NoticeSettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知设置服务层
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeSettingService extends ServiceImpl<NoticeSettingMapper, NoticeSetting> {

    /**
     * 获取消息通知设置列表（支持按类型筛选）
     * 
     * @param params 参数（notify_type - 通知类型，page，size）
     * @return 分页数据
     */
    public List<NoticeSetting> list(Map<String, Object> params) {
        String notifyType = getString(params, "notify_type");
        
        if (notifyType != null && !notifyType.isEmpty()) {
            var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NoticeSetting>();
            wrapper.eq(NoticeSetting::getNotifyType, notifyType);
            return this.list(wrapper);
        }
        
        return this.list();
    }

    /**
     * 获取单个通知设置详情
     * 
     * @param id 主键 ID
     * @return 通知设置对象
     */
    public NoticeSetting getDetail(Long id) {
        return this.getById(id);
    }

    /**
     * 保存消息通知设置
     * 
     * @param noticeSetting 通知设置对象
     * @return 是否成功
     */
    public boolean saveConfig(NoticeSetting noticeSetting) {
        try {
            // 先查询该店铺该类型是否已有配置
            var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NoticeSetting>();
            wrapper.eq(NoticeSetting::getShopId, noticeSetting.getShopId())
                   .eq(NoticeSetting::getNotifyType, noticeSetting.getNotifyType());
            
            NoticeSetting existConfig = this.getOne(wrapper);
            
            if (existConfig != null) {
                // 更新现有配置
                noticeSetting.setId(existConfig.getId());
                return this.updateById(noticeSetting);
            } else {
                // 新增配置
                return this.save(noticeSetting);
            }
        } catch (Exception e) {
            log.error("保存消息通知设置失败", e);
            return false;
        }
    }

    /**
     * 批量保存通知设置
     * 
     * @param settings 通知设置列表
     * @return 是否成功
     */
    public boolean batchSave(List<NoticeSetting> settings) {
        try {
            for (NoticeSetting setting : settings) {
                saveConfig(setting);
            }
            return true;
        } catch (Exception e) {
            log.error("批量保存通知设置失败", e);
            return false;
        }
    }

    /**
     * 从 Map 中提取字符串值
     */
    private String getString(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 从 Map 中提取 Long 值
     */
    private Long getLong(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }
}
