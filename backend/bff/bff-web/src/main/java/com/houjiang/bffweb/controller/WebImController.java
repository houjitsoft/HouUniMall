package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.ImServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 即时通讯BFF适配器
/**
 * 只做请求转发，不包含业务逻辑
 * @author Amos
 */
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/im")
public class WebImController {

    @Autowired
    private ImServiceFeign imServiceFeign;

    // ================= 用户相关接口 =================
    
    /**
     * 客服登录
     */
    @PostMapping("/kf/login")
    public ResponseResult kfLogin(@RequestBody Map<String, Object> params) {
        return imServiceFeign.kfLogin(params);
    }

    /**
     * 客服登出
     */
    @PostMapping("/kf/logout")
    public ResponseResult kfLogout(@RequestBody Map<String, Object> params) {
        return imServiceFeign.kfLogout(params);
    }

    /**
     * 获取客服信息
     */
    @GetMapping("/kf/info")
    public ResponseResult getKfInfo(@RequestParam Map<String, Object> params) {
        return imServiceFeign.getKfInfo(params);
    }

    /** ================= 消息相关接口 ================= */
    
    @PostMapping("/message/send")
    public ResponseResult sendMessage(@RequestBody Map<String, Object> params) {
        return imServiceFeign.sendMessage(params);
    }

    @GetMapping("/message/history")
    public ResponseResult getMessageHistory(@RequestParam Map<String, Object> params) {
        return imServiceFeign.getMessageHistory(params);
    }

    /** ================= 群组相关接口 ================= */
    
    @PostMapping("/group/create")
    public ResponseResult createGroup(@RequestBody Map<String, Object> params) {
        return imServiceFeign.createGroup(params);
    }

    @GetMapping("/group/list")
    public ResponseResult getGroupList(@RequestParam Map<String, Object> params) {
        return imServiceFeign.getGroupList(params);
    }

    /** ================= 好友相关接口 ================= */
    
    @PostMapping("/friend/add")
    public ResponseResult addFriend(@RequestBody Map<String, Object> params) {
        return imServiceFeign.addFriend(params);
    }

    @GetMapping("/friend/list")
    public ResponseResult getFriendList(@RequestParam Map<String, Object> params) {
        return imServiceFeign.getFriendList(params);
    }

    // ================= 会话相关接口 =================
    
    /**
     * 查询会话列表
     */
    @GetMapping("/conversation/list")
    public ResponseResult getConversationList(@RequestParam Map<String, Object> params) {
        return imServiceFeign.getConversationList(params);
    }

    /**
     * 关闭会话
     */
    @PostMapping("/conversation/close")
    public ResponseResult closeConversation(@RequestBody Map<String, Object> params) {
        return imServiceFeign.closeConversation(params);
    }

    // ================= 自动回复相关接口 =================
    
    /**
     * 查询自动回复列表
     */
    @GetMapping("/auto-reply/list")
    public ResponseResult getAutoReplyList(@RequestParam Map<String, Object> params) {
        return imServiceFeign.getAutoReplyList(params);
    }

    /**
     * 添加/编辑自动回复
     */
    @PostMapping("/auto-reply/update")
    public ResponseResult updateAutoReply(@RequestBody Map<String, Object> params) {
        return imServiceFeign.updateAutoReply(params);
    }

    /**
     * 删除自动回复
     */
    @PostMapping("/auto-reply/delete")
    public ResponseResult deleteAutoReply(@RequestBody Map<String, Object> params) {
        return imServiceFeign.deleteAutoReply(params);
    }

    // ================= 快捷用语相关接口 =================
    
    /**
     * 查询快捷用语列表
     */
    @GetMapping("/quick-phrase/list")
    public ResponseResult getQuickPhraseList(@RequestParam Map<String, Object> params) {
        return imServiceFeign.getQuickPhraseList(params);
    }

    /**
     * 添加/编辑快捷用语
     */
    @PostMapping("/quick-phrase/update")
    public ResponseResult updateQuickPhrase(@RequestBody Map<String, Object> params) {
        return imServiceFeign.updateQuickPhrase(params);
    }

    /**
     * 删除快捷用语
     */
    @PostMapping("/quick-phrase/delete")
    public ResponseResult deleteQuickPhrase(@RequestBody Map<String, Object> params) {
        return imServiceFeign.deleteQuickPhrase(params);
    }

    // ================= 客服评价相关接口 =================

    @PostMapping("/customer/service/evaluation/submit")
    public ResponseResult submitCustomerServiceEvaluation(@RequestBody Map<String, Object> params) {
        return imServiceFeign.submitCustomerServiceEvaluation(params);
    }

    @GetMapping("/customer/service/evaluation/conversation/{conversationId}")
    public ResponseResult getCustomerServiceEvaluationByConversation(@PathVariable Integer conversationId) {
        return imServiceFeign.getCustomerServiceEvaluationByConversation(conversationId);
    }

    @GetMapping("/customer/service/evaluation/list")
    public ResponseResult listCustomerServiceEvaluations(@RequestParam Map<String, Object> params) {
        return imServiceFeign.listCustomerServiceEvaluations(params);
    }

    @GetMapping("/customer/service/evaluation/listPage")
    public ResponseResult listCustomerServiceEvaluationsPage(@RequestParam Map<String, Object> params) {
        return imServiceFeign.listCustomerServiceEvaluationsPage(params);
    }

    @GetMapping("/customer/service/evaluation/info/{id}")
    public ResponseResult getCustomerServiceEvaluation(@PathVariable Long id) {
        return imServiceFeign.getCustomerServiceEvaluation(id);
    }

    // ================= 客服会话统计相关接口 =================

    @GetMapping("/csSessionStats/list")
    public ResponseResult listCustomerServiceSessionStats(@RequestParam Map<String, Object> params) {
        return imServiceFeign.listCustomerServiceSessionStats(params);
    }

    @GetMapping("/csSessionStats/info/{id}")
    public ResponseResult getCustomerServiceSessionStats(@PathVariable Long id) {
        return imServiceFeign.getCustomerServiceSessionStats(id);
    }

    @GetMapping("/csSessionStats/dailySummary")
    public ResponseResult getCustomerServiceSessionDailySummary(@RequestParam Map<String, Object> params) {
        return imServiceFeign.getCustomerServiceSessionDailySummary(params);
    }

    @GetMapping("/csSessionStats/kfSummary")
    public ResponseResult getCustomerServiceSessionKfSummary(@RequestParam Map<String, Object> params) {
        return imServiceFeign.getCustomerServiceSessionKfSummary(params);
    }

    @GetMapping("/csSessionStats/statsIndex")
    public ResponseResult getCustomerServiceSessionStatsIndex() {
        return imServiceFeign.getCustomerServiceSessionStatsIndex();
    }
}
