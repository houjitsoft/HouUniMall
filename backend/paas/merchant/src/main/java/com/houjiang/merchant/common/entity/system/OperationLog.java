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
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "操作人ID")
    private Integer adminId;

    private String adminName;

    private String account;

    @Schema(description = "操作标题")
    private String action;

    @Schema(description = "请求类型: GET/POST/PUT")
    private String type;

    @Schema(description = "请求接口")
    private String url;

    @Schema(description = "请求参数")
    private String params;

    @Schema(description = "请求结果")
    private String result;

    @Schema(description = "请求IP")
    private String ip;

    @Schema(description = "创建时间")
    private Long createTime;

}
