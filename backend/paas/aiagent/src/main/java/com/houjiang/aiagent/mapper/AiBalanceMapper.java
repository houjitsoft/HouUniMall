package com.houjiang.aiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.aiagent.entity.AiBalance;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 余额扣减必须原子：用一条条件 UPDATE 同时完成「余额充足判断 + 扣减」，
 * 返回影响行数，0 即代表余额不足，避免并发下超扣。
 */
public interface AiBalanceMapper extends BaseMapper<AiBalance> {

    @Update("UPDATE hou_ai_balance SET "
            + "used_tokens = used_tokens + #{tokens}, "
            + "free_used_tokens = LEAST(free_quota_tokens, free_used_tokens + #{tokens}), "
            + "update_time = NOW() "
            + "WHERE subject_type = #{subjectType} AND subject_key = #{subjectKey} "
            + "AND (free_quota_tokens + purchased_balance_tokens - used_tokens) >= #{tokens}")
    int consume(@Param("subjectType") String subjectType,
                @Param("subjectKey") String subjectKey,
                @Param("tokens") long tokens);

    @Update("UPDATE hou_ai_balance SET "
            + "purchased_balance_tokens = purchased_balance_tokens + #{tokens}, "
            + "update_time = NOW() "
            + "WHERE subject_type = #{subjectType} AND subject_key = #{subjectKey}")
    int recharge(@Param("subjectType") String subjectType,
                 @Param("subjectKey") String subjectKey,
                 @Param("tokens") long tokens);
}
