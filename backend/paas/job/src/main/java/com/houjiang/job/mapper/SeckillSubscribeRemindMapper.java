package com.houjiang.job.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 秒杀开抢预约提醒 Mapper（job 服务读取 order-seckill 插件预约表）。
 *
 * <p>沿用 job 服务既有直连秒杀表的做法（参考 {@link SeckillGoodsMapper}），
 * 仅做「取待提醒预约」与「回写提醒状态」两件事。
 *
 * @author Amos
 */
@Mapper
public interface SeckillSubscribeRemindMapper {

    /**
     * 取开抢前 advanceMinutes 分钟内、尚未提醒（remind_status=0）的预约。
     * 命中 hou_addon_activity_seckill_subscribe 的 idx_start_remind 索引。
     */
    @Select("SELECT subscribe_id AS subscribeId, seckill_id AS seckillId, goods_id AS goodsId, "
            + "user_id AS userId, user_mobile AS userMobile, goods_name AS goodsName, "
            + "DATE_FORMAT(seckill_start_time, '%Y-%m-%d %H:%i') AS seckillStartTime "
            + "FROM hou_addon_activity_seckill_subscribe "
            + "WHERE remind_status = 0 "
            + "AND seckill_start_time > NOW() "
            + "AND seckill_start_time <= DATE_ADD(NOW(), INTERVAL #{advanceMinutes} MINUTE) "
            + "ORDER BY seckill_start_time ASC "
            + "LIMIT #{limit}")
    List<Map<String, Object>> selectDueSubscribes(@Param("advanceMinutes") int advanceMinutes,
                                                  @Param("limit") int limit);

    /**
     * 回写提醒状态：1-已提醒 2-提醒失败。
     */
    @Update("UPDATE hou_addon_activity_seckill_subscribe "
            + "SET remind_status = #{status}, remind_time = NOW() "
            + "WHERE subscribe_id = #{subscribeId}")
    int updateRemindStatus(@Param("subscribeId") int subscribeId, @Param("status") int status);
}
