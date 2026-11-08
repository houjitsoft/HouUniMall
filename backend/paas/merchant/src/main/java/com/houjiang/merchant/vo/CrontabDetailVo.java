package com.houjiang.merchant.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class CrontabDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    private Integer id;

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "类型 1-定时任务")
    private Integer type;

    @Schema(description = "是否系统任务 0-否 1-是")
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

}
