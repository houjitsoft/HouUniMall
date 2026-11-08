package com.houjiang.aiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.aiagent.entity.AiBalance;
import com.houjiang.aiagent.mapper.AiBalanceMapper;
import com.houjiang.aiagent.support.AiPageSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 中台 - 余额与配额。
 *
 * <p>迁移自 GEO（{@code hou_geo_ai_balance}）。中台接管后，GEO 通过同名视图继续读写，
 * 本服务是同一份数据的权威入口，将来 GEO 改为远程调用时无需再做数据迁移。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiBalanceService {

    private static final long DEFAULT_FREE_QUOTA = 100_000L;

    private final AiBalanceMapper balanceMapper;

    public Map<String, Object> list(Map<String, Object> params) {
        LambdaQueryWrapper<AiBalance> wrapper = new LambdaQueryWrapper<>();
        String subjectType = AiPageSupport.str(params, "subjectType");
        if (StringUtils.hasText(subjectType)) {
            wrapper.eq(AiBalance::getSubjectType, subjectType);
        }
        String keyword = AiPageSupport.str(params, "keyword");
        if (StringUtils.hasText(keyword)) {
            wrapper.like(AiBalance::getSubjectKey, keyword);
        }
        wrapper.orderByDesc(AiBalance::getUpdateTime);
        IPage<AiBalance> page = balanceMapper.selectPage(AiPageSupport.page(params), wrapper);
        return AiPageSupport.pageResult(page, page.getRecords());
    }

    public Map<String, Object> detail(String subjectType, String subjectKey) {
        AiBalance balance = find(subjectType, subjectKey);
        return balance == null ? new LinkedHashMap<>() : toView(balance);
    }

    /**
     * 扣减额度。原子条件更新，余额不足时返回 false（不会发生超扣）。
     */
    public boolean consume(String subjectType, String subjectKey, long tokens) {
        if (!StringUtils.hasText(subjectType) || !StringUtils.hasText(subjectKey) || tokens <= 0) {
            return false;
        }
        ensure(subjectType, subjectKey);
        return balanceMapper.consume(subjectType, subjectKey, tokens) > 0;
    }

    public boolean recharge(String subjectType, String subjectKey, long tokens) {
        if (!StringUtils.hasText(subjectType) || !StringUtils.hasText(subjectKey) || tokens <= 0) {
            return false;
        }
        ensure(subjectType, subjectKey);
        return balanceMapper.recharge(subjectType, subjectKey, tokens) > 0;
    }

    /** 可用额度 = 免费额度 + 已购余额 - 累计已用 */
    public long available(String subjectType, String subjectKey) {
        AiBalance balance = find(subjectType, subjectKey);
        if (balance == null) {
            return 0L;
        }
        long free = nz(balance.getFreeQuotaTokens());
        long purchased = nz(balance.getPurchasedBalanceTokens());
        long used = nz(balance.getUsedTokens());
        return Math.max(0L, free + purchased - used);
    }

    public AiBalance find(String subjectType, String subjectKey) {
        if (!StringUtils.hasText(subjectType) || !StringUtils.hasText(subjectKey)) {
            return null;
        }
        return balanceMapper.selectOne(new LambdaQueryWrapper<AiBalance>()
                .eq(AiBalance::getSubjectType, subjectType)
                .eq(AiBalance::getSubjectKey, subjectKey)
                .last("limit 1"));
    }

    /** 不存在则按默认免费额度开户；已存在则原样返回 */
    public AiBalance ensure(String subjectType, String subjectKey) {
        AiBalance exist = find(subjectType, subjectKey);
        if (exist != null) {
            return exist;
        }
        AiBalance created = new AiBalance();
        created.setSubjectType(subjectType);
        created.setSubjectKey(subjectKey);
        created.setFreeQuotaTokens(DEFAULT_FREE_QUOTA);
        created.setFreeUsedTokens(0L);
        created.setUsedTokens(0L);
        created.setPurchasedBalanceTokens(0L);
        created.setFreeQuotaStartedAt(LocalDateTime.now());
        try {
            balanceMapper.insert(created);
        } catch (Exception e) {
            // 并发下可能已被别的线程开户，直接回查
            log.debug("[aiagent] 余额开户冲突，回查 subject={}:{} msg={}", subjectType, subjectKey, e.getMessage());
            return find(subjectType, subjectKey);
        }
        return created;
    }

    private Map<String, Object> toView(AiBalance b) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", b.getId());
        item.put("subjectType", b.getSubjectType());
        item.put("subjectKey", b.getSubjectKey());
        item.put("freeQuotaTokens", b.getFreeQuotaTokens());
        item.put("freeUsedTokens", b.getFreeUsedTokens());
        item.put("usedTokens", b.getUsedTokens());
        item.put("purchasedBalanceTokens", b.getPurchasedBalanceTokens());
        item.put("availableTokens", available(b.getSubjectType(), b.getSubjectKey()));
        item.put("freeQuotaStartedAt", b.getFreeQuotaStartedAt());
        item.put("freeQuotaExpiresAt", b.getFreeQuotaExpiresAt());
        item.put("updateTime", b.getUpdateTime());
        return item;
    }

    private static long nz(Long value) {
        return value == null ? 0L : value;
    }
}
