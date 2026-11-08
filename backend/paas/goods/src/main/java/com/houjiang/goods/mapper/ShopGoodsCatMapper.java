package com.houjiang.goods.mapper;

import com.houjiang.commons.dto.goods.MerchantShopGoodsCatDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName ShopGoodsCatMapper
 * @Description 店铺商品分类关系表
 * @Author Amos
 * @Date 2023/6/27 9:42
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ShopGoodsCatMapper {

    /**
     * 添加店铺商品分类关系
     * @param records
     * @return
     */
    int insertSelectiveBatch(@Param("records") List<MerchantShopGoodsCatDTO> records);

    /**
     * 删除店铺商品分类关系
     * @param commonId
     * @return
     */
    int deleteByCommonId(Integer commonId);


    /**
     * 查询店铺商品分类
     * @param commonId
     * @return
     */
    List<MerchantShopGoodsCatDTO> queryByCommonId(Integer commonId);
}
