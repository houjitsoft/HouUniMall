package com.houjiang.merchant.common.entity.server;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class Jvm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "当前JVM占用的内存总数(M)")
    private double total;

    @Schema(description = "JVM最大可用内存总数(M)")
    private double max;

    @Schema(description = "JVM空闲内存(M)")
    private double free;

    @Schema(description = "JVM内存使用率")
    private double usage;

    @Schema(description = "JDK版本")
    private String version;

    @Schema(description = "JDK路径")
    private String home;

    @Schema(description = "JDK名称")
    private String name;

    @Schema(description = "运行参数")
    private String inputArgs;

    @Schema(description = "JDK运行时间")
    private String runTime;

    @Schema(description = "JDK启动时间")
    private String startTime;
}
