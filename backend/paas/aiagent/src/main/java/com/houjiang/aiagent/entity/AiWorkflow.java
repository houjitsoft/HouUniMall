package com.houjiang.aiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 中台 - 工作流（可视化编排）。
 *
 * <p>一个工作流 = 一张画布：{@code graph} 存节点与连线的 JSON（nodes / edges / viewport），
 * 后台画布保存什么，执行引擎就按什么跑，不把编排逻辑硬编码进 Java。
 *
 * <p>节点类型约定（见 {@code AiWorkflowService} 执行引擎）：
 * start 开始 / llm 模型调用 / condition 条件分支 / transform 变量加工 / end 结束。
 */
@Data
@TableName("hou_ai_workflow")
public class AiWorkflow {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工作流编码，智能体与前台按此引用 */
    private String code;

    private String name;

    /** 所属端：merchant 商家端 / frontend 前端 / admin 管理后台 / im IM 客服 */
    private String terminal;

    /** 场景分组，复用智能体 scene 口径 */
    private String scene;

    /** Element Plus 图标名 */
    private String icon;

    private String description;

    /** 1 启用 / 0 停用 */
    private Integer status;

    /** 画布中节点未单独指定模型时的默认模型编码 */
    private String modelCode;

    /** 画布 JSON：{nodes:[{id,type,name,x,y,config}],edges:[{id,source,sourceHandle,target}],viewport:{}} */
    private String graph;

    /** 保存次数，每次保存自增，便于回溯 */
    private Integer version;

    /** 1 内置，拒绝删除 */
    private Integer builtIn;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
