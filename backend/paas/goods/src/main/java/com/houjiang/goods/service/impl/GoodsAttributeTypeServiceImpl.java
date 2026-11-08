package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsAttributeTypeEntity;
import com.houjiang.goods.entity.GoodsAttributeTypeValueEntity;
import com.houjiang.goods.mapper.GoodsAttributeTypeMapper;
import com.houjiang.goods.mapper.GoodsAttributeTypeValueMapper;
import com.houjiang.goods.service.GoodsAttributeTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 属性类型ServiceImpl
 * @author Amos
 */
@Service
public class GoodsAttributeTypeServiceImpl extends ServiceImpl<GoodsAttributeTypeMapper, GoodsAttributeTypeEntity>
    implements GoodsAttributeTypeService {

    @Autowired
    private GoodsAttributeTypeValueMapper valueMapper;

    @Override
    public ResponseResult queryPage(Map<String, Object> param) {
        try {
            Integer page = param.get("page") != null ? Integer.parseInt(param.get("page").toString()) : 1;
            Integer limit = param.get("limit") != null ? Integer.parseInt(param.get("limit").toString()) : 10;
            String valueName = (String) param.get("valueName");
            String typeName = (String) param.get("typeName");
            
            Page<GoodsAttributeTypeEntity> pageObj = new Page<>(page, limit);
            LambdaQueryWrapper<GoodsAttributeTypeEntity> wrapper = new LambdaQueryWrapper<>();
            
            if (StringUtils.hasText(typeName)) {
                wrapper.like(GoodsAttributeTypeEntity::getName, typeName);
            }
            
            wrapper.orderByDesc(GoodsAttributeTypeEntity::getCreateTime);
            
            IPage<GoodsAttributeTypeEntity> result = this.baseMapper.selectPage(pageObj, wrapper);
            
            // 查询每个类型的属性值
            for (GoodsAttributeTypeEntity entity : result.getRecords()) {
                List<GoodsAttributeTypeValueEntity> values = valueMapper.selectByAttributeTypeId(entity.getId());
                entity.setValues(values);
                entity.setValueCount(values.size());
                
                // 如果指定了valueName，需要过滤
                if (StringUtils.hasText(valueName)) {
                    boolean hasMatch = values.stream()
                        .anyMatch(v -> v.getValueName() != null && v.getValueName().contains(valueName));
                    if (!hasMatch) {
                        continue;
                    }
                }
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("totalCount", result.getTotal());
            
            return ResponseResult.success(data);
        } catch (Exception e) {
            return ResponseResult.error("查询属性类型列表失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseResult queryList() {
        try {
            List<GoodsAttributeTypeEntity> list = this.list();
            
            // 查询每个类型的属性值
            for (GoodsAttributeTypeEntity entity : list) {
                List<GoodsAttributeTypeValueEntity> values = valueMapper.selectByAttributeTypeId(entity.getId());
                entity.setValues(values);
                entity.setValueCount(values.size());
            }
            
            return ResponseResult.success(list);
        } catch (Exception e) {
            return ResponseResult.error("查询所有属性类型失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult create(Map<String, Object> param) {
        try {
            GoodsAttributeTypeEntity entity = new GoodsAttributeTypeEntity();
            entity.setName((String) param.get("name"));
            entity.setRemark((String) param.get("remark"));
            
            // 保存属性类型
            this.save(entity);
            
            // 保存属性值
            Object valuesObj = param.get("values");
            if (valuesObj != null && valuesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> valuesList = (List<Map<String, Object>>) valuesObj;
                
                if (!valuesList.isEmpty()) {
                    List<GoodsAttributeTypeValueEntity> values = new ArrayList<>();
                    for (int i = 0; i < valuesList.size(); i++) {
                        Map<String, Object> valueMap = valuesList.get(i);
                        GoodsAttributeTypeValueEntity value = new GoodsAttributeTypeValueEntity();
                        value.setAttributeTypeId(entity.getId());
                        value.setValueName((String) valueMap.get("valueName"));
                        value.setSort(i + 1);
                        values.add(value);
                    }
                    valueMapper.insertBatch(values);
                    entity.setValueCount(values.size());
                    this.updateById(entity);
                }
            }
            
            return ResponseResult.success(entity);
        } catch (Exception e) {
            return ResponseResult.error("创建属性类型失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult update(Map<String, Object> param) {
        try {
            Object idObj = param.get("id");
            if (idObj == null) {
                return ResponseResult.error("属性类型ID不能为空");
            }
            
            Integer id = Integer.parseInt(idObj.toString());
            GoodsAttributeTypeEntity entity = this.getById(id);
            if (entity == null) {
                return ResponseResult.error("属性类型不存在");
            }
            
            entity.setName((String) param.get("name"));
            entity.setRemark((String) param.get("remark"));
            
            // 删除旧的属性值
            valueMapper.deleteByAttributeTypeId(id);
            
            // 保存新的属性值
            Object valuesObj = param.get("values");
            if (valuesObj != null && valuesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> valuesList = (List<Map<String, Object>>) valuesObj;
                
                if (!valuesList.isEmpty()) {
                    List<GoodsAttributeTypeValueEntity> values = new ArrayList<>();
                    for (int i = 0; i < valuesList.size(); i++) {
                        Map<String, Object> valueMap = valuesList.get(i);
                        GoodsAttributeTypeValueEntity value = new GoodsAttributeTypeValueEntity();
                        value.setAttributeTypeId(id);
                        value.setValueName((String) valueMap.get("valueName"));
                        value.setSort(i + 1);
                        values.add(value);
                    }
                    valueMapper.insertBatch(values);
                    entity.setValueCount(values.size());
                } else {
                    entity.setValueCount(0);
                }
            } else {
                entity.setValueCount(0);
            }
            
            this.updateById(entity);
            
            return ResponseResult.success(entity);
        } catch (Exception e) {
            return ResponseResult.error("更新属性类型失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult delete(Map<String, Object> param) {
        try {
            Object idObj = param.get("id");
            if (idObj == null) {
                return ResponseResult.error("属性类型ID不能为空");
            }
            
            Integer id = Integer.parseInt(idObj.toString());
            
            // 删除属性值
            valueMapper.deleteByAttributeTypeId(id);
            
            // 删除属性类型
            this.removeById(id);
            
            return ResponseResult.success();
        } catch (Exception e) {
            return ResponseResult.error("删除属性类型失败：" + e.getMessage());
        }
    }
}
