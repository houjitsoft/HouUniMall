package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * IM即时通讯服务Feign客户端
/**
 * 
 * @description 调用paas中台IM服务(im)的接口
 * @author Amos
 * @date 2024-12-20
 */
@FeignClient(name = "im", url = "${feign.im.url:}", path = "/plugin", fallback = ImServiceFeignFallback.class, contextId = "imServiceFeign")
public interface ImServiceFeign {

    // ================= 用户相关接口 =================
    
    /**
     * 客服登录
     */
    @PostMapping("/im/user/imKfLogin")
    ResponseResult kfLogin(@RequestBody Map<String, Object> param);
    
    /**
     * 客服登出
     */
    @PostMapping("/im/user/kfLogOut")
    ResponseResult kfLogout(@RequestBody Map<String, Object> param);
    
    /**
     * 获取客服信息
     */
    @PostMapping("/im/user/imGetUser")
    ResponseResult getKfInfo(@RequestBody Map<String, Object> param);
    
    /**
     * 获取用户签名
     */
    @GetMapping("/im/user/signature")
    ResponseResult getUserSignature(@RequestParam Map<String, Object> param);
    
    /**
     * 查询用户信息
     */
    @GetMapping("/im/user/info")
    ResponseResult getUserInfo(@RequestParam Map<String, Object> param);
    
    /**
     * 更新用户信息
     */
    @PostMapping("/im/user/update")
    ResponseResult updateUserInfo(@RequestBody Map<String, Object> param);
    
    // ================= 好友相关接口 =================
    
    /**
     * 添加好友
     */
    @PostMapping("/im/friend/add")
    ResponseResult addFriend(@RequestBody Map<String, Object> param);
    
    /**
     * 删除好友
     */
    @PostMapping("/im/friend/delete")
    ResponseResult deleteFriend(@RequestBody Map<String, Object> param);
    
    /**
     * 查询好友列表
     */
    @GetMapping("/im/friend/list")
    ResponseResult getFriendList(@RequestParam Map<String, Object> param);
    
    /**
     * 查询好友申请列表
     */
    @GetMapping("/im/friend/apply-list")
    ResponseResult getFriendApplyList(@RequestParam Map<String, Object> param);
    
    /**
     * 处理好友申请
     */
    @PostMapping("/im/friend/handle-apply")
    ResponseResult handleFriendApply(@RequestBody Map<String, Object> param);
    
    // ================= 群组相关接口 =================
    
    /**
     * 创建群组
     */
    @PostMapping("/im/group/create")
    ResponseResult createGroup(@RequestBody Map<String, Object> param);
    
    /**
     * 解散群组
     */
    @PostMapping("/im/group/dismiss")
    ResponseResult dismissGroup(@RequestBody Map<String, Object> param);
    
    /**
     * 查询群组列表
     */
    @GetMapping("/im/group/list")
    ResponseResult getGroupList(@RequestParam Map<String, Object> param);
    
    /**
     * 查询群组成员
     */
    @GetMapping("/im/group/members")
    ResponseResult getGroupMembers(@RequestParam Map<String, Object> param);
    
    /**
     * 邀请加入群组
     */
    @PostMapping("/im/group/invite")
    ResponseResult inviteToGroup(@RequestBody Map<String, Object> param);
    
    /**
     * 踢出群组成员
     */
    @PostMapping("/im/group/kick")
    ResponseResult kickFromGroup(@RequestBody Map<String, Object> param);
    
    /**
     * 退出群组
     */
    @PostMapping("/im/group/quit")
    ResponseResult quitGroup(@RequestBody Map<String, Object> param);
    
    /**
     * 修改群组信息
     */
    @PostMapping("/im/group/update")
    ResponseResult updateGroupInfo(@RequestBody Map<String, Object> param);
    
    // ================= 消息相关接口 =================
    
    /**
     * 发送消息
     */
    @PostMapping("/im/message/send")
    ResponseResult sendMessage(@RequestBody Map<String, Object> param);
    
    /**
     * 查询历史消息
     */
    @GetMapping("/im/message/history")
    ResponseResult getMessageHistory(@RequestParam Map<String, Object> param);
    
    /**
     * 撤回消息
     */
    @PostMapping("/im/message/recall")
    ResponseResult recallMessage(@RequestBody Map<String, Object> param);
    
    /**
     * 删除消息
     */
    @PostMapping("/im/message/delete")
    ResponseResult deleteMessage(@RequestBody Map<String, Object> param);
    
    /**
     * 标记已读
     */
    @PostMapping("/im/message/mark-read")
    ResponseResult markMessageRead(@RequestBody Map<String, Object> param);
    
    /**
     * 查询未读消息数量
     */
    @GetMapping("/im/message/unread-count")
    ResponseResult getUnreadCount(@RequestParam Map<String, Object> param);
    
    // ================= 会话相关接口 =================
    
    /**
     * 查询会话列表
     */
    @GetMapping("/im/conversation/list")
    ResponseResult getConversationList(@RequestParam Map<String, Object> param);
    
    /**
     * 关闭会话
     */
    @PostMapping("/im/conversation/close")
    ResponseResult closeConversation(@RequestBody Map<String, Object> param);
    
    // ================= 自动回复相关接口 =================
    
    /**
     * 查询自动回复列表
     */
    @GetMapping("/im/automatic-reply/list")
    ResponseResult getAutoReplyList(@RequestParam Map<String, Object> param);
    
    /**
     * 添加/编辑自动回复
     */
    @PostMapping("/im/automatic-reply/update")
    ResponseResult updateAutoReply(@RequestBody Map<String, Object> param);
    
    /**
     * 删除自动回复
     */
    @PostMapping("/im/automatic-reply/delete")
    ResponseResult deleteAutoReply(@RequestBody Map<String, Object> param);
    
    // ================= 快捷用语相关接口 =================
    
    /**
     * 查询快捷用语列表
     */
    @GetMapping("/im/quick-expressions/list")
    ResponseResult getQuickPhraseList(@RequestParam Map<String, Object> param);
    
    /**
     * 添加/编辑快捷用语
     */
    @PostMapping("/im/quick-expressions/update")
    ResponseResult updateQuickPhrase(@RequestBody Map<String, Object> param);
    
    /**
     * 删除快捷用语
     */
    @PostMapping("/im/quick-expressions/delete")
    ResponseResult deleteQuickPhrase(@RequestBody Map<String, Object> param);

    // ================= 客服评价相关接口 =================

    @PostMapping("/customer/service/evaluation/submit")
    ResponseResult submitCustomerServiceEvaluation(@RequestBody Map<String, Object> param);

    @GetMapping("/customer/service/evaluation/conversation/{conversationId}")
    ResponseResult getCustomerServiceEvaluationByConversation(@PathVariable("conversationId") Integer conversationId);

    @GetMapping("/customer/service/evaluation/list")
    ResponseResult listCustomerServiceEvaluations(@RequestParam Map<String, Object> param);

    @GetMapping("/customer/service/evaluation/listPage")
    ResponseResult listCustomerServiceEvaluationsPage(@RequestParam Map<String, Object> param);

    @GetMapping("/customer/service/evaluation/info/{id}")
    ResponseResult getCustomerServiceEvaluation(@PathVariable("id") Long id);

    // ================= 客服会话统计相关接口 =================

    @GetMapping("/csSessionStats/list")
    ResponseResult listCustomerServiceSessionStats(@RequestParam Map<String, Object> param);

    @GetMapping("/csSessionStats/info/{id}")
    ResponseResult getCustomerServiceSessionStats(@PathVariable("id") Long id);

    @GetMapping("/csSessionStats/dailySummary")
    ResponseResult getCustomerServiceSessionDailySummary(@RequestParam Map<String, Object> param);

    @GetMapping("/csSessionStats/kfSummary")
    ResponseResult getCustomerServiceSessionKfSummary(@RequestParam Map<String, Object> param);

    @GetMapping("/csSessionStats/statsIndex")
    ResponseResult getCustomerServiceSessionStatsIndex();
}
