package com.houjiang.mall.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.user.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车Mapper接口
 * 
 * @author Amos
 */
@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
