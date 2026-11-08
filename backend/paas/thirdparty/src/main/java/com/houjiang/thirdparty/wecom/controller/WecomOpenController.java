package com.houjiang.thirdparty.wecom.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.wecom.service.WecomOpenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/wecom/open")
public class WecomOpenController {

    private final WecomOpenService wecomOpenService;

    @GetMapping("/departments")
    public ResponseResult<List<Map<String, Object>>> departments(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String orgId) {
        var api = wecomOpenService.fetchDepartments(orgId, id);
        if (!api.isOk()) {
            return ResponseResult.error(api.toUserMessage());
        }
        List<Map<String, Object>> data = api.getData() != null ? api.getData() : Collections.emptyList();
        return ResponseResult.success(data);
    }

    @GetMapping("/users")
    public ResponseResult<List<Map<String, Object>>> users(
            @RequestParam(defaultValue = "1") int departmentId,
            @RequestParam(required = false) String orgId) {
        var api = wecomOpenService.fetchUsers(orgId, departmentId);
        if (!api.isOk()) {
            return ResponseResult.error(api.toUserMessage());
        }
        List<Map<String, Object>> data = api.getData() != null ? api.getData() : Collections.emptyList();
        return ResponseResult.success(data);
    }

    @GetMapping("/tags")
    public ResponseResult<List<Map<String, Object>>> tags() {
        var api = wecomOpenService.fetchCorpTags();
        if (!api.isOk()) {
            return ResponseResult.error(api.toUserMessage());
        }
        List<Map<String, Object>> data = api.getData() != null ? api.getData() : Collections.emptyList();
        return ResponseResult.success(data);
    }

    @GetMapping("/group-chats")
    public ResponseResult<?> groupChats(
            @RequestParam(required = false) String orgId,
            @RequestParam(required = false) String ownerUserid,
            @RequestParam(required = false, defaultValue = "false") boolean refresh) {
        if (org.springframework.util.StringUtils.hasText(ownerUserid)) {
            return ResponseResult.success(wecomOpenService.queryGroupChatsByOwner(orgId, ownerUserid, refresh));
        }
        return ResponseResult.success(wecomOpenService.listGroupChats());
    }

    @GetMapping("/group-chat")
    public ResponseResult<Map<String, Object>> groupChat(
            @RequestParam(required = false) String orgId,
            @RequestParam String chatId) {
        return ResponseResult.success(wecomOpenService.getGroupChatDetail(orgId, chatId));
    }

    @GetMapping("/follow-users")
    public ResponseResult<List<String>> followUsers() {
        return ResponseResult.success(wecomOpenService.listFollowUsers());
    }

    @GetMapping("/kf/accounts")
    public ResponseResult<List<Map<String, Object>>> kfAccounts() {
        return ResponseResult.success(wecomOpenService.listKfAccounts());
    }

    @GetMapping("/kf/servicers")
    public ResponseResult<List<Map<String, Object>>> kfServicers(@RequestParam String openKfid) {
        return ResponseResult.success(wecomOpenService.listKfServicers(openKfid));
    }

    @PostMapping("/kf/servicers/batch")
    public ResponseResult<List<Map<String, Object>>> kfServicersBatch(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> openKfidList = body != null && body.get("open_kfid_list") instanceof List<?>
                ? (List<String>) body.get("open_kfid_list")
                : List.of();
        return ResponseResult.success(wecomOpenService.listKfServicersBatch(openKfidList));
    }

    @GetMapping("/external-contact/ids")
    public ResponseResult<List<String>> externalContactIds(@RequestParam String userid) {
        return ResponseResult.success(wecomOpenService.listExternalContactIds(userid));
    }

    @GetMapping("/external-contact")
    public ResponseResult<Map<String, Object>> externalContact(@RequestParam String externalUserid) {
        return ResponseResult.success(wecomOpenService.getExternalContact(externalUserid));
    }

    @PostMapping("/external-contact/batch-by-user")
    public ResponseResult<List<Map<String, Object>>> batchExternalContacts(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> useridList = body != null && body.get("userid_list") instanceof List<?>
                ? (List<String>) body.get("userid_list")
                : List.of();
        return ResponseResult.success(wecomOpenService.batchListExternalContacts(useridList));
    }

    @PostMapping("/contact-way")
    public ResponseResult<Map<String, Object>> contactWay(@RequestBody Map<String, Object> body) {
        return ResponseResult.success(wecomOpenService.addContactWay(body));
    }
}
