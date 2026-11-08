package com.houjiang.job.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MerchantGoodsCommonJobMapper
 * @Description
 * @Author Amos
 * @Date 2023/5/17 11:37
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface MerchantGoodsCommonJobMapper {

    /**
     * 查询商品时间
     *
     * @return
     */
    List<Map<String, Object>> queryGoodsTimeShelf();

    /**
     * 更新商品
     *
     * @param commonId
     */
    void updateGoodsPutaway(@Param("commonId") Integer commonId);

    /**
     * 更新商品基础
     * @param commonId
     */
    void updateGoodsBase(@Param("commonId") Integer commonId);

}
