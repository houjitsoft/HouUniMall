package com.houjiang.userms.mapper;

import com.houjiang.userms.dto.BehaviorTrackDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户行为与留存分析 · 真实数据聚合
 *
 * <p>只读既有真实表 + 统一埋点事件表，跨域表沿用 {@code ${database.prefix.*}} 前缀写法。
 * 四个查询都把明细交回 Service 计算，SQL 只负责取数与统一字符集，口径集中在 Java 侧可复核。</p>
 *
 * @author Amos
 */
@Mapper
public interface BehaviorAnalyticsMapper {

    /**
     * 事件统一视图：新埋点表 + 成功登录日志 + 商品足迹 + 订单，按时间范围取明细。
     *
     * @param from 起始时间 yyyy-MM-dd HH:mm:ss（含）
     * @param to   结束时间 yyyy-MM-dd HH:mm:ss（不含）
     */
    List<Map<String, Object>> selectEvents(@Param("from") String from, @Param("to") String to);

    /** 时间范围内新增注册会员（漏斗与新增用户留存的 cohort） */
    List<Map<String, Object>> selectRegistrations(@Param("from") String from, @Param("to") String to);

    /** 时间范围内订单明细（含状态，用于有效成交 / 首购判定） */
    List<Map<String, Object>> selectOrders(@Param("from") String from, @Param("to") String to);

    /** 全量注册会员（漏斗基准，不受时间范围影响） */
    List<Map<String, Object>> selectAllMembers();

    /** 既有真实表的数据量，用于判定「已采集 / 未采集」 */
    List<Map<String, Object>> selectLegacyCounts();

    /** 埋点表按类型的数据量，用于判定「已采集 / 未采集」 */
    List<Map<String, Object>> selectEventTypeCounts();

    /** 写入一条埋点事件 */
    int insertEvent(BehaviorTrackDTO dto);
}
