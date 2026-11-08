package com.houjiang.mall.entity.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 插件开发者主体（hou_plugin_developer）。
 *
 * <p><b>为什么需要这张表</b>：审计 2026-10-27 的结论是「发布能力只对平台管理员开放」——
 * 没有开发者身份，就没有「谁在发布、能发几个、出了问题找谁」的答案，
 * 发布审核也只能凭感觉。本表把发布主体显式化：一个平台用户对应一个开发者身份，
 * 带审核状态与配额，审核通过才能作为独立发布方上架插件。
 *
 * @author Amos
 * @since 2026-10-27
 */
@Data
@TableName("hou_plugin_developer")
public class PluginDeveloper implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 0=待审核 1=正常 2=冻结 3=驳回 */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_FROZEN = 2;
    public static final int STATUS_REJECTED = 3;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 平台用户 ID（hou_auth_user.id），一个用户只能有一个开发者身份 */
    private Long userId;

    /** 对外展示的开发者/工作室名称 */
    private String displayName;

    private String contactEmail;

    private String contactPhone;

    /** 公司主体；个人开发者为空 */
    private String company;

    /** 纳税人识别号 */
    private String taxNo;

    /** 开发者简介，市场详情页展示 */
    private String intro;

    /** 0=待审核 1=正常 2=冻结 3=驳回 */
    private Integer status;

    /** 可上架插件数上限 */
    private Integer quotaPlugins;

    /** 每月可上传版本数上限 */
    private Integer quotaVersionsMonthly;

    private String reviewNote;

    private LocalDateTime reviewTime;

    private Long reviewerId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime deleteTime;
}
