package com.houjiang.merchant.vo.user;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class UserListExportVo  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @ExcelProperty("ID")
    @ColumnWidth(50)
    private Integer id;
    @Schema(description = "用户编码")
    @ExcelProperty("用户编码")
    @ColumnWidth(50)
    private Integer sn;
    @Schema(description = "用户头像")
    @ExcelProperty("用户头像")
    @ColumnWidth(50)
    private String avatar;
    @Schema(description = "真实姓名")
    @ExcelProperty("真实姓名")
    @ColumnWidth(50)
    private String realName;
    @Schema(description = "用户昵称")
    @ExcelProperty("用户昵称")
    @ColumnWidth(50)
    private String nickname;
    @Schema(description = "登录账号")
    @ExcelProperty("登录账号")
    @ColumnWidth(50)
    private String account;
    @Schema(description = "手机号码")
    @ExcelProperty("手机号码")
    @ColumnWidth(50)
    private String mobile;
    @Schema(description = "用户性别")
    @ExcelProperty("用户性别")
    @ColumnWidth(50)
    private String sex;
    @Schema(description = "注册渠道")
    @ExcelProperty("注册渠道")
    @ColumnWidth(50)
    private String channel;
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
    public void setSex(Integer sex) {
        switch (sex) {
            case 0:
                this.sex = "未知";
                break;
            case 1:
                this.sex = "男";
                break;
            case 2:
                this.sex = "女";
                break;
        }
    }
}
