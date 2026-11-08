package com.houjiang.thirdparty.wecom.service.impl;

import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.thirdparty.service.ThirdPartyConfigService;
import com.houjiang.thirdparty.wecom.client.WecomApiClient;
import com.houjiang.thirdparty.wecom.client.WecomApiResult;
import com.houjiang.thirdparty.wecom.service.WecomOpenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Amos
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WecomOpenServiceImpl implements WecomOpenService {

    private static final int GROUP_CHAT_FETCH_PARALLELISM = 8;
    private static final long GROUP_CHAT_CACHE_TTL_MS = 5 * 60 * 1000L;
    private static final long MEMBER_SCAN_WALL_CLOCK_MS = 25_000L;

    private static final ExecutorService GROUP_CHAT_FETCH_EXECUTOR = Executors.newFixedThreadPool(
            GROUP_CHAT_FETCH_PARALLELISM,
            r -> {
                Thread t = new Thread(r, "wecom-group-chat-fetch");
                t.setDaemon(true);
                return t;
            });

    private final ThirdPartyConfigService thirdPartyConfigService;
    private final WecomApiClient wecomApiClient;
    private final ConcurrentHashMap<String, CachedGroupChatQuery> groupChatByOwnerCache = new ConcurrentHashMap<>();

    private static final class CachedGroupChatQuery {
        private final Map<String, Object> data;
        private final long expireAt;

        private CachedGroupChatQuery(Map<String, Object> data, long expireAt) {
            this.data = data;
            this.expireAt = expireAt;
        }
    }

    private ThirdPartyConfigDTO.WechatWorkConfig loadWechatWork(String orgId) {
        return thirdPartyConfigService.resolveWechatWorkConfig(orgId);
    }

    private ThirdPartyConfigDTO.WechatWorkConfig loadWechatWork() {
        return loadWechatWork(null);
    }

    private String resolveSecret(ThirdPartyConfigDTO.WechatWorkConfig ww, boolean contact) {
        if (ww == null) {
            return null;
        }
        if (contact) {
            return resolveContactSecret(ww);
        }
        return ww.getSecret();
    }

    /** 客户联系 API：优先 contactSecret，未填则使用主 Secret（须为「可调用接口的应用」中已授权应用的 Secret） */
    private String resolveContactSecret(ThirdPartyConfigDTO.WechatWorkConfig ww) {
        if (ww == null) {
            return null;
        }
        if (StringUtils.hasText(ww.getContactSecret())) {
            return ww.getContactSecret().trim();
        }
        return StringUtils.hasText(ww.getSecret()) ? ww.getSecret().trim() : null;
    }

    private String contactSecretMissingHint() {
        return "未配置企业微信 Secret：请在系统设置→第三方集成 填写 Secret，"
                + "并在企微后台「客户联系→客户→可调用接口的应用」中授权该自建应用";
    }

    private String resolveAddressBookSecret(ThirdPartyConfigDTO.WechatWorkConfig ww) {
        if (ww == null) {
            return null;
        }
        if (StringUtils.hasText(ww.getAddressBookSecret())) {
            return ww.getAddressBookSecret();
        }
        return ww.getSecret();
    }

    private WecomApiResult<List<Map<String, Object>>> fetchUsersInternal(String orgId, int departmentId) {
        ThirdPartyConfigDTO.WechatWorkConfig ww = loadWechatWork(orgId);
        if (ww == null || !StringUtils.hasText(ww.getCorpId())) {
            return WecomApiResult.failure("未配置企业微信凭证：请在「第三方集成 → 企业微信」填写 CorpId、Secret 或通讯录 Secret");
        }
        String primarySecret = resolveAddressBookSecret(ww);
        if (!StringUtils.hasText(primarySecret)) {
            return WecomApiResult.failure("未配置企业微信 Secret：请在「第三方集成 → 企业微信」填写 Secret 或通讯录 Secret");
        }
        return fetchAddressBookWithFallback(
            ww.getCorpId(),
            primarySecret,
            ww.getSecret(),
            departmentId,
            true
        );
    }

    private WecomApiResult<List<Map<String, Object>>> fetchDepartmentsInternal(String orgId, Integer departmentId) {
        ThirdPartyConfigDTO.WechatWorkConfig ww = loadWechatWork(orgId);
        if (ww == null || !StringUtils.hasText(ww.getCorpId())) {
            return WecomApiResult.failure("未配置企业微信凭证：请在「第三方集成 → 企业微信」填写 CorpId、Secret 或通讯录 Secret");
        }
        String primarySecret = resolveAddressBookSecret(ww);
        if (!StringUtils.hasText(primarySecret)) {
            return WecomApiResult.failure("未配置企业微信 Secret：请在「第三方集成 → 企业微信」填写 Secret 或通讯录 Secret");
        }
        return fetchAddressBookWithFallback(
            ww.getCorpId(),
            primarySecret,
            ww.getSecret(),
            departmentId,
            false
        );
    }

    /**
     * 通讯录类接口：优先通讯录 Secret，失败或空列表时回退主 Secret（与考勤同步一致，避免仅通讯录应用未配可信 IP 时 60020）。
     */
    private WecomApiResult<List<Map<String, Object>>> fetchAddressBookWithFallback(
        String corpId,
        String primarySecret,
        String fallbackSecret,
        Integer departmentId,
        boolean users
    ) {
        WecomApiResult<List<Map<String, Object>>> primary = callAddressBookApi(
            corpId, primarySecret, departmentId, users
        );
        if (primary.isOk() && primary.getData() != null && !primary.getData().isEmpty()) {
            return primary;
        }
        if (StringUtils.hasText(fallbackSecret) && !fallbackSecret.equals(primarySecret)) {
            WecomApiResult<List<Map<String, Object>>> second = callAddressBookApi(
                corpId, fallbackSecret, departmentId, users
            );
            if (second.isOk() && second.getData() != null && !second.getData().isEmpty()) {
                log.info("通讯录 Secret {} 失败(errcode={})，已回退主 Secret 拉取{}",
                    users ? "user/list" : "department/list",
                    primary.getErrcode(),
                    users ? "员工" : "部门");
                return second;
            }
            if (second.isOk()) {
                return second;
            }
        }
        if (!primary.isOk() && isAddressBookSecretMisconfigured(primary.getErrcode())) {
            return WecomApiResult.failure(primary.getErrcode(),
                primary.getErrmsg() + "（已尝试回退主 Secret 仍失败，请检查 Secret 配置）");
        }
        return primary;
    }

    /** 通讯录 Secret 误填为客户联系助手等不可用于 user/list 的凭证 */
    private static boolean isAddressBookSecretMisconfigured(int errcode) {
        return errcode == 48009 || errcode == 48002;
    }

    private WecomApiResult<List<Map<String, Object>>> callAddressBookApi(
        String corpId,
        String secret,
        Integer departmentId,
        boolean users
    ) {
        String token = wecomApiClient.getAccessToken(corpId, secret);
        if (token == null) {
            return WecomApiResult.failure("获取 access_token 失败，请检查 CorpId 与 Secret");
        }
        if (users) {
            int deptId = departmentId != null ? departmentId : 1;
            return wecomApiClient.listDepartmentUsersDetailed(token, deptId);
        }
        return wecomApiClient.listDepartmentsDetailed(token, departmentId);
    }

    @Override
    public String getAccessToken() {
        ThirdPartyConfigDTO.WechatWorkConfig ww = loadWechatWork();
        if (ww == null || !StringUtils.hasText(ww.getCorpId()) || !StringUtils.hasText(ww.getSecret())) {
            return null;
        }
        return wecomApiClient.getAccessToken(ww.getCorpId(), ww.getSecret());
    }

    @Override
    public String getContactAccessToken() {
        return getContactAccessToken(null);
    }

    @Override
    public String getContactAccessToken(String orgId) {
        ThirdPartyConfigDTO.WechatWorkConfig ww = loadWechatWork(orgId);
        String secret = resolveContactSecret(ww);
        if (ww == null || !StringUtils.hasText(ww.getCorpId()) || !StringUtils.hasText(secret)) {
            return null;
        }
        return wecomApiClient.getAccessToken(ww.getCorpId(), secret);
    }

    @Override
    public List<Map<String, Object>> listDepartments(Integer departmentId) {
        WecomApiResult<List<Map<String, Object>>> result = fetchDepartments(departmentId);
        return result.isOk() && result.getData() != null ? result.getData() : Collections.emptyList();
    }

    @Override
    public WecomApiResult<List<Map<String, Object>>> fetchDepartments(Integer departmentId) {
        return fetchDepartments(null, departmentId);
    }

    @Override
    public WecomApiResult<List<Map<String, Object>>> fetchDepartments(String orgId, Integer departmentId) {
        return fetchDepartmentsInternal(orgId, departmentId);
    }

    @Override
    public List<Map<String, Object>> listUsers(int departmentId) {
        WecomApiResult<List<Map<String, Object>>> result = fetchUsers(departmentId);
        return result.isOk() && result.getData() != null ? result.getData() : Collections.emptyList();
    }

    @Override
    public WecomApiResult<List<Map<String, Object>>> fetchUsers(int departmentId) {
        return fetchUsers(null, departmentId);
    }

    @Override
    public WecomApiResult<List<Map<String, Object>>> fetchUsers(String orgId, int departmentId) {
        return fetchUsersInternal(orgId, departmentId);
    }

    @Override
    public List<Map<String, Object>> listCorpTags() {
        WecomApiResult<List<Map<String, Object>>> result = fetchCorpTags();
        return result.isOk() && result.getData() != null ? result.getData() : Collections.emptyList();
    }

    @Override
    public WecomApiResult<List<Map<String, Object>>> fetchCorpTags() {
        ThirdPartyConfigDTO.WechatWorkConfig ww = loadWechatWork();
        String secret = resolveSecret(ww, true);
        if (ww == null || !StringUtils.hasText(ww.getCorpId()) || !StringUtils.hasText(secret)) {
            return WecomApiResult.failure("未配置企业微信客户联系凭证：请在「第三方集成 → 企业微信」填写 CorpId、Secret，"
                    + "并建议填写「客户联系 Secret」（企微后台：客户联系 → 客户 → API）");
        }
        String token = wecomApiClient.getAccessToken(ww.getCorpId(), secret);
        if (token == null) {
            return WecomApiResult.failure("获取 access_token 失败，请检查 CorpId 与 Secret（客户联系接口建议使用 contactSecret）");
        }
        return wecomApiClient.getCorpTagListDetailed(token);
    }

    @Override
    public List<Map<String, Object>> listGroupChats() {
        String token = getContactAccessToken(null);
        if (token == null) {
            log.warn("客户群列表为空：无法获取客户联系 access_token");
            return Collections.emptyList();
        }
        return loadGroupChatOptions(token, null);
    }

    @Override
    public List<Map<String, Object>> listGroupChatsByOwner(String orgId, String ownerUserid) {
        if (!StringUtils.hasText(ownerUserid)) {
            String token = getContactAccessToken(orgId);
            if (token == null) {
                log.warn("客户群列表为空：无法获取客户联系 access_token，orgId={}", orgId);
                return Collections.emptyList();
            }
            return loadGroupChatOptions(token, null);
        }
        Map<String, Object> query = queryGroupChatsByOwner(orgId, ownerUserid);
        Object list = query != null ? query.get("list") : null;
        if (list instanceof List<?> raw) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> typed = (List<Map<String, Object>>) raw;
            return typed;
        }
        return Collections.emptyList();
    }

    private static final int GROUP_CHAT_OWNER_MAX = 200;
    private static final int GROUP_CHAT_MEMBER_SCAN_MAX = 800;
    private static final int GROUP_CHAT_MEMBER_RESULT_MAX = 200;

    @Override
    public Map<String, Object> queryGroupChatsByOwner(String orgId, String ownerUserid) {
        return queryGroupChatsByOwner(orgId, ownerUserid, false);
    }

    @Override
    public Map<String, Object> queryGroupChatsByOwner(String orgId, String ownerUserid, boolean refresh) {
        if (!StringUtils.hasText(ownerUserid)) {
            return queryGroupChatsByOwnerInternal(orgId, ownerUserid);
        }
        if (!StringUtils.hasText(orgId)) {
            return queryGroupChatsByOwnerInternal(null, ownerUserid);
        }
        String cacheKey = orgId.trim() + "|" + ownerUserid.trim();
        long now = System.currentTimeMillis();
        if (!refresh) {
            CachedGroupChatQuery cached = groupChatByOwnerCache.get(cacheKey);
            if (cached != null && cached.expireAt > now) {
                return new HashMap<>(cached.data);
            }
        }
        Map<String, Object> result = queryGroupChatsByOwnerInternal(orgId, ownerUserid);
        groupChatByOwnerCache.put(cacheKey, new CachedGroupChatQuery(new HashMap<>(result), now + GROUP_CHAT_CACHE_TTL_MS));
        return result;
    }

    private Map<String, Object> queryGroupChatsByOwnerInternal(String orgId, String ownerUserid) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", Collections.emptyList());
        if (!StringUtils.hasText(ownerUserid)) {
            result.put("hint", "缺少企微 userid");
            return result;
        }
        String trimmedOwner = ownerUserid.trim();
        ThirdPartyConfigDTO.WechatWorkConfig ww = loadWechatWork(orgId);
        if (ww == null || !StringUtils.hasText(ww.getCorpId())) {
            result.put("hint", "未配置企业微信 CorpId，请在系统设置→第三方集成 填写");
            result.put("contactTokenOk", false);
            result.put("contactSecretConfigured", false);
            return result;
        }
        String secret = resolveContactSecret(ww);
        if (!StringUtils.hasText(secret)) {
            result.put("hint", contactSecretMissingHint());
            result.put("contactTokenOk", false);
            result.put("contactSecretConfigured", false);
            return result;
        }
        result.put("contactSecretConfigured", true);
        if (StringUtils.hasText(ww.getSecret()) && secret.equals(ww.getSecret().trim())) {
            result.put("usingMainSecretAsContact", true);
        }
        String token = wecomApiClient.getAccessToken(ww.getCorpId(), secret);
        if (token == null) {
            result.put("hint", "客户联系 Secret 无效，无法获取 access_token，请检查 CorpId 与「客户联系 Secret」");
            result.put("contactTokenOk", false);
            return result;
        }
        result.put("contactTokenOk", true);

        GroupChatLoadOutcome owned = loadGroupChatOptionsDetailed(token, List.of(trimmedOwner), GROUP_CHAT_OWNER_MAX);
        if (owned.apiError != null) {
            result.put("hint", enrichContactApiErrorHint(owned.apiError.toUserMessage(), result));
            return result;
        }

        GroupChatLoadOutcome member = loadGroupChatsWhereMemberDetailed(token, trimmedOwner);
        List<Map<String, Object>> merged = mergeGroupChatLists(owned.groups, member.groups);
        result.put("list", merged);
        if (member.scannedCount > 0) {
            result.put("scannedCount", member.scannedCount);
        }
        if (merged.isEmpty()) {
            if (member.apiError != null) {
                result.put("hint", enrichContactApiErrorHint(member.apiError.toUserMessage(), result));
            } else if (member.scannedCount == 0 && owned.groups.isEmpty()) {
                result.put("hint", "企微 API 未返回任何客户群，请确认企业内已创建外部客户群且客户联系应用可见");
            } else {
                result.put("hint", "未找到该经理作为群主或成员的客户群（已扫描 "
                        + member.scannedCount + " 个群，若群数量较多请联系管理员扩大扫描范围）");
            }
        }
        return result;
    }

    private List<Map<String, Object>> mergeGroupChatLists(List<Map<String, Object>> owned,
                                                          List<Map<String, Object>> member) {
        LinkedHashMap<String, Map<String, Object>> byId = new LinkedHashMap<>();
        appendGroupChatRows(byId, owned);
        appendGroupChatRows(byId, member);
        List<Map<String, Object>> merged = new ArrayList<>(byId.values());
        if (merged.size() <= GROUP_CHAT_MEMBER_RESULT_MAX) {
            return merged;
        }
        return new ArrayList<>(merged.subList(0, GROUP_CHAT_MEMBER_RESULT_MAX));
    }

    private void appendGroupChatRows(LinkedHashMap<String, Map<String, Object>> byId, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (Map<String, Object> row : rows) {
            if (row == null || row.isEmpty()) {
                continue;
            }
            Object rawId = row.containsKey("chatId") ? row.get("chatId") : row.get("chat_id");
            if (rawId == null || !StringUtils.hasText(rawId.toString())) {
                continue;
            }
            String chatId = rawId.toString().trim();
            byId.putIfAbsent(chatId, row);
        }
    }

    @SuppressWarnings("unchecked")
    private String enrichContactApiErrorHint(String baseHint, Map<String, Object> meta) {
        if (baseHint == null) {
            return contactSecretMissingHint();
        }
        if (baseHint.contains("48002")) {
            return baseHint + "。请到企微「客户联系→客户→支持通过API管理客户→可调用接口的应用→修改」，"
                    + "勾选 HouCloud 使用的自建应用（如厚匠科技），保存后 Secret 填该应用在「应用管理」中的 Secret";
        }
        return baseHint;
    }

    private static final class GroupChatLoadOutcome {
        private final List<Map<String, Object>> groups;
        private final WecomApiResult<?> apiError;
        private final int scannedCount;

        private GroupChatLoadOutcome(List<Map<String, Object>> groups, WecomApiResult<?> apiError, int scannedCount) {
            this.groups = groups != null ? groups : Collections.emptyList();
            this.apiError = apiError;
            this.scannedCount = scannedCount;
        }
    }

    private List<Map<String, Object>> loadGroupChatOptions(String token, List<String> ownerUserids) {
        GroupChatLoadOutcome outcome = loadGroupChatOptionsDetailed(token, ownerUserids, GROUP_CHAT_OWNER_MAX);
        return outcome.groups;
    }

    private GroupChatLoadOutcome loadGroupChatOptionsDetailed(String token, List<String> ownerUserids, int maxCount) {
        WecomApiResult<List<Map<String, Object>>> idResult =
                wecomApiClient.listAllGroupChatIdsDetailed(token, ownerUserids, maxCount);
        if (!idResult.isOk()) {
            return new GroupChatLoadOutcome(Collections.emptyList(), idResult, 0);
        }
        List<Map<String, Object>> idRows = idResult.getData() != null ? idResult.getData() : Collections.emptyList();
        if (idRows.isEmpty()) {
            return new GroupChatLoadOutcome(Collections.emptyList(), null, 0);
        }
        List<Map<String, Object>> result = fetchGroupChatOptionsParallel(token, idRows);
        return new GroupChatLoadOutcome(result, null, idRows.size());
    }

    private List<Map<String, Object>> fetchGroupChatOptionsParallel(String token, List<Map<String, Object>> idRows) {
        List<String> chatIds = new ArrayList<>();
        for (Map<String, Object> row : idRows) {
            String chatId = row.get("chat_id") != null ? row.get("chat_id").toString() : null;
            if (StringUtils.hasText(chatId)) {
                chatIds.add(chatId.trim());
            }
        }
        if (chatIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> futures = new ArrayList<>(chatIds.size());
        for (String chatId : chatIds) {
            futures.add(CompletableFuture.runAsync(() -> {
                Map<String, Object> detail = wecomApiClient.getGroupChat(token, chatId);
                if (detail != null) {
                    result.add(toGroupChatOption(detail));
                    return;
                }
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("chat_id", chatId);
                fallback.put("chatId", chatId);
                fallback.put("name", chatId);
                result.add(fallback);
            }, GROUP_CHAT_FETCH_EXECUTOR));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return new ArrayList<>(result);
    }

    @SuppressWarnings("unchecked")
    private GroupChatLoadOutcome loadGroupChatsWhereMemberDetailed(String token, String userid) {
        WecomApiResult<List<Map<String, Object>>> idResult =
                wecomApiClient.listAllGroupChatIdsDetailed(token, null, GROUP_CHAT_MEMBER_SCAN_MAX);
        if (!idResult.isOk()) {
            return new GroupChatLoadOutcome(Collections.emptyList(), idResult, 0);
        }
        List<Map<String, Object>> idRows = idResult.getData() != null ? idResult.getData() : Collections.emptyList();
        if (idRows.isEmpty()) {
            return new GroupChatLoadOutcome(Collections.emptyList(), null, 0);
        }
        List<String> chatIds = new ArrayList<>(idRows.size());
        for (Map<String, Object> row : idRows) {
            String chatId = row.get("chat_id") != null ? row.get("chat_id").toString() : null;
            if (StringUtils.hasText(chatId)) {
                chatIds.add(chatId.trim());
            }
        }
        if (chatIds.isEmpty()) {
            return new GroupChatLoadOutcome(Collections.emptyList(), null, 0);
        }

        List<Map<String, Object>> result = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger scanned = new AtomicInteger(0);
        long deadline = System.currentTimeMillis() + MEMBER_SCAN_WALL_CLOCK_MS;
        int batchSize = GROUP_CHAT_FETCH_PARALLELISM * 4;
        for (int offset = 0; offset < chatIds.size(); offset += batchSize) {
            if (result.size() >= GROUP_CHAT_MEMBER_RESULT_MAX || System.currentTimeMillis() > deadline) {
                break;
            }
            int end = Math.min(offset + batchSize, chatIds.size());
            List<CompletableFuture<Void>> futures = new ArrayList<>(end - offset);
            for (int i = offset; i < end; i++) {
                if (result.size() >= GROUP_CHAT_MEMBER_RESULT_MAX || System.currentTimeMillis() > deadline) {
                    break;
                }
                String chatId = chatIds.get(i);
                futures.add(CompletableFuture.runAsync(() -> {
                    scanned.incrementAndGet();
                    if (result.size() >= GROUP_CHAT_MEMBER_RESULT_MAX || System.currentTimeMillis() > deadline) {
                        return;
                    }
                    Map<String, Object> detail = wecomApiClient.getGroupChat(token, chatId);
                    if (detail == null || !isGroupMember(detail, userid)) {
                        return;
                    }
                    synchronized (result) {
                        if (result.size() < GROUP_CHAT_MEMBER_RESULT_MAX) {
                            result.add(toGroupChatOption(detail));
                        }
                    }
                }, GROUP_CHAT_FETCH_EXECUTOR));
            }
            if (!futures.isEmpty()) {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }
        }
        return new GroupChatLoadOutcome(new ArrayList<>(result), null, scanned.get());
    }

    @SuppressWarnings("unchecked")
    private boolean isGroupMember(Map<String, Object> detail, String userid) {
        Object owner = detail.get("owner");
        if (owner != null && userid.equals(owner.toString())) {
            return true;
        }
        Object members = detail.get("member_list");
        if (!(members instanceof List<?> list)) {
            return false;
        }
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> member)) {
                continue;
            }
            Object memberUserid = member.get("userid");
            if (memberUserid != null && userid.equals(memberUserid.toString())) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> toGroupChatOption(Map<String, Object> detail) {
        Map<String, Object> item = new HashMap<>();
        String chatId = detail.get("chat_id") != null ? detail.get("chat_id").toString() : null;
        item.put("chat_id", chatId);
        item.put("chatId", chatId);
        Object name = detail.get("name");
        item.put("name", name != null ? name.toString() : chatId);
        Object owner = detail.get("owner");
        item.put("owner_userid", owner != null ? owner.toString() : null);
        Object members = detail.get("member_list");
        if (members instanceof List<?> list) {
            item.put("member_count", list.size());
            item.put("memberCount", list.size());
        }
        return item;
    }

    @Override
    public Map<String, Object> getGroupChatDetail(String orgId, String chatId) {
        if (!StringUtils.hasText(chatId)) {
            return Collections.emptyMap();
        }
        String token = getContactAccessToken(orgId);
        if (token == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> detail = wecomApiClient.getGroupChat(token, chatId.trim());
        return detail != null ? detail : Collections.emptyMap();
    }

    @Override
    public List<String> listFollowUsers() {
        String token = getContactAccessToken();
        return token == null ? Collections.emptyList() : wecomApiClient.getFollowUserList(token);
    }

    @Override
    public List<Map<String, Object>> listKfAccounts() {
        String token = getAccessToken();
        if (token != null) {
            List<Map<String, Object>> list = wecomApiClient.listKfAccounts(token);
            if (!list.isEmpty()) {
                return list;
            }
        }
        String contactToken = getContactAccessToken();
        return contactToken == null ? Collections.emptyList() : wecomApiClient.listKfAccounts(contactToken);
    }

    @Override
    public List<Map<String, Object>> listKfServicers(String openKfid) {
        String token = resolveKfAccessToken();
        return token == null ? Collections.emptyList() : wecomApiClient.listKfServicers(token, openKfid);
    }

    @Override
    public List<Map<String, Object>> listKfServicersBatch(List<String> openKfidList) {
        if (openKfidList == null || openKfidList.isEmpty()) {
            return Collections.emptyList();
        }
        String token = resolveKfAccessToken();
        if (token == null) {
            return Collections.emptyList();
        }
        List<String> ids = openKfidList.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> all = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> futures = new ArrayList<>(ids.size());
        for (String openKfid : ids) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (Map<String, Object> row : wecomApiClient.listKfServicers(token, openKfid)) {
                    Map<String, Object> item = new HashMap<>(row);
                    item.put("open_kfid", openKfid);
                    all.add(item);
                }
            }, GROUP_CHAT_FETCH_EXECUTOR));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return new ArrayList<>(all);
    }

    private String resolveKfAccessToken() {
        String token = getAccessToken();
        if (StringUtils.hasText(token)) {
            return token;
        }
        return getContactAccessToken();
    }

    @Override
    public List<String> listExternalContactIds(String userid) {
        String token = getContactAccessToken();
        if (token == null) {
            return Collections.emptyList();
        }
        return wecomApiClient.listExternalContactIds(token, userid);
    }

    @Override
    public Map<String, Object> getExternalContact(String externalUserid) {
        String token = getContactAccessToken();
        if (token == null) {
            return Collections.emptyMap();
        }
        return wecomApiClient.getExternalContact(token, externalUserid);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> batchListExternalContacts(List<String> useridList) {
        String token = getContactAccessToken();
        if (token == null || useridList == null || useridList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> all = new ArrayList<>();
        int memberBatch = 100;
        for (int i = 0; i < useridList.size(); i += memberBatch) {
            List<String> slice = useridList.subList(i, Math.min(i + memberBatch, useridList.size()));
            String cursor = "";
            int pageGuard = 0;
            while (pageGuard++ < 500) {
                Map<String, Object> page = wecomApiClient.batchGetExternalContactByUser(token, slice, cursor, 100);
                if (page == null || !isWecomResponseOk(page)) {
                    log.warn("batch/get_by_user 失败: {}", page);
                    break;
                }
                Object list = page.get("external_contact_list");
                if (list instanceof List<?> items) {
                    for (Object item : items) {
                        if (item instanceof Map<?, ?> row) {
                            all.add((Map<String, Object>) row);
                        }
                    }
                }
                Object next = page.get("next_cursor");
                cursor = next == null ? "" : next.toString().trim();
                if (!StringUtils.hasText(cursor)) {
                    break;
                }
            }
        }
        return all;
    }

    private boolean isWecomResponseOk(Map<String, Object> result) {
        if (result == null) {
            return false;
        }
        Object code = result.get("errcode");
        if (code instanceof Number n) {
            return n.intValue() == 0;
        }
        return "0".equals(String.valueOf(code));
    }

    @Override
    public Map<String, Object> addContactWay(Map<String, Object> payload) {
        String token = getContactAccessToken();
        if (token == null) {
            return Map.of("errcode", -1, "errmsg", "企业微信客户联系未配置");
        }
        return wecomApiClient.addContactWay(token, payload);
    }
}
