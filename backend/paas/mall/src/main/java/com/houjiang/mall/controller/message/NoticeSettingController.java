package com.houjiang.mall.controller.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.message.NoticeSetting;
import com.houjiang.mall.service.message.NoticeSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知设置控制器
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/message")
@RequiredArgsConstructor
public class NoticeSettingController {

    private final NoticeSettingService noticeSettingService;

    /**
     * 获取消息通知设置列表
     * POST /mall/message/list
     */
    @PostMapping("/list")
    public ResponseResult<List<NoticeSetting>> messageLists(@RequestBody Map<String, Object> params) {
        try {
            List<NoticeSetting> list = noticeSettingService.list(params);
            return ResponseResult.success(list);
        } catch (Exception e) {
            log.error("获取消息通知设置列表失败", e);
            return ResponseResult.error("获取失败");
        }
    }

    /**
     * 获取单个通知设置详情
     * POST /mall/message/detail
     */
    @PostMapping("/detail")
    public ResponseResult<NoticeSetting> messageDetail(@RequestBody Map<String, Object> params) {
        try {
            Long id = getLong(params, "id");
            if (id == null) {
                return ResponseResult.error("缺少 id 参数");
            }
            
            NoticeSetting setting = noticeSettingService.getDetail(id);
            if (setting == null) {
                return ResponseResult.error("通知设置不存在");
            }
            
            return ResponseResult.success(setting);
        } catch (Exception e) {
            log.error("获取通知设置详情失败", e);
            return ResponseResult.error("获取失败");
        }
    }

    /**
     * 保存消息通知设置
     * POST /mall/message/save
     */
    @PostMapping("/save")
    public ResponseResult<NoticeSetting> messageSave(@RequestBody Map<String, Object> params) {
        try {
            NoticeSetting setting = convertToNoticeSetting(params);
            
            // 必填参数验证
            if (setting.getShopId() == null || setting.getNotifyType() == null) {
                return ResponseResult.error("缺少必填参数：shop_id 或 notify_type");
            }
            
            boolean result = noticeSettingService.saveConfig(setting);
            if (result) {
                return ResponseResult.success(setting);
            } else {
                return ResponseResult.error("保存失败");
            }
        } catch (Exception e) {
            log.error("保存消息通知设置失败", e);
            return ResponseResult.error("保存失败");
        }
    }

    /**
     * 删除消息通知设置
     * POST /mall/message/delete
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> messageDelete(@RequestBody Map<String, Object> params) {
        try {
            Long id = getLong(params, "id");
            if (id == null) {
                return ResponseResult.error("缺少 id 参数");
            }
            
            boolean result = noticeSettingService.removeById(id);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("删除消息通知设置失败", e);
            return ResponseResult.error("删除失败");
        }
    }

    /**
     * 将 Map 转换为 NoticeSetting 对象
     */
    private NoticeSetting convertToNoticeSetting(Map<String, Object> params) {
        NoticeSetting setting = new NoticeSetting();
        
        Object idObj = params.get("id");
        if (idObj != null) {
            setting.setId(Long.parseLong(idObj.toString()));
        }
        
        Object shopIdObj = params.get("shop_id");
        if (shopIdObj != null) {
            setting.setShopId(Long.parseLong(shopIdObj.toString()));
        }
        
        setting.setNotifyType((String) params.get("notify_type"));
        
        Object smsEnabledObj = params.get("sms_enabled");
        if (smsEnabledObj != null) {
            setting.setSmsEnabled(Integer.parseInt(smsEnabledObj.toString()));
        }
        
        Object appPushEnabledObj = params.get("app_push_enabled");
        if (appPushEnabledObj != null) {
            setting.setAppPushEnabled(Integer.parseInt(appPushEnabledObj.toString()));
        }
        
        Object inSiteEnabledObj = params.get("in_site_enabled");
        if (inSiteEnabledObj != null) {
            setting.setInSiteEnabled(Integer.parseInt(inSiteEnabledObj.toString()));
        }
        
        return setting;
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
