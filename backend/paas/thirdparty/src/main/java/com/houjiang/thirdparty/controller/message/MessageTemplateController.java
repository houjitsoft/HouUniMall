package com.houjiang.thirdparty.controller.message;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.entity.message.MessageTemplate;
import com.houjiang.thirdparty.service.message.MessageTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

/**
 * 消息模板控制器
 * 
 * @author Amos
 * @since 2025-12-26
 */
@RestController
@RequestMapping("/message/template")
public class MessageTemplateController {

    @Autowired
    private MessageTemplateService templateService;

    /**
     * 获取模板列表
     * 
     * @param params 查询参数，包含 page, size, bizType, keyword, channel, status
     * @return 模板列表
     */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody(required = false) Map<String, Object> params) {
        try {
            // 处理分页参数，支持字符串和整数类型
            Integer page = 1;
            Integer size = 20;
            if (params != null) {
                if (params.containsKey("page")) {
                    Object pageObj = params.get("page");
                    if (pageObj instanceof Integer) {
                        page = (Integer) pageObj;
                    } else if (pageObj instanceof String) {
                        page = Integer.parseInt((String) pageObj);
                    } else if (pageObj instanceof Number) {
                        page = ((Number) pageObj).intValue();
                    }
                }
                if (params.containsKey("size")) {
                    Object sizeObj = params.get("size");
                    if (sizeObj instanceof Integer) {
                        size = (Integer) sizeObj;
                    } else if (sizeObj instanceof String) {
                        size = Integer.parseInt((String) sizeObj);
                    } else if (sizeObj instanceof Number) {
                        size = ((Number) sizeObj).intValue();
                    }
                }
            }
            
            // 处理其他参数
            String bizType = params != null && params.containsKey("bizType") ? String.valueOf(params.get("bizType")) : null;
            String keyword = params != null && params.containsKey("keyword") ? String.valueOf(params.get("keyword")) : null;
            String channel = params != null && params.containsKey("channel") ? String.valueOf(params.get("channel")) : null;
            
            // 处理 status 参数，支持字符串和整数类型
            Integer status = null;
            if (params != null && params.containsKey("status")) {
                Object statusObj = params.get("status");
                if (statusObj instanceof Integer) {
                    status = (Integer) statusObj;
                } else if (statusObj instanceof String) {
                    status = Integer.parseInt((String) statusObj);
                } else if (statusObj instanceof Number) {
                    status = ((Number) statusObj).intValue();
                }
            }
            
            // 空字符串转换为 null
            if (bizType != null && bizType.trim().isEmpty()) {
                bizType = null;
            }
            if (keyword != null && keyword.trim().isEmpty()) {
                keyword = null;
            }
            if (channel != null && channel.trim().isEmpty()) {
                channel = null;
            }
            
            Map<String, Object> data = templateService.getTemplateList(page, size, bizType, keyword, channel, status);
            return ResponseResult.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail(500, "获取模板列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的模板
     * 
     * @return 模板列表
     */
    @GetMapping("/enabled")
    public ResponseResult enabled() {
        return ResponseResult.success(templateService.getEnabledTemplates());
    }

    /**
     * 获取模板详情
     * 
     * @param id 模板ID
     * @return 模板信息
     */
    @GetMapping("/{id}")
    public ResponseResult detail(@PathVariable Integer id) {
        MessageTemplate template = templateService.getTemplateById(id);
        if (template == null) {
            return ResponseResult.fail(404, "模板不存在");
        }
        return ResponseResult.success(template);
    }

    /**
     * 根据编码获取模板
     * 
     * @param code 模板编码
     * @return 模板信息
     */
    @GetMapping("/code/{code}")
    public ResponseResult getByCode(@PathVariable String code) {
        MessageTemplate template = templateService.getTemplateByCode(code);
        if (template == null) {
            return ResponseResult.fail(404, "模板不存在");
        }
        return ResponseResult.success(template);
    }

    /**
     * 创建模板
     * 
     * @param template 模板信息
     * @return 操作结果
     */
    @PostMapping("/create")
    public ResponseResult create(@RequestBody MessageTemplate template) {
        // 检查模板编码是否已存在
        MessageTemplate existing = templateService.getTemplateByCode(template.getTemplateCode());
        if (existing != null) {
            return ResponseResult.fail(400, "模板编码已存在");
        }
        
        boolean result = templateService.createTemplate(template);
        if (result) {
            return ResponseResult.success("创建成功");
        }
        return ResponseResult.fail(500, "创建失败");
    }

    /**
     * 更新模板
     * 
     * @param template 模板信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody MessageTemplate template) {
        if (template.getId() == null) {
            return ResponseResult.fail(400, "模板ID不能为空");
        }
        boolean result = templateService.updateTemplate(template);
        if (result) {
            return ResponseResult.success("更新成功");
        }
        return ResponseResult.fail(500, "更新失败");
    }

    /**
     * 更新模板状态
     * 
     * @param id 模板ID
     * @param status 状态
     * @return 操作结果
     */
    @PostMapping("/{id}/status")
    public ResponseResult updateStatus(@PathVariable Integer id, @RequestParam Integer status) {
        boolean result = templateService.updateStatus(id, status);
        if (result) {
            return ResponseResult.success("状态更新成功");
        }
        return ResponseResult.fail(500, "状态更新失败");
    }

    /**
     * 删除模板
     * 
     * @param id 模板ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseResult delete(@PathVariable Integer id) {
        boolean result = templateService.deleteTemplate(id);
        if (result) {
            return ResponseResult.success("删除成功");
        }
        return ResponseResult.fail(500, "删除失败");
    }

    /**
     * 批量更新模板状态
     * 
     * @param params 包含ids和status的Map
     * @return 操作结果
     */
    @PostMapping("/batch/status")
    public ResponseResult batchUpdateStatus(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) params.get("ids");
        Integer status = (Integer) params.get("status");
        
        if (ids == null || ids.isEmpty()) {
            return ResponseResult.fail(400, "模板ID列表不能为空");
        }
        if (status == null) {
            return ResponseResult.fail(400, "状态不能为空");
        }
        
        boolean result = templateService.batchUpdateStatus(ids, status);
        if (result) {
            return ResponseResult.success("批量更新成功");
        }
        return ResponseResult.fail(500, "批量更新失败");
    }
}
