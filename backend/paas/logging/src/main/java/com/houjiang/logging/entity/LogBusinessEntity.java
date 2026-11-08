package com.houjiang.logging.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务日志实体类
 *
 * <p>映射 hou_log_business 表，记录核心业务数据的变更轨迹，
 * 保存变更前后的数据快照，支持审计溯源。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Data
@TableName("hou_log_business")
@Schema(description = "业务日志实体")
public class LogBusinessEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "操作用户ID")
    private String userId;

    @Schema(description = "操作用户名")
    private String username;

    @Schema(description = "所属模块（如pm、oa、crm）")
    private String module;

    @Schema(description = "操作动作（create:新增 / update:修改 / delete:删除 / approve:审批）")
    private String action;

    @Schema(description = "目标对象类型（如project、task、contract）")
    private String targetType;

    @Schema(description = "目标对象ID")
    private String targetId;

    @Schema(description = "操作详情描述")
    private String detail;

    @Schema(description = "变更前的数据快照（JSON格式）")
    private String beforeData;

    @Schema(description = "变更后的数据快照（JSON格式）")
    private String afterData;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
