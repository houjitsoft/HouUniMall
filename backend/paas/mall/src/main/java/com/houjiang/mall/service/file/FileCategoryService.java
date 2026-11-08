package com.houjiang.mall.service.file;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.file.FileCategory;
import com.houjiang.mall.mapper.file.FileCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 文件分类 Service
 * 
 * @author Amos
 */
@Service
public class FileCategoryService extends ServiceImpl<FileCategoryMapper, FileCategory> {
    
    /**
     * 查询分类列表
     * @param params 查询参数
     * @return 分类列表
     */
    public List<FileCategory> list(Map<String, Object> params) {
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        
        LambdaQueryWrapper<FileCategory> wrapper = new LambdaQueryWrapper<>();
        
        // 按店铺 ID 筛选
        wrapper.eq(shopId != null, FileCategory::getShopId, shopId);
        
        // 按名称模糊搜索
        if (StringUtils.hasText((String) params.get("cateName"))) {
            wrapper.like(FileCategory::getCateName, params.get("cateName").toString());
        }
        
        // 按排序升序
        wrapper.orderByAsc(FileCategory::getSort)
               .orderByAsc(FileCategory::getCateId);
        
        return this.list(wrapper);
    }
    
    /**
     * 添加分类
     * @param fileCategory 分类对象
     * @return 是否成功
     */
    public boolean addCategory(FileCategory fileCategory) {
        return this.save(fileCategory);
    }
    
    /**
     * 编辑分类
     * @param fileCategory 分类对象
     * @return 是否成功
     */
    public boolean editCategory(FileCategory fileCategory) {
        return this.updateById(fileCategory);
    }
    
    /**
     * 删除分类
     * @param cateId 分类 ID
     * @return 是否成功
     */
    public boolean deleteCategory(Long cateId) {
        // TODO: 检查分类下是否有文件，如果有则需要先删除或移动文件
        
        return this.removeById(cateId);
    }
}
