package com.houjiang.aiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 中台 - 主体余额与配额。
 *
 * <p>迁移自 GEO 的 {@code hou_geo_ai_balance}（见 28/29 号脚本），
 * GEO 侧已改为读写同名视图，数据落在本表，实现「GEO 代码零改动迁入中台」。
 *
 * <p>可用额度口径：{@code free_quota_tokens + purchased_balance_tokens - used_tokens}。
 * {@code free_used_tokens} 只表示已用额度中走免费部分的量，用于展示，不参与可用性判断。
 */
@Data
@TableName("hou_ai_balance")
public class AiBalance {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** tenant 租户 / guest 访客 / user 用户 */
    private String subjectType;

    private String subjectKey;

    private Long freeQuotaTokens;

    private Long freeUsedTokens;

    private LocalDateTime freeQuotaStartedAt;

    private LocalDateTime freeQuotaExpiresAt;

    /** 累计已用 token（含走免费额度与走付费余额的部分） */
    private Long usedTokens;

    private Long purchasedBalanceTokens;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
