package com.houjiang.mall.service.setting.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.mall.dto.PageParam;
import com.houjiang.mall.entity.setting.SettingDictData;
import com.houjiang.mall.entity.setting.SettingDictType;
import com.houjiang.mall.mapper.setting.SettingDictDataMapper;
import com.houjiang.mall.mapper.setting.SettingDictTypeMapper;
import com.houjiang.mall.service.setting.SettingDictDataService;
import com.houjiang.mall.validator.setting.DictDataCreateParam;
import com.houjiang.mall.validator.setting.DictDataUpdateParam;
import com.houjiang.mall.vo.setting.SettingDictDataVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 字典数据 Service 实现。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingDictDataServiceImpl implements SettingDictDataService {

    private static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final SettingDictDataMapper dictDataMapper;
    private final SettingDictTypeMapper dictTypeMapper;

    @Override
    public List<SettingDictDataVo> all(Map<String, String> params) {
        Integer typeId = parseTypeId(params);
        if (typeId == null) {
            throw new IllegalArgumentException("typeId 缺失");
        }

        // 验证字典类型存在
        SettingDictType dictType = dictTypeMapper.selectOne(new QueryWrapper<SettingDictType>()
                .eq("id", typeId)
                .isNull("delete_time")
                .last("limit 1"));
        if (dictType == null) {
            throw new IllegalArgumentException("该字典类型不存在");
        }

        QueryWrapper<SettingDictData> wrapper = new QueryWrapper<>();
        wrapper.select("id,type_id,type_value,name,value,remark,sort,status,create_time,update_time");
        wrapper.eq("type_id", typeId);
        wrapper.isNull("delete_time");
        wrapper.orderByAsc("sort").orderByDesc("id");

        applySearchFilters(wrapper, params);

        List<SettingDictData> records = dictDataMapper.selectList(wrapper);
        List<SettingDictDataVo> result = new LinkedList<>();
        for (SettingDictData record : records) {
            result.add(toVo(record));
        }
        return result;
    }

    @Override
    public IPage<SettingDictDataVo> list(PageParam pageParam, Map<String, String> params) {
        int pageNo = pageParam == null || pageParam.getPageNo() == null ? 1 : pageParam.getPageNo();
        int pageSize = pageParam == null || pageParam.getPageSize() == null ? 15 : pageParam.getPageSize();

        QueryWrapper<SettingDictData> wrapper = new QueryWrapper<>();
        wrapper.select("id,type_id,type_value,name,value,remark,sort,status,create_time,update_time");
        wrapper.isNull("delete_time");
        wrapper.orderByAsc("sort").orderByDesc("id");

        Integer typeId = parseTypeId(params);
        if (typeId != null) {
            SettingDictType dictType = dictTypeMapper.selectOne(new QueryWrapper<SettingDictType>()
                    .eq("id", typeId)
                    .isNull("delete_time")
                    .last("limit 1"));
            if (dictType == null) {
                throw new IllegalArgumentException("该字典类型不存在");
            }
            wrapper.eq("type_id", typeId);
        }

        applySearchFilters(wrapper, params);

        Page<SettingDictData> page = new Page<>(pageNo, pageSize);
        IPage<SettingDictData> iPage = dictDataMapper.selectPage(page, wrapper);

        IPage<SettingDictDataVo> voPage = new Page<>(iPage.getCurrent(), iPage.getSize(), iPage.getTotal());
        List<SettingDictDataVo> voList = new LinkedList<>();
        for (SettingDictData record : iPage.getRecords()) {
            voList.add(toVo(record));
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public SettingDictDataVo detail(Integer id) {
        SettingDictData record = dictDataMapper.selectOne(new QueryWrapper<SettingDictData>()
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));
        if (record == null) {
            throw new IllegalArgumentException("字典数据不存在");
        }
        return toVo(record);
    }

    @Override
    public void add(DictDataCreateParam param) {
        // 校验字典类型存在
        SettingDictType dictType = dictTypeMapper.selectOne(new QueryWrapper<SettingDictType>()
                .eq("id", param.getTypeId())
                .isNull("delete_time")
                .last("limit 1"));
        if (dictType == null) {
            throw new IllegalArgumentException("该字典类型不存在");
        }

        // 同 typeId 下 name 唯一
        SettingDictData nameExist = dictDataMapper.selectOne(new QueryWrapper<SettingDictData>()
                .select("id")
                .eq("type_id", param.getTypeId())
                .eq("name", param.getName())
                .isNull("delete_time")
                .last("limit 1"));
        if (nameExist != null) {
            throw new IllegalArgumentException("字典数据名称已存在");
        }

        long now = System.currentTimeMillis() / 1000L;
        SettingDictData model = new SettingDictData();
        model.setTypeId(param.getTypeId());
        model.setTypeValue(param.getTypeValue() != null ? param.getTypeValue() : dictType.getType());
        model.setName(param.getName());
        model.setValue(param.getValue());
        model.setRemark(param.getRemark());
        model.setSort(param.getSort() == null ? 0 : param.getSort());
        model.setStatus(param.getStatus());
        model.setCreateTime(now);
        model.setUpdateTime(now);
        dictDataMapper.insert(model);
    }

    @Override
    public void edit(DictDataUpdateParam param) {
        SettingDictData model = dictDataMapper.selectOne(new QueryWrapper<SettingDictData>()
                .eq("id", param.getId())
                .isNull("delete_time")
                .last("limit 1"));
        if (model == null) {
            throw new IllegalArgumentException("字典数据不存在");
        }

        SettingDictType dictType = dictTypeMapper.selectOne(new QueryWrapper<SettingDictType>()
                .eq("id", param.getTypeId())
                .isNull("delete_time")
                .last("limit 1"));
        if (dictType == null) {
            throw new IllegalArgumentException("该字典类型不存在");
        }

        // 同 typeId 下 name 唯一（排除自身）
        SettingDictData nameExist = dictDataMapper.selectOne(new QueryWrapper<SettingDictData>()
                .select("id")
                .ne("id", param.getId())
                .eq("type_id", param.getTypeId())
                .eq("name", param.getName())
                .isNull("delete_time")
                .last("limit 1"));
        if (nameExist != null) {
            throw new IllegalArgumentException("字典数据名称已存在");
        }

        model.setTypeId(param.getTypeId());
        model.setTypeValue(param.getTypeValue() != null ? param.getTypeValue() : dictType.getType());
        model.setName(param.getName());
        model.setValue(param.getValue());
        model.setRemark(param.getRemark());
        model.setSort(param.getSort() == null ? 0 : param.getSort());
        model.setStatus(param.getStatus());
        model.setUpdateTime(System.currentTimeMillis() / 1000L);
        dictDataMapper.updateById(model);
    }

    @Override
    public void del(Integer id) {
        SettingDictData model = dictDataMapper.selectOne(new QueryWrapper<SettingDictData>()
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));
        if (model == null) {
            throw new IllegalArgumentException("字典数据不存在");
        }
        model.setDeleteTime(System.currentTimeMillis() / 1000L);
        dictDataMapper.updateById(model);
    }

    /** 从参数中提取 typeId（同时兼容驼峰/下划线） */
    private Integer parseTypeId(Map<String, String> params) {
        if (params == null) {
            return null;
        }
        String value = params.get("typeId");
        if (value == null || value.isEmpty()) {
            value = params.get("type_id");
        }
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("typeId 必须为整数");
        }
    }

    /** 公共搜索条件：name/value 模糊，status 等值 */
    private void applySearchFilters(QueryWrapper<SettingDictData> wrapper, Map<String, String> params) {
        if (params == null) {
            return;
        }
        String name = params.get("name");
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        String value = params.get("value");
        if (value != null && !value.isEmpty()) {
            wrapper.like("value", value);
        }
        String status = params.get("status");
        if (status != null && !status.isEmpty()) {
            try {
                wrapper.eq("status", Integer.parseInt(status));
            } catch (NumberFormatException ignored) {
                // 忽略非法 status
            }
        }
    }

    /** Entity → Vo（自动把 bigint 秒级时间戳格式化为字符串） */
    private SettingDictDataVo toVo(SettingDictData entity) {
        SettingDictDataVo vo = new SettingDictDataVo();
        BeanUtils.copyProperties(entity, vo, "createTime", "updateTime");
        vo.setCreateTime(formatTimestamp(entity.getCreateTime()));
        vo.setUpdateTime(formatTimestamp(entity.getUpdateTime()));
        return vo;
    }

    private String formatTimestamp(Long seconds) {
        if (seconds == null || seconds <= 0) {
            return "";
        }
        return new SimpleDateFormat(TIME_PATTERN).format(new Date(seconds * 1000L));
    }
}
