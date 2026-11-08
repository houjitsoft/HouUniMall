package com.houjiang.mall.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.shop.Goods;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品管理 Mapper 接口
 * 
 * @author Amos
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}
