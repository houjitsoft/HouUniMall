package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsAttributeEntity;

import java.util.Map;

/**
 * @ClassName GoodsBrandCatService
 * @Description  商品品牌分类表
 * @Author Amos
 * @Date 2023-06-26 13:50:11
 * @Version 1.0
 * @author Amos
 */
public interface GoodsAttributeService extends IService<GoodsAttributeEntity> {

    /*
/**
     *
     * 查询分类关联下的属性品牌回显
     *
     */
    ResponseResult selectCatAttribute(Map<String, Object> map);


    /*
     * 通过catId商品属性表查找属性
     * */
    /**
     * @param map map
     * @return 处理结果
     */
    ResponseResult queryBrandCatAttributeList(Map<String,Object> map);
}
