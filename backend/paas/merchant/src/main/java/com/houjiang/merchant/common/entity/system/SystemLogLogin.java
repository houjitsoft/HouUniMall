package com.houjiang.merchant.common.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SystemLogLogin implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "管理员ID")
    private Integer adminId;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "登录IP")
    private String ip;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "浏览器")
    private String browser;

    @Schema(description = "操作状态: [1=成功, 2=失败]")
    private Integer status;

    @Schema(description = "创建时间")
    private Long createTime;

}
