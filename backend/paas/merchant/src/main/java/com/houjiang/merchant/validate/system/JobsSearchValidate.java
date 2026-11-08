package com.houjiang.merchant.validate.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class JobsSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "岗位名称")
    private String name;

    @Schema(description = "是否停用")
    private Integer status;

    @Schema(description = "导出信息")
    private Integer export;
    @Schema(description = "file_name")
    private String file_name;

    @Schema(description = "page_start")
    private Integer page_start;

    @Schema(description = "page_end")
    private Integer page_end;

    @Schema(description = "page_size")
    private Integer page_size;

    @Schema(description = "page_type")
    private Integer page_type;

}
