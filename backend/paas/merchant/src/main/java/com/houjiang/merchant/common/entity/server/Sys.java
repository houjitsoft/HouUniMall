package com.houjiang.merchant.common.entity.server;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class Sys implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "服务器名称")
    private String computerName;

    @Schema(description = "服务器Ip")
    private String computerIp;

    @Schema(description = "项目路径")
    private String userDir;

    @Schema(description = "操作系统")
    private String osName;

    @Schema(description = "系统架构")
    private String osArch;

}
