package com.houjiang.goods.mapper;

import com.houjiang.commons.dto.goods.MerchantShopGoodsCatDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName ShopSelfCatMapper
 * @Description 店铺商品分类表
 * @Author Amos
 * @Date 2023/6/27 9:49
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ShopSelfCatMapper {

    /**
     * 查询分类
     * @param catIdList
     * @return
     */
    List<MerchantShopGoodsCatDTO> queryShopCatIds(@Param("catIdList") List<Integer> catIdList);
}
