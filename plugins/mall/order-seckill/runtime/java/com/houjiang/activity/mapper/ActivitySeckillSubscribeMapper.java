package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.activity.entity.ActivitySeckillSubscribeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 限时秒杀开抢预约表 Mapper
 *
 * @author Amos
 */
@Mapper
public interface ActivitySeckillSubscribeMapper extends BaseMapper<ActivitySeckillSubscribeEntity> {

    /**
     * 按场次统计各秒杀商品的预约人数（后台展开行 / C 端「N 人已预约」用）。
     *
     * @param seckillId 场次编号
     * @return 每行含 goods_id、subscribe_count
     */
    @Select("SELECT goods_id, COUNT(*) AS subscribe_count "
            + "FROM hou_addon_activity_seckill_subscribe "
            + "WHERE seckill_id = #{seckillId} GROUP BY goods_id")
    List<Map<String, Object>> countBySeckillGroupGoods(@Param("seckillId") Integer seckillId);
}
