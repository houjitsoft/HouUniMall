package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * IM即时通讯服务Feign降级处理类
/**
 * 
 * @description 当IM服务不可用时提供降级响应
 * @author Amos
 * @date 2024-12-20
 */
@Component
public class ImServiceFeignFallback implements ImServiceFeign {

    /** ================= 用户相关接口 ================= */
    
    @Override
    public ResponseResult kfLogin(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult kfLogout(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getKfInfo(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getUserSignature(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getUserInfo(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateUserInfo(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    /** ================= 好友相关接口 ================= */
    
    @Override
    public ResponseResult addFriend(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteFriend(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getFriendList(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getFriendApplyList(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult handleFriendApply(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    /** ================= 群组相关接口 ================= */
    
    @Override
    public ResponseResult createGroup(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult dismissGroup(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getGroupList(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getGroupMembers(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult inviteToGroup(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult kickFromGroup(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult quitGroup(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateGroupInfo(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    /** ================= 消息相关接口 ================= */
    
    @Override
    public ResponseResult sendMessage(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getMessageHistory(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult recallMessage(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteMessage(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult markMessageRead(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getUnreadCount(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }
    
    /** ================= 会话相关接口 ================= */
    
    @Override
    public ResponseResult getConversationList(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult closeConversation(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }
    
    /** ================= 自动回复相关接口 ================= */
    
    @Override
    public ResponseResult getAutoReplyList(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updateAutoReply(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deleteAutoReply(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }
    
    /** ================= 快捷用语相关接口 ================= */
    
    @Override
    public ResponseResult getQuickPhraseList(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updateQuickPhrase(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deleteQuickPhrase(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult submitCustomerServiceEvaluation(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCustomerServiceEvaluationByConversation(Integer conversationId) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult listCustomerServiceEvaluations(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult listCustomerServiceEvaluationsPage(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCustomerServiceEvaluation(Long id) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult listCustomerServiceSessionStats(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCustomerServiceSessionStats(Long id) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCustomerServiceSessionDailySummary(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCustomerServiceSessionKfSummary(Map<String, Object> param) {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCustomerServiceSessionStatsIndex() {
        return ResponseResult.fail("IM服务暂时不可用，请稍后重试");
    }
}
