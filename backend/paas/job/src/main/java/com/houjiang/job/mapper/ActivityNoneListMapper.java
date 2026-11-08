package com.houjiang.job.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName ActivityNoneListMapper
 * @Description
 * @Author Amos
 * @Date 2023/5/17 14:35
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ActivityNoneListMapper {

    /**
     * 查询国奇虚拟商品
     *
     * @return
     */
    List<String> queryGuoQiVirtualGoods();

    /**
     * 更新商品启动
     *
     * @param commonId
     */
    void updateGoodsStart(@Param("commonId") String commonId);

    /**
     * 根据commonId找到所有goodsId
     *
     * @param commonIdList
     * @return
     */
    List<Integer> queryAllGoodsIdByCommonId(List<String> commonIdList);

    /**
     * 更新商品状态
     *
     * @param goodsId
     */
    void updateGoodsState(@Param("goodsId") Integer goodsId);
}
