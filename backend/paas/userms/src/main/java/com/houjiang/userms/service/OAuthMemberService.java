package com.houjiang.userms.service;

import com.houjiang.userms.dto.OAuthIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthMemberService {
    private final OAuthMemberTransaction transaction;

    public Map<String, Object> getOrCreate(Map<String, String> body) {
        OAuthIdentity identity = OAuthIdentity.from(body);
        try {
            return transaction.getOrCreate(identity);
        } catch (DuplicateKeyException collision) {
            // 原事务已回滚；新事务/新快照查询并发赢家，不能在失败事务中吞异常。
            Map<String, Object> winner = transaction.find(identity);
            if (winner != null) return winner;
            throw collision;
        }
    }
}
