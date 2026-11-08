package com.houjiang.merchant.common.mapper.ms;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.ms.Ms;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 秒杀活动Mapper
 * @author Amos
 */
@Mapper
public interface MsMapper extends IBaseMapper<Ms> {


    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT gci.category_id AS category_id FROM hou_merchant_ms ms INNER JOIN hou_merchant_mall_goods_category_index gci ON gci.goods_id = ms.goods_id WHERE ms.delete_time = 0 AND gci.delete_time = 0 AND ms.status = 1 AND ms.start_time <= #{curTime} AND ms.end_time >= #{curTime} AND ms.stock > 0 GROUP BY category_id")
    List<Integer> getIndexMsCategoryIds(@Param("curTime") Long curTime);

}
