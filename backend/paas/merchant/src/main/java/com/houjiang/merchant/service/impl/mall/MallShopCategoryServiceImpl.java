package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallShopCategory;
import com.houjiang.merchant.common.mapper.mall.MallShopCategoryMapper;
import com.houjiang.merchant.service.mall.IMallShopCategoryService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 商家账号实现类
 * @author Amos
 */
@Service
public class MallShopCategoryServiceImpl implements IMallShopCategoryService {

    @Resource
    MallShopCategoryMapper mallShopCategoryMapper;


    @Override
    public MallShopCategory getById(Integer id) {
        return mallShopCategoryMapper.selectOne(new QueryWrapper<MallShopCategory>().eq("id", id).eq("delete_time", 0));
    }
}
