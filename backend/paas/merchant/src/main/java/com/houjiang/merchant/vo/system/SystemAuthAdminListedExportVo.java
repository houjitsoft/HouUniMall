package com.houjiang.merchant.vo.system;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class SystemAuthAdminListedExportVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @ExcelProperty("主键")
    @ColumnWidth(50)
    private Integer id;

    @Schema(description = "账号")
    @ExcelProperty("账号")
    @ColumnWidth(50)
    private String account;

    @Schema(description = "昵称")
    @ExcelProperty("昵称")
    @ColumnWidth(50)
    private String name;

    @Schema(description = "头像")
    @ExcelProperty("头像")
    @ColumnWidth(50)
    private String avatar;

    @Schema(description = "部门")
    @ExcelProperty("部门")
    @ColumnWidth(50)
    private String deptName;

    @Schema(description = "部门")
    @ExcelProperty("部门")
    @ColumnWidth(50)
    private String roleName;

    @Schema(description = "部门")
    @ExcelProperty("部门")
    @ColumnWidth(50)
    private String jobsName;

    @Schema(description = "角色ID")
    @ExcelProperty("角色ID")
    @ColumnWidth(50)
    private List<Integer> roleId;

    @Schema(description = "部门ID")
    @ExcelProperty("部门ID")
    @ColumnWidth(50)
    private List<Integer> deptId;

    @Schema(description = "岗位ID")
    @ExcelProperty("ID")
    @ColumnWidth(50)
    private List<Integer> jobsId;

    @Schema(description = "多端登录: [0=否, 1=是]")
    @ExcelProperty("多端登录: [0=否, 1=是]")
    @ColumnWidth(50)
    private Integer multipointLogin;

    @Schema(description = "是否禁用: [0=否, 1=是]")
    @ExcelProperty("是否禁用: [0=否, 1=是]")
    @ColumnWidth(50)
    private Integer disable;

    @Schema(description = "是否禁用")
    @ExcelProperty("是否禁用")
    @ColumnWidth(50)
    private String disableDesc;

    @Schema(description = "root")
    @ExcelProperty("root")
    @ColumnWidth(50)
    private Integer root;

    @Schema(description = "最后登录IP")
    @ExcelProperty("最后登录IP")
    @ColumnWidth(50)
    private String loginIp;

    @Schema(description = "最后登录时间")
    @ExcelProperty("最后登录时间")
    @ColumnWidth(50)
    private String loginTime;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    @ColumnWidth(50)
    private String createTime;

    @Schema(description = "更新时间")
    @ExcelProperty("更新时间")
    @ColumnWidth(50)
    private String updateTime;

}
