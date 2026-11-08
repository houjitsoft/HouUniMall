package com.houjiang.job.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/5/17 14:54
 */
@Mapper
public interface UpdateCommonStatuJobMapper {

    /**
     * 查询common表库存为0 状态不为下架的商品id
     * @return
     */
    Integer queryCommonIdBystock(String commomId);


    /**
     * 查询goods表 goodsId
     * @return
     */
    List<Map<String,String>> queryGoodsIdByBase();

    /**
     *修改common表状态
/**
     * @param commomId
     * @return
     */
    Integer updateCommonStatu(String commomId);

    /**
     * 修改goods表状态
     * @param commomId
     * @return
     */
    Integer updateGoodsStatu(String commomId);

    /**
     * 修改goods表状态
     * @param goodsId
     * @return
     */
    Integer updateGoodsStatuGoodsId(String goodsId);
}
