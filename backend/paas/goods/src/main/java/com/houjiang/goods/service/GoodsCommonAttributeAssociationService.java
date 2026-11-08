package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsCommonAttributeAssociationEntity;

import java.util.Map;

/**
 * @ClassName GoodsBrandCatService
 * @Description  商品品牌分类表
 * @Author Amos
 * @Date 2023-06-26 13:50:11
 * @Version 1.0
 * @author Amos
 */
public interface GoodsCommonAttributeAssociationService extends IService<GoodsCommonAttributeAssociationEntity> {

    /*
     *
     * 删除时判断商品属性是否已发布
     * */
     /**
      * @param map map
      * @return 处理结果
      */
     ResponseResult selectAttribute(Map<String, Object> map);
}
