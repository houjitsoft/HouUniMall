package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.yiyatong.CategoryDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.SupplierYiyatongCatEntity;

/**
 * @author Amos
 */
public interface SupplierYiyatongCatService extends IService<SupplierYiyatongCatEntity> {

    /**
     * 同步分类信息
     * @param categoryDTO
     */
    ResponseResult synchronousCategorys(CategoryDTO categoryDTO);

}
