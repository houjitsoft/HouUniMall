package com.houjiang.merchant.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class JobsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "岗位编号")
    private String code;

    @Schema(description = "岗位名称")
    private String name;

    @Schema(description = "岗位备注")
    private String remark;

    @Schema(description = "岗位排序")
    private Integer sort;

    @Schema(description = "是否停用: [0=否, 1=是]")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

}
