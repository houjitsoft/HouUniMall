package com.houjiang.mall.service.setting.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.mall.dto.PageParam;
import com.houjiang.mall.entity.setting.SettingDictType;
import com.houjiang.mall.mapper.setting.SettingDictTypeMapper;
import com.houjiang.mall.service.setting.SettingDictTypeService;
import com.houjiang.mall.validator.setting.DictTypeCreateParam;
import com.houjiang.mall.validator.setting.DictTypeUpdateParam;
import com.houjiang.mall.vo.setting.SettingDictTypeVo;
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
 * 字典类型 Service 实现
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingDictTypeServiceImpl implements SettingDictTypeService {

    private static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final SettingDictTypeMapper dictTypeMapper;

    @Override
    public List<SettingDictTypeVo> all() {
        QueryWrapper<SettingDictType> wrapper = new QueryWrapper<>();
        wrapper.select("id,name,type,remark,status,create_time,update_time");
        wrapper.isNull("delete_time");
        wrapper.orderByDesc("id");

        List<SettingDictType> records = dictTypeMapper.selectList(wrapper);
        List<SettingDictTypeVo> result = new LinkedList<>();
        for (SettingDictType record : records) {
            result.add(toVo(record));
        }
        return result;
    }

    @Override
    public IPage<SettingDictTypeVo> list(PageParam pageParam, Map<String, String> params) {
        int pageNo = pageParam == null || pageParam.getPageNo() == null ? 1 : pageParam.getPageNo();
        int pageSize = pageParam == null || pageParam.getPageSize() == null ? 15 : pageParam.getPageSize();

        QueryWrapper<SettingDictType> wrapper = new QueryWrapper<>();
        wrapper.select("id,name,type,remark,status,create_time,update_time");
        wrapper.isNull("delete_time");
        wrapper.orderByDesc("id");

        if (params != null) {
            String name = params.get("name");
            if (name != null && !name.isEmpty()) {
                wrapper.like("name", name);
            }
            String type = params.get("type");
            if (type != null && !type.isEmpty()) {
                wrapper.like("type", type);
            }
            String status = params.get("status");
            if (status != null && !status.isEmpty()) {
                wrapper.eq("status", Integer.parseInt(status));
            }
        }

        Page<SettingDictType> page = new Page<>(pageNo, pageSize);
        IPage<SettingDictType> iPage = dictTypeMapper.selectPage(page, wrapper);

        IPage<SettingDictTypeVo> voPage = new Page<>(iPage.getCurrent(), iPage.getSize(), iPage.getTotal());
        List<SettingDictTypeVo> voList = new LinkedList<>();
        for (SettingDictType record : iPage.getRecords()) {
            voList.add(toVo(record));
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public SettingDictTypeVo detail(Integer id) {
        SettingDictType record = dictTypeMapper.selectOne(new QueryWrapper<SettingDictType>()
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));
        if (record == null) {
            throw new IllegalArgumentException("字典类型不存在");
        }
        return toVo(record);
    }

    @Override
    public void add(DictTypeCreateParam param) {
        SettingDictType nameExist = dictTypeMapper.selectOne(new QueryWrapper<SettingDictType>()
                .select("id")
                .eq("name", param.getName())
                .isNull("delete_time")
                .last("limit 1"));
        if (nameExist != null) {
            throw new IllegalArgumentException("字典名称已存在");
        }

        SettingDictType typeExist = dictTypeMapper.selectOne(new QueryWrapper<SettingDictType>()
                .select("id")
                .eq("type", param.getType())
                .isNull("delete_time")
                .last("limit 1"));
        if (typeExist != null) {
            throw new IllegalArgumentException("字典类型已存在");
        }

        long now = System.currentTimeMillis() / 1000L;
        SettingDictType model = new SettingDictType();
        model.setName(param.getName());
        model.setType(param.getType());
        model.setRemark(param.getRemark());
        model.setStatus(param.getStatus());
        model.setCreateTime(now);
        model.setUpdateTime(now);
        dictTypeMapper.insert(model);
    }

    @Override
    public void edit(DictTypeUpdateParam param) {
        SettingDictType model = dictTypeMapper.selectOne(new QueryWrapper<SettingDictType>()
                .eq("id", param.getId())
                .isNull("delete_time")
                .last("limit 1"));
        if (model == null) {
            throw new IllegalArgumentException("字典类型不存在");
        }

        SettingDictType nameExist = dictTypeMapper.selectOne(new QueryWrapper<SettingDictType>()
                .select("id")
                .ne("id", param.getId())
                .eq("name", param.getName())
                .isNull("delete_time")
                .last("limit 1"));
        if (nameExist != null) {
            throw new IllegalArgumentException("字典名称已存在");
        }

        SettingDictType typeExist = dictTypeMapper.selectOne(new QueryWrapper<SettingDictType>()
                .select("id")
                .ne("id", param.getId())
                .eq("type", param.getType())
                .isNull("delete_time")
                .last("limit 1"));
        if (typeExist != null) {
            throw new IllegalArgumentException("字典类型已存在");
        }

        model.setName(param.getName());
        model.setType(param.getType());
        model.setRemark(param.getRemark());
        model.setStatus(param.getStatus());
        model.setUpdateTime(System.currentTimeMillis() / 1000L);
        dictTypeMapper.updateById(model);
    }

    @Override
    public void del(Integer id) {
        SettingDictType model = dictTypeMapper.selectOne(new QueryWrapper<SettingDictType>()
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));
        if (model == null) {
            throw new IllegalArgumentException("字典类型不存在");
        }
        model.setDeleteTime(System.currentTimeMillis() / 1000L);
        dictTypeMapper.updateById(model);
    }

    /**
     * Entity → Vo（自动把 bigint 秒级时间戳格式化为字符串）。
     */
    private SettingDictTypeVo toVo(SettingDictType entity) {
        SettingDictTypeVo vo = new SettingDictTypeVo();
        BeanUtils.copyProperties(entity, vo, "createTime", "updateTime");
        vo.setCreateTime(formatTimestamp(entity.getCreateTime()));
        vo.setUpdateTime(formatTimestamp(entity.getUpdateTime()));
        return vo;
    }

    /**
     * 秒级时间戳格式化为 yyyy-MM-dd HH:mm:ss 字符串。
     */
    private String formatTimestamp(Long seconds) {
        if (seconds == null || seconds <= 0) {
            return "";
        }
        return new SimpleDateFormat(TIME_PATTERN).format(new Date(seconds * 1000L));
    }
}
