package com.houjiang.merchant.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class DevCrontab implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "类型 1-定时任务")
    private Integer type;

    @Schema(description = "是否系统任务 0-否 1-是")
    @TableField(value = "`system`")
    private Integer system;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "执行命令")
    private String command;

    @Schema(description = " 执行状态: 1=正在运行, 2=任务停止, 3=发生错误")
    private Integer status;

    @Schema(description = " 参数")
    private String params;

    @Schema(description = "执行规则")
    private String expression;

    @Schema(description = "错误信息")
    private String error;

    @Schema(description = "结束时间")
    private Long lastTime;

/**
 * @Schema(description = "执行策略: 1=立即执行, 2=执行一次, 3=放弃执行")
 * private Integer strategy;
 * 
 * @Schema(description = "并发执行: 0=否, 1=是")
 * private Integer concurrent;
 */

    @Schema(description = "最大执行时长")
    private Long time;

    @Schema(description = "最大执行时长")
    private Long maxTime;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

}
