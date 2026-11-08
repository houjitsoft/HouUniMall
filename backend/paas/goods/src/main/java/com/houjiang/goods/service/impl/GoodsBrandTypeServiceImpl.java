package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsBrandTypeEntity;
import com.houjiang.goods.mapper.GoodsBrandTypeMapper;
import com.houjiang.goods.mapper.GoodsBrandTypeRelationMapper;
import com.houjiang.goods.service.GoodsBrandTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 品牌类型ServiceImpl
 * @author Amos
 */
@Service
public class GoodsBrandTypeServiceImpl extends ServiceImpl<GoodsBrandTypeMapper, GoodsBrandTypeEntity>
    implements GoodsBrandTypeService {

    @Autowired
    private GoodsBrandTypeRelationMapper relationMapper;

    @Override
    public ResponseResult queryList(Map<String, Object> param) {
        try {
            Integer page = param.get("page") != null ? Integer.parseInt(param.get("page").toString()) : 1;
            Integer limit = param.get("limit") != null ? Integer.parseInt(param.get("limit").toString()) : 10;
            
            Page<GoodsBrandTypeEntity> pageObj = new Page<>(page, limit);
            LambdaQueryWrapper<GoodsBrandTypeEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(GoodsBrandTypeEntity::getSort);
            
            IPage<GoodsBrandTypeEntity> result = this.baseMapper.selectPage(pageObj, wrapper);
            
            // 查询每个类型关联的品牌ID列表
            for (GoodsBrandTypeEntity entity : result.getRecords()) {
                List<Integer> brandIds = relationMapper.selectBrandIdsByTypeId(entity.getId());
                entity.setBrandIds(brandIds);
                entity.setBrandCount(brandIds.size());
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("totalCount", result.getTotal());
            
            return ResponseResult.success(data);
        } catch (Exception e) {
            return ResponseResult.error("查询品牌类型列表失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult create(Map<String, Object> param) {
        try {
            GoodsBrandTypeEntity entity = new GoodsBrandTypeEntity();
            entity.setName((String) param.get("name"));
            
            Object sortObj = param.get("sort");
            if (sortObj != null) {
                entity.setSort(Integer.parseInt(sortObj.toString()));
            } else {
                entity.setSort(0);
            }
            
            this.save(entity);
            
            // 保存品牌关联
            Object brandIdsObj = param.get("brandIds");
            if (brandIdsObj != null && brandIdsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Integer> brandIds = (List<Integer>) brandIdsObj;
                if (!brandIds.isEmpty()) {
                    relationMapper.insertRelations(entity.getId(), brandIds);
                }
            }
            
            return ResponseResult.success(entity);
        } catch (Exception e) {
            return ResponseResult.error("创建品牌类型失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult update(Map<String, Object> param) {
        try {
            Object idObj = param.get("id");
            if (idObj == null) {
                return ResponseResult.error("品牌类型ID不能为空");
            }
            
            Integer id = Integer.parseInt(idObj.toString());
            GoodsBrandTypeEntity entity = this.getById(id);
            if (entity == null) {
                return ResponseResult.error("品牌类型不存在");
            }
            
            entity.setName((String) param.get("name"));
            
            Object sortObj = param.get("sort");
            if (sortObj != null) {
                entity.setSort(Integer.parseInt(sortObj.toString()));
            }
            
            this.updateById(entity);
            
            // 更新品牌关联
            // 1. 删除旧关联
            relationMapper.deleteByBrandTypeId(id);
            
            // 2. 插入新关联
            Object brandIdsObj = param.get("brandIds");
            if (brandIdsObj != null && brandIdsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Integer> brandIds = (List<Integer>) brandIdsObj;
                if (!brandIds.isEmpty()) {
                    relationMapper.insertRelations(id, brandIds);
                }
            }
            
            return ResponseResult.success(entity);
        } catch (Exception e) {
            return ResponseResult.error("更新品牌类型失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult delete(Map<String, Object> param) {
        try {
            Object idObj = param.get("id");
            if (idObj == null) {
                return ResponseResult.error("品牌类型ID不能为空");
            }
            
            Integer id = Integer.parseInt(idObj.toString());
            
            // 删除关联关系
            relationMapper.deleteByBrandTypeId(id);
            
            // 删除品牌类型
            this.removeById(id);
            
            return ResponseResult.success();
        } catch (Exception e) {
            return ResponseResult.error("删除品牌类型失败：" + e.getMessage());
        }
    }
}
