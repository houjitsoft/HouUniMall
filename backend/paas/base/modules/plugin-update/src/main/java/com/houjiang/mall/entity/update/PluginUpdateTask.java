package com.houjiang.mall.entity.update;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 插件安装 / 升级 / 回滚任务（{@code hou_plugin_update_task}，方案 §3.7）。
 *
 * <p>本实体必须放在 {@code com.houjiang.mall.**} 包下：项目未配置 {@code @MapperScan}，
 * MyBatis-Plus 只扫描 mall 启动类所在包，放到 {@code com.houjiang.plugin.update.**}
 * 会导致 Mapper 注册不进容器，启动即空指针（与 M2 的 {@code PluginDownloadToken} 同一约束）。
 *
 * <p><b>幂等键的真实语义（M3 交付后被修正过，以本节为准）</b>：
 * 早期唯一键是 {@code uk_idem(instance_id, plugin_code, op_type, to_version, state)}——
 * 把 {@code state} 放进唯一键，真实约束就变成「同一组合的<b>同一状态</b>只能有一条」，
 * 非终态互斥成立，但终态历史只剩一条（第二次成功安装置 SUCCESS 会撞上一次的 SUCCESS 行）。
 * 已在 {@code plugin_artifact_tables.sql 第六段}改为<b>函数式唯一键</b>：
 * 生成列 {@code active_key} 在非终态（PENDING / RUNNING）时为
 * {@code instance_id|plugin_code|op_type|to_version}、终态为 NULL，
 * 配 {@code UNIQUE KEY uk_active(active_key)}——唯一索引中 NULL 互不冲突，于是
 * 「非终态严格互斥（3016）+ 终态可无限共存（完整历史）」同时成立。
 *
 * <p><b>本实体不映射 active_key</b>：它由数据库按 state 自动算出，Java 侧写入即报错，
 * MyBatis-Plus 的 SELECT 也只取本类声明的列，天然不会碰到它。
 *
 * @author Amos
 * @since M3
 */
@Data
@TableName("hou_plugin_update_task")
public class PluginUpdateTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务号（服务端生成，对外暴露的唯一标识）。
     */
    private String taskId;

    /**
     * 站点实例（执行主体，来自登录态的 orgId）。
     */
    private String instanceId;

    private String pluginCode;

    /**
     * 操作类型：INSTALL / UPGRADE / ROLLBACK（入库值见 {@code UpdateOpType}）。
     */
    private String opType;

    /**
     * 来源版本（INSTALL 时为 null）。
     */
    private String fromVersion;

    /**
     * 目标版本。
     */
    private String toVersion;

    /**
     * 任务状态：PENDING / RUNNING / SUCCESS / ROLLED_BACK / FAILED。
     */
    private String state;

    /**
     * 当前阶段：DOWNLOAD / VERIFY / COMPAT / BACKUP / STOP / SCHEMA / START / HEALTH。
     */
    private String step;

    /**
     * 备份目录（M4 升级/回滚使用；M3 安装恒为 null）。
     */
    private String backupPath;

    /**
     * 错误码：插件分发体系的数字码（3001-3018）或符号名（如 {@code SCHEMA_FAILED}）。
     *
     * <p>列类型是 VARCHAR(32)，因此允许写符号名——引擎/文件系统这类没有分发错误码的失败
     * 用它记录，避免把无关故障硬塞进 3001-3018 造成语义污染。
     */
    private String errorCode;

    private String errorMsg;

    private Long operatorId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
