package com.houjiang.logging.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体类
 *
 * <p>映射 hou_log_login 表，记录用户的登录行为，
 * 包括登录方式、设备信息及登录结果。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Data
@TableName("hou_log_login")
@Schema(description = "登录日志实体")
public class LogLoginEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录方式（password:密码 / sms:短信 / wechat:微信 / third:第三方）")
    private String loginType;

    @Schema(description = "登录状态：1成功 0失败")
    private Integer loginStatus;

    @Schema(description = "客户端IP地址")
    private String ip;

    @Schema(description = "登录地点（由IP解析，可选）")
    private String location;

    @Schema(description = "设备类型")
    private String device;

    @Schema(description = "浏览器名称及版本")
    private String browser;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "登录失败原因")
    private String failReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
