package com.houjiang.mall.mapper.update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.update.PluginUpdateTask;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 安装/升级/回滚任务 Mapper（{@code hou_plugin_update_task}）。
 *
 * <p>放在 {@code com.houjiang.mall.mapper.update} 包下的原因同实体：
 * MyBatis-Plus 未配 {@code @MapperScan}，只扫 mall 启动类所在包。
 *
 * @author Amos
 * @since M3
 */
@Mapper
public interface PluginUpdateTaskMapper extends BaseMapper<PluginUpdateTask> {

    /**
     * 查同一组合（实例 + 插件 + 操作 + 目标版本）仍在进行中的任务，用于并发互斥（3016）。
     *
     * @param instanceId 站点实例
     * @param pluginCode 插件编码
     * @param opType     操作类型入库值
     * @param toVersion  目标版本
     * @return 活动任务，没有则返回 null
     */
    @Select("SELECT * FROM hou_plugin_update_task "
            + "WHERE instance_id = #{instanceId} AND plugin_code = #{pluginCode} "
            + "AND op_type = #{opType} AND to_version = #{toVersion} "
            + "AND state IN ('PENDING', 'RUNNING') ORDER BY id DESC LIMIT 1")
    PluginUpdateTask selectActive(@Param("instanceId") String instanceId,
                                  @Param("pluginCode") String pluginCode,
                                  @Param("opType") String opType,
                                  @Param("toVersion") String toVersion);

    /**
     * 按任务号查询（{@code /task/status} 用）。
     *
     * @param taskId 任务号
     * @return 任务，不存在返回 null
     */
    @Select("SELECT * FROM hou_plugin_update_task WHERE task_id = #{taskId} LIMIT 1")
    PluginUpdateTask selectByTaskId(@Param("taskId") String taskId);

    /**
     * 查孤儿任务候选：仍是非终态、且开始时间早于阈值的任务（M4 启动扫描用）。
     *
     * <p>{@code start_time IS NULL} 也算超时——建单时一定会写开始时间，
     * 为空只可能是写入被中断的残行，同样需要收敛。
     *
     * @param threshold 开始时间阈值，早于该时间的非终态任务视为孤儿
     * @return 任务列表，按 ID 升序（先处理更早的）
     */
    @Select("SELECT * FROM hou_plugin_update_task "
            + "WHERE state IN ('PENDING', 'RUNNING') "
            + "AND (start_time IS NULL OR start_time < #{threshold}) "
            + "ORDER BY id ASC")
    List<PluginUpdateTask> selectOrphans(@Param("threshold") LocalDateTime threshold);
}
