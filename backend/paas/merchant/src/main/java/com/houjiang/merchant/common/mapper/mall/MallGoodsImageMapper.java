package com.houjiang.merchant.common.mapper.mall;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.mall.MallGoodsImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品轮播图Mapper
 * @author Amos
 */
@Mapper
public interface MallGoodsImageMapper extends IBaseMapper<MallGoodsImage> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT image FROM hou_merchant_mall_goods_image WHERE delete_time = 0 AND goods_id = #{goodsId}")
    List<String> getImages(@Param("goodsId")Integer goodsId);

}
