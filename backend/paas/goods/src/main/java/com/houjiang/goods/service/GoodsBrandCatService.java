package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsBrandCatEntity;

import java.util.Map;

/**
 * @ClassName GoodsBrandCatService
 * @Description  商品品牌分类表
 * @Author Amos
 * @Date 2023-06-26 13:50:11
 * @Version 1.0
 * @author Amos
 */
public interface GoodsBrandCatService extends IService<GoodsBrandCatEntity> {

    /**
     * 查询品牌分类关联信息
     * @param objectMap
     * @return
     */
    ResponseResult selectByBrandCat(Map<String, Object> objectMap);
}
