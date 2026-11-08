package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.entity.mall.MallShopCategory;

/**
 *  商家店铺接口类
 * @author Amos
 */
public interface IMallShopCategoryService {

    /**
     * 根据店铺经营类目
     * @param id
     * @return
     */
    MallShopCategory getById(Integer id);
}
