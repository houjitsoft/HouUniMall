package com.houjiang.merchant.common.entity.server;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class Disk implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "盘符路径")
    private String dirName;

    @Schema(description = "盘符类型")
    private String sysTypeName;

    @Schema(description = "文件类型")
    private String typeName;

    @Schema(description = "总大小")
    private String total;

    @Schema(description = "剩余大小")
    private String free;

    @Schema(description = "已经使用量")
    private String used;

    @Schema(description = "资源的使用率")
    private double usage;

}
