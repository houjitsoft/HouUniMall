package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会员回调通知流水。
 *
 * <p>唯一键 {@code (biz_user_id, notify_type, notify_key)} 承担幂等：渠道回调会重复发，
 * 靠插入是否成功判断这条通知有没有处理过，重复的直接忽略，避免同一条审核结果被应用两次。
 *
 * @author Amos
 */
@Data
@TableName("pay_member_notify")
public class PayMemberNotifyEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String bizUserId;

    /** 通知类型：REG_ENTERPRISE / REG_SUPPLEMENT / BIND_PHONE / MEM_PROTOCOL */
    private String notifyType;

    /** 幂等键：会员号 + 类型 + 渠道侧时间/流水 */
    private String notifyKey;

    private String auditResult;

    /** 原始通知内容，排障用 */
    private String payload;

    private LocalDateTime createTime;
}
