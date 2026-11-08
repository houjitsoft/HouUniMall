package com.houjiang.mall.service.shop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.shop.ShopCategory;
import com.houjiang.mall.mapper.shop.ShopCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 商家经营类目服务层
 *
 * @author Amos
 */
@Service
public class ShopCategoryService extends ServiceImpl<ShopCategoryMapper, ShopCategory> {

    /**
     * 分页查询经营类目列表
     *
     * @param params 查询参数：page, size, name, status
     * @return 分页结果
     */
    public IPage<ShopCategory> pageList(Map<String, Object> params) {
        Integer page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        Integer size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        String name = (String) params.get("name");
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;

        LambdaQueryWrapper<ShopCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), ShopCategory::getName, name);
        wrapper.eq(status != null, ShopCategory::getStatus, status);
        wrapper.orderByDesc(ShopCategory::getSort);

        return this.page(new Page<>(page, size), wrapper);
    }

    /**
     * 获取所有启用的经营类目（用于下拉选择）
     *
     * @return 启用的类目列表
     */
    public List<ShopCategory> listEnabled() {
        return this.list(new LambdaQueryWrapper<ShopCategory>()
                .eq(ShopCategory::getStatus, 1)
                .orderByDesc(ShopCategory::getSort));
    }

    /**
     * 更新类目状态
     *
     * @param id     类目ID
     * @param status 新状态: 0-禁用 1-启用
     * @return 是否成功
     */
    public boolean updateStatus(Long id, Integer status) {
        ShopCategory category = this.getById(id);
        if (category == null) return false;
        category.setStatus(status);
        return this.updateById(category);
    }
}
