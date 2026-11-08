package com.houjiang.activity.controller;

import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivityPointsMallToolService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.activity.entity.ActivityPointsMallToolEntity;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 积分商城工具配置控制器
 * @author Amos
 */
@RestController
@RequestMapping("/pointsMallTool")
public class ActivityPointsMallToolController {


    @Autowired
    private ActivityPointsMallToolService activityPointsMallToolService;

    /**
     * 分页查询积分商城工具配置列表
     */
    @PostMapping("/page")
    public ResponseResult queryPointsMallToolPage(@RequestBody Map<String, Object> param) {
        int page = param.get("page") == null ? 1 : Integer.parseInt(String.valueOf(param.get("page")));
        int size = param.get("size") == null ? 10 : Integer.parseInt(String.valueOf(param.get("size")));

        LambdaQueryWrapper<ActivityPointsMallToolEntity> wrapper = new LambdaQueryWrapper<>();

        if (param.get("toolName") != null && StringUtils.hasText(String.valueOf(param.get("toolName")))) {
            wrapper.like(ActivityPointsMallToolEntity::getToolName, String.valueOf(param.get("toolName")));
        }
        if (param.get("toolCode") != null && StringUtils.hasText(String.valueOf(param.get("toolCode")))) {
            wrapper.eq(ActivityPointsMallToolEntity::getToolCode, String.valueOf(param.get("toolCode")));
        }
        if (param.get("status") != null) {
            wrapper.eq(ActivityPointsMallToolEntity::getStatus, Integer.parseInt(String.valueOf(param.get("status"))));
        }

        wrapper.orderByDesc(ActivityPointsMallToolEntity::getUpdateTime);

        Page<ActivityPointsMallToolEntity> pageParam = new Page<>(page, size);
        IPage<ActivityPointsMallToolEntity> pageResult = activityPointsMallToolService.page(pageParam, wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());

        return ResponseResult.success(result);
    }

    /**
     * 查询配置详情
     */
    @PostMapping("/detail")
    public ResponseResult getPointsMallToolDetail(@RequestBody Map<String, Object> param) {
        if (param.get("toolId") == null) {
            return ResponseResult.error("toolId 不能为空");
        }
        Long toolId = Long.parseLong(String.valueOf(param.get("toolId")));
        ActivityPointsMallToolEntity entity = activityPointsMallToolService.getById(toolId);
        if (entity == null) {
            return ResponseResult.error("配置不存在");
        }
        return ResponseResult.success(entity);
    }

    /**
     * 新增或更新积分商城工具配置
     */
    @PostMapping("/save")
    public ResponseResult savePointsMallTool(@RequestBody Map<String, Object> param) {
        UserInfoVO userInfo = ActivitySupport.validateTokenAndGetUser();
        Date now = new Date();

        // 基础必填校验，避免空值进入后续逻辑
        Object toolNameObj = param.get("toolName");
        if (toolNameObj == null || !StringUtils.hasText(String.valueOf(toolNameObj))) {
            return ResponseResult.error("工具名称不能为空");
        }
        Object toolCodeObj = param.get("toolCode");
        if (toolCodeObj == null || !StringUtils.hasText(String.valueOf(toolCodeObj))) {
            return ResponseResult.error("工具编码不能为空");
        }
        Object positionCodeObj = param.get("positionCode");
        if (positionCodeObj == null || !StringUtils.hasText(String.valueOf(positionCodeObj))) {
            return ResponseResult.error("展示位置编码不能为空");
        }

        ActivityPointsMallToolEntity entity;
        if (param.get("toolId") != null) {
            Long toolId = Long.parseLong(String.valueOf(param.get("toolId")));
            entity = activityPointsMallToolService.getById(toolId);
            if (entity == null) {
                return ResponseResult.error("配置不存在");
            }
            entity.setUpdateBy(userInfo.getUserName());
            entity.setUpdateTime(now);
        } else {
            entity = new ActivityPointsMallToolEntity();
            entity.setCreateBy(userInfo.getUserName());
            entity.setCreateTime(now);
            entity.setUpdateBy(userInfo.getUserName());
            entity.setUpdateTime(now);
        }

        if (param.get("toolName") != null) {
            entity.setToolName(String.valueOf(param.get("toolName")));
        }
        if (param.get("toolCode") != null) {
            entity.setToolCode(String.valueOf(param.get("toolCode")));
        }
        if (param.get("positionCode") != null) {
            entity.setPositionCode(String.valueOf(param.get("positionCode")));
        }
        if (param.get("configJson") != null) {
            entity.setConfigJson(String.valueOf(param.get("configJson")));
        }
        if (param.get("status") != null) {
            entity.setStatus(Integer.parseInt(String.valueOf(param.get("status"))));
        }
        if (param.get("startTime") != null) {
            entity.setStartTime(new Date(Long.parseLong(String.valueOf(param.get("startTime")))));
        }
        if (param.get("endTime") != null) {
            entity.setEndTime(new Date(Long.parseLong(String.valueOf(param.get("endTime")))));
        }
        if (param.get("remark") != null) {
            entity.setRemark(String.valueOf(param.get("remark")));
        }

        return activityPointsMallToolService.saveWithValidation(entity);
    }

    /**
     * 删除配置
     */
    @PostMapping("/delete")
    public ResponseResult deletePointsMallTool(@RequestBody Map<String, Object> param) {
        if (param.get("toolId") == null) {
            return ResponseResult.error("toolId 不能为空");
        }
        Long toolId = Long.parseLong(String.valueOf(param.get("toolId")));
        activityPointsMallToolService.removeById(toolId);
        return ResponseResult.success(null);
    }

    /**
     * 更新启用状态
     */
    @PostMapping("/status/update")
    public ResponseResult updatePointsMallToolStatus(@RequestBody Map<String, Object> param) {
        if (param.get("toolId") == null || param.get("status") == null) {
            return ResponseResult.error("toolId 和 status 不能为空");
        }
        Long toolId = Long.parseLong(String.valueOf(param.get("toolId")));
        Integer status = Integer.parseInt(String.valueOf(param.get("status")));

        ActivityPointsMallToolEntity entity = activityPointsMallToolService.getById(toolId);
        if (entity == null) {
            return ResponseResult.error("配置不存在");
        }

        entity.setStatus(status);
        entity.setUpdateTime(new Date());
        activityPointsMallToolService.updateById(entity);
        return ResponseResult.success(null);
    }
}
