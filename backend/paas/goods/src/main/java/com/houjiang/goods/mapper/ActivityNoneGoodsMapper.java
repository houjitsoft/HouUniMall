package com.houjiang.goods.mapper;

import com.houjiang.goods.dto.NoneGoodsUpdateDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName ActivityNoneGoods
 * @Description 活动专区商品
 * @Author Amos
 * @Date 2023/6/26 11:34
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ActivityNoneGoodsMapper {

    /**
     * 删除活动专区商品
     * @param param
     */
    void deleteNoneGoods(@Param("param") NoneGoodsUpdateDTO param);

}
