package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.entity.BaseProtocolEntity;
import com.houjiang.base.mapper.BaseProtocolMapper;
import com.houjiang.base.service.BaseProtocolService;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 协议管理ServiceImpl
 * @author Amos
 */
@Service
public class BaseProtocolServiceImpl extends ServiceImpl<BaseProtocolMapper, BaseProtocolEntity> 
        implements BaseProtocolService {
    
    @Override
    public ResponseResult queryList(Map<String, Object> param) {
        try {
            LambdaQueryWrapper<BaseProtocolEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByAsc(BaseProtocolEntity::getSort);
            queryWrapper.orderByDesc(BaseProtocolEntity::getUpdateTime);
            
            List<BaseProtocolEntity> list = this.list(queryWrapper);
            return ResponseResult.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("查询失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult saveOrUpdate(Map<String, Object> param) {
        try {
            BaseProtocolEntity entity = new BaseProtocolEntity();
            
            if (param.get("id") != null) {
                entity.setId(Integer.valueOf(param.get("id").toString()));
            }
            if (param.get("name") != null) {
                entity.setName(param.get("name").toString());
            }
            if (param.get("type") != null) {
                entity.setType(param.get("type").toString());
            }
            if (param.get("fileUrl") != null) {
                entity.setFileUrl(param.get("fileUrl").toString());
            }
            if (param.get("fileName") != null) {
                entity.setFileName(param.get("fileName").toString());
            }
            if (param.get("sort") != null) {
                entity.setSort(Integer.valueOf(param.get("sort").toString()));
            }
            if (param.get("status") != null) {
                entity.setStatus(Integer.valueOf(param.get("status").toString()));
            }
            
            if (entity.getId() == null) {
                // 新增
                entity.setCreateTime(new Date());
                entity.setUpdateTime(new Date());
                this.save(entity);
            } else {
                // 更新
                entity.setUpdateTime(new Date());
                this.updateById(entity);
            }
            
            return ResponseResult.success("操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("操作失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult deleteById(Map<String, Object> param) {
        try {
            if (param.get("id") == null) {
                return ResponseResult.error("ID不能为空");
            }
            
            Integer id = Integer.valueOf(param.get("id").toString());
            this.removeById(id);
            
            return ResponseResult.success("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("删除失败");
        }
    }
}
