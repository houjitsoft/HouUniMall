package com.houjiang.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.activity.entity.ActivityPointsMallToolEntity;
import com.houjiang.activity.mapper.ActivityPointsMallToolMapper;
import com.houjiang.activity.service.ActivityPointsMallToolService;
import com.houjiang.commons.util.ResponseResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 积分商城工具配置 Service 实现
 * @author Amos
 */
@Service
public class ActivityPointsMallToolServiceImpl extends ServiceImpl<ActivityPointsMallToolMapper, ActivityPointsMallToolEntity>
        implements ActivityPointsMallToolService {

    /**
     * 允许的展示位置编码集合
     */
    private static final Set<String> VALID_POSITION_CODES = new HashSet<>(Arrays.asList(
            "HOME_BANNER",
            "HOME_FLOOR",
            "HOME_RECOMMEND",
            "CATEGORY_ENTRY",
            "DETAIL_RECOMMEND"
    ));

    @Override
    public ResponseResult saveWithValidation(ActivityPointsMallToolEntity entity) {
        if (entity == null) {
            return ResponseResult.error("配置参数不能为空");
        }

        // 工具名称校验
        if (!StringUtils.hasText(entity.getToolName())) {
            return ResponseResult.error("工具名称不能为空");
        }
        if (entity.getToolName().length() > 100) {
            return ResponseResult.error("工具名称长度不能超过100字符");
        }

        // 工具编码校验：非空 + 格式
        if (!StringUtils.hasText(entity.getToolCode())) {
            return ResponseResult.error("工具编码不能为空");
        }
        String toolCode = entity.getToolCode().trim();
        if (!toolCode.startsWith("POINTS_")) {
            return ResponseResult.error("工具编码格式不正确，必须以\"POINTS_\"开头，只能包含字母、数字和下划线");
        }
        for (int i = 0; i < toolCode.length(); i++) {
            char c = toolCode.charAt(i);
            if (!(c >= 'A' && c <= 'Z') && !(c >= '0' && c <= '9') && c != '_' ) {
                return ResponseResult.error("工具编码格式不正确，必须以\"POINTS_\"开头，只能包含大写字母、数字和下划线");
            }
        }
        entity.setToolCode(toolCode);

        // 展示位置编码校验
        if (!StringUtils.hasText(entity.getPositionCode())) {
            return ResponseResult.error("展示位置编码不能为空");
        }
        String positionCode = entity.getPositionCode().trim();
        if (!VALID_POSITION_CODES.contains(positionCode)) {
            return ResponseResult.error("展示位置编码不合法，请选择系统预定义的页面位置编码");
        }
        entity.setPositionCode(positionCode);

        // 配置 JSON 格式校验
        if (StringUtils.hasText(entity.getConfigJson())) {
            try {
                JSON.parse(entity.getConfigJson());
            } catch (JSONException e) {
                return ResponseResult.error("配置内容必须是合法的JSON格式");
            }
        }

        // 时间逻辑校验
        Date startTime = entity.getStartTime();
        Date endTime = entity.getEndTime();
        if (startTime != null && endTime != null && startTime.after(endTime)) {
            return ResponseResult.error("生效开始时间不能晚于结束时间");
        }

        // 工具编码唯一性校验：新增时直接查，更新时排除自身ID
        LambdaQueryWrapper<ActivityPointsMallToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityPointsMallToolEntity::getToolCode, toolCode);
        if (entity.getToolId() != null) {
            wrapper.ne(ActivityPointsMallToolEntity::getToolId, entity.getToolId());
        }
        long count = this.count(wrapper);
        if (count > 0) {
            return ResponseResult.error("工具编码已存在，请更换其他编码");
        }

        this.saveOrUpdate(entity);
        return ResponseResult.success(entity.getToolId());
    }
}
