package com.houjiang.merchant.vo.system;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class JobsExportVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @ExcelProperty("ID")
    @ColumnWidth(50)
    private Integer id;

    @Schema(description = "岗位编号")
    @ExcelProperty("岗位编号")
    @ColumnWidth(50)
    private String code;

    @Schema(description = "岗位名称")
    @ExcelProperty("岗位名称")
    @ColumnWidth(50)
    private String name;

    @Schema(description = "岗位备注")
    @ExcelProperty("岗位备注")
    @ColumnWidth(50)
    private String remark;

    @Schema(description = "岗位排序")
    @ExcelProperty("岗位排序")
    @ColumnWidth(50)
    private Integer sort;

    @Schema(description = "是否停用: [0=否, 1=是]")
    @ExcelProperty("是否停用: [0=否, 1=是]")
    @ColumnWidth(50)
    private Integer status;

    @Schema(description = "状态描述")
    @ExcelProperty("状态描述")
    @ColumnWidth(50)
    private String statusDesc;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    @ColumnWidth(50)
    private String createTime;

    @Schema(description = "更新时间")
    @ExcelProperty("更新时间")
    @ColumnWidth(50)
    private String updateTime;

}
