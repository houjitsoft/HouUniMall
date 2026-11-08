package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.dto.goods.GoodsAttrDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.goods.GoodsAttrVO;
import com.houjiang.commons.vo.goods.GoodsCategoryAttributesVO;
import com.houjiang.goods.entity.GoodsAttributeGroupEntity;
import com.houjiang.goods.entity.GoodsAttributeGroupRelationEntity;
import com.houjiang.goods.mapper.GoodsCategoryAttributesMapper;
import com.houjiang.goods.mapper.GoodsAttributeGroupMapper;
import com.houjiang.goods.mapper.GoodsAttributeGroupRelationMapper;
import com.houjiang.goods.service.GoodsAttributeGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 属性组ServiceImpl
 * @author Amos
 */
@Service
public class GoodsAttributeGroupServiceImpl extends ServiceImpl<GoodsAttributeGroupMapper, GoodsAttributeGroupEntity>
    implements GoodsAttributeGroupService {

    @Autowired
    private GoodsAttributeGroupRelationMapper relationMapper;
    
    @Autowired
    private GoodsCategoryAttributesMapper categoryAttributesMapper;

    @Override
    public ResponseResult queryList(Map<String, Object> param) {
        try {
            Integer page = param.get("page") != null ? Integer.parseInt(param.get("page").toString()) : 1;
            Integer limit = param.get("limit") != null ? Integer.parseInt(param.get("limit").toString()) : 10;
            
            Page<GoodsAttributeGroupEntity> pageObj = new Page<>(page, limit);
            LambdaQueryWrapper<GoodsAttributeGroupEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(GoodsAttributeGroupEntity::getCreateTime);
            
            IPage<GoodsAttributeGroupEntity> result = this.baseMapper.selectPage(pageObj, wrapper);
            
            // 查询每个属性组的关联属性
            for (GoodsAttributeGroupEntity entity : result.getRecords()) {
                List<GoodsAttributeGroupRelationEntity> relations = relationMapper.selectByAttributeGroupId(entity.getId());
                entity.setAttributes(relations);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("totalCount", result.getTotal());
            
            return ResponseResult.success(data);
        } catch (Exception e) {
            return ResponseResult.error("查询属性组列表失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult create(Map<String, Object> param) {
        try {
            GoodsAttributeGroupEntity entity = new GoodsAttributeGroupEntity();
            entity.setName((String) param.get("name"));
            entity.setRemark((String) param.get("remark"));
            
            this.save(entity);
            
            // 保存属性关联
            Object attributesObj = param.get("attributes");
            if (attributesObj != null && attributesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> attributesList = (List<Map<String, Object>>) attributesObj;
                
                if (!attributesList.isEmpty()) {
                    List<GoodsAttributeGroupRelationEntity> relations = new ArrayList<>();
                    for (int i = 0; i < attributesList.size(); i++) {
                        Map<String, Object> attrMap = attributesList.get(i);
                        GoodsAttributeGroupRelationEntity relation = new GoodsAttributeGroupRelationEntity();
                        relation.setAttributeGroupId(entity.getId());
                        
                        Object attrTypeIdObj = attrMap.get("attributeTypeId");
                        if (attrTypeIdObj != null) {
                            relation.setAttributeTypeId(Integer.parseInt(attrTypeIdObj.toString()));
                        }
                        
                        relation.setAttributeTypeName((String) attrMap.get("attributeTypeName"));
                        
                        Object isFilterObj = attrMap.get("isFilter");
                        relation.setIsFilter(isFilterObj != null ? Integer.parseInt(isFilterObj.toString()) : 0);
                        
                        relation.setSort(i + 1);
                        relations.add(relation);
                    }
                    relationMapper.insertBatch(relations);
                }
            }
            
            return ResponseResult.success(entity);
        } catch (Exception e) {
            return ResponseResult.error("创建属性组失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult update(Map<String, Object> param) {
        try {
            Object idObj = param.get("id");
            if (idObj == null) {
                return ResponseResult.error("属性组ID不能为空");
            }
            
            Integer id = Integer.parseInt(idObj.toString());
            GoodsAttributeGroupEntity entity = this.getById(id);
            if (entity == null) {
                return ResponseResult.error("属性组不存在");
            }
            
            entity.setName((String) param.get("name"));
            entity.setRemark((String) param.get("remark"));
            
            this.updateById(entity);
            
            // 删除旧的属性关联
            relationMapper.deleteByAttributeGroupId(id);
            
            // 保存新的属性关联
            Object attributesObj = param.get("attributes");
            if (attributesObj != null && attributesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> attributesList = (List<Map<String, Object>>) attributesObj;
                
                if (!attributesList.isEmpty()) {
                    List<GoodsAttributeGroupRelationEntity> relations = new ArrayList<>();
                    for (int i = 0; i < attributesList.size(); i++) {
                        Map<String, Object> attrMap = attributesList.get(i);
                        GoodsAttributeGroupRelationEntity relation = new GoodsAttributeGroupRelationEntity();
                        relation.setAttributeGroupId(id);
                        
                        Object attrTypeIdObj = attrMap.get("attributeTypeId");
                        if (attrTypeIdObj != null) {
                            relation.setAttributeTypeId(Integer.parseInt(attrTypeIdObj.toString()));
                        }
                        
                        relation.setAttributeTypeName((String) attrMap.get("attributeTypeName"));
                        
                        Object isFilterObj = attrMap.get("isFilter");
                        relation.setIsFilter(isFilterObj != null ? Integer.parseInt(isFilterObj.toString()) : 0);
                        
                        relation.setSort(i + 1);
                        relations.add(relation);
                    }
                    relationMapper.insertBatch(relations);
                }
            }
            
            return ResponseResult.success(entity);
        } catch (Exception e) {
            return ResponseResult.error("更新属性组失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult delete(Map<String, Object> param) {
        try {
            Object idObj = param.get("id");
            if (idObj == null) {
                return ResponseResult.error("属性组ID不能为空");
            }
            
            Integer id = Integer.parseInt(idObj.toString());
            
            // 删除属性关联
            relationMapper.deleteByAttributeGroupId(id);
            
            // 删除属性组
            this.removeById(id);
            
            return ResponseResult.success();
        } catch (Exception e) {
            return ResponseResult.error("删除属性组失败：" + e.getMessage());
        }
    }
    
    @Override
    public List<GoodsAttrVO> selectGoodsAttributeGroupList(GoodsAttrDTO dto) {
        // 获取属性组原始数据
        List<Map<String, Object>> rawData = categoryAttributesMapper.selectGoodsAttributeGroupList(dto);
        List<GoodsAttrVO> result = new ArrayList<>();
        
        // 转换为 GoodsAttrVO
        for (Map<String, Object> map : rawData) {
            GoodsAttrVO attrVO = new GoodsAttrVO();
            attrVO.setGroupName((String) map.get("attributeName"));
            attrVO.setDisplayOrder((Integer) map.get("displayOrder"));
            
            // 查询该属性组下的属性列表
            String attributeName = (String) map.get("attributeName");
            List<GoodsCategoryAttributesVO> attributeList = categoryAttributesMapper.selectGoodsAttributeList(attributeName);
            attrVO.setGoodsAttributeList(attributeList);
            
            result.add(attrVO);
        }
        
        return result;
    }
    
    @Override
    public List<Integer> selectCommonIdListByAttrs(List<Map<String, Object>> attrList) {
        return categoryAttributesMapper.selectCommonIdListByAttrs(attrList, attrList.size());
    }
}
