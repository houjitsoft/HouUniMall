package com.houjiang.thirdparty.wecom.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 企业微信 API 客户端
 * @author Amos
 */
@Slf4j
@Component
public class WecomApiClient {

    private static final String GET_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    private static final String DEPARTMENT_LIST_URL = "https://qyapi.weixin.qq.com/cgi-bin/department/list";
    private static final String USER_LIST_URL = "https://qyapi.weixin.qq.com/cgi-bin/user/list";
    private static final String CHECKIN_DATA_URL = "https://qyapi.weixin.qq.com/cgi-bin/checkin/getcheckindata";
    private static final String EXTERNAL_CONTACT_LIST_URL = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/list";
    private static final String EXTERNAL_CONTACT_GET_URL = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/get";
    private static final String EXTERNAL_CONTACT_BATCH_BY_USER_URL =
            "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/batch/get_by_user";
    private static final String CORP_TAG_LIST_URL = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/get_corp_tag_list";
    private static final String GROUP_CHAT_LIST_URL = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/groupchat/list";
    private static final String GROUP_CHAT_GET_URL = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/groupchat/get";
    private static final String KF_ACCOUNT_LIST_URL = "https://qyapi.weixin.qq.com/cgi-bin/kf/account/list";
    private static final String KF_SERVICER_LIST_URL = "https://qyapi.weixin.qq.com/cgi-bin/kf/servicer/list";
    private static final String ADD_CONTACT_WAY_URL = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/add_contact_way";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAccessToken(String corpId, String secret) {
        if (!StringUtils.hasText(corpId) || !StringUtils.hasText(secret)) {
            return null;
        }
        try {
            String url = GET_TOKEN_URL + "?corpid=" + corpId + "&corpsecret=" + secret;
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(body, new TypeReference<>() {});
                Object errcode = result.get("errcode");
                if (errcode instanceof Number && ((Number) errcode).intValue() == 0) {
                    return (String) result.get("access_token");
                }
                log.warn("企业微信 gettoken 失败: {}", body);
            }
        } catch (Exception e) {
            log.error("企业微信 gettoken 异常: {}", e.getMessage(), e);
        }
        return null;
    }

    public boolean testConnection(String corpId, String secret) {
        return StringUtils.hasText(getAccessToken(corpId, secret));
    }

    /**
     * 获取部门列表
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listDepartments(String accessToken, Integer departmentId) {
        WecomApiResult<List<Map<String, Object>>> detailed = listDepartmentsDetailed(accessToken, departmentId);
        return detailed.isOk() && detailed.getData() != null ? detailed.getData() : Collections.emptyList();
    }

    /**
     * 获取部门列表（含 errcode/errmsg）
     */
    @SuppressWarnings("unchecked")
    public WecomApiResult<List<Map<String, Object>>> listDepartmentsDetailed(String accessToken, Integer departmentId) {
        if (!StringUtils.hasText(accessToken)) {
            return WecomApiResult.failure("access_token 为空");
        }
        try {
            String url = DEPARTMENT_LIST_URL + "?access_token=" + accessToken;
            if (departmentId != null) {
                url += "&id=" + departmentId;
            }
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(body, new TypeReference<>() {});
                if (!isOk(result)) {
                    log.warn("企业微信 department/list 失败: {}", body);
                    return WecomApiResult.failure(parseErrcode(result), parseErrmsg(result));
                }
                Object dept = result.get("department");
                if (dept instanceof List) {
                    return WecomApiResult.success((List<Map<String, Object>>) dept);
                }
                return WecomApiResult.success(Collections.emptyList());
            }
        } catch (Exception e) {
            log.error("企业微信 department/list 异常: {}", e.getMessage(), e);
            return WecomApiResult.failure("调用 department/list 异常: " + e.getMessage());
        }
    }

    /**
     * 企业标签库
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCorpTagList(String accessToken) {
        WecomApiResult<List<Map<String, Object>>> detailed = getCorpTagListDetailed(accessToken);
        return detailed.isOk() && detailed.getData() != null ? detailed.getData() : Collections.emptyList();
    }

    /**
     * 企业标签库（含 errcode/errmsg，供 SCRM 同步透传失败原因）
     */
    @SuppressWarnings("unchecked")
    public WecomApiResult<List<Map<String, Object>>> getCorpTagListDetailed(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return WecomApiResult.failure("access_token 为空");
        }
        try {
            String url = CORP_TAG_LIST_URL + "?access_token=" + accessToken;
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create("{}", JSON))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(body, new TypeReference<>() {});
                if (!isOk(result)) {
                    log.warn("企业微信 get_corp_tag_list 失败: {}", body);
                    return WecomApiResult.failure(parseErrcode(result), parseErrmsg(result));
                }
                Object tagGroup = result.get("tag_group");
                if (tagGroup instanceof List) {
                    return WecomApiResult.success((List<Map<String, Object>>) tagGroup);
                }
                return WecomApiResult.success(Collections.emptyList());
            }
        } catch (Exception e) {
            log.error("企业微信 get_corp_tag_list 异常: {}", e.getMessage(), e);
            return WecomApiResult.failure("调用 get_corp_tag_list 异常: " + e.getMessage());
        }
    }

    /**
     * 客户群列表（可按群主 userid 过滤），返回列表与下一页游标（含 errcode/errmsg）
     */
    @SuppressWarnings("unchecked")
    public WecomApiResult<Map<String, Object>> listGroupChatsPageDetailed(String accessToken, int limit, String cursor,
                                                                            List<String> ownerUserids) {
        if (!StringUtils.hasText(accessToken)) {
            return WecomApiResult.failure("access_token 为空");
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("limit", limit);
            payload.put("status_filter", 0);
            if (StringUtils.hasText(cursor)) {
                payload.put("cursor", cursor);
            }
            if (ownerUserids != null && !ownerUserids.isEmpty()) {
                Map<String, Object> ownerFilter = new HashMap<>();
                ownerFilter.put("userid_list", ownerUserids);
                payload.put("owner_filter", ownerFilter);
            }
            String json = objectMapper.writeValueAsString(payload);
            String url = GROUP_CHAT_LIST_URL + "?access_token=" + accessToken;
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(json, JSON))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(body, new TypeReference<>() {});
                if (!isOk(result)) {
                    log.warn("企业微信 groupchat/list 失败: {}", body);
                    return WecomApiResult.failure(parseErrcode(result), parseErrmsg(result));
                }
                Map<String, Object> page = new HashMap<>();
                Object list = result.get("group_chat_list");
                page.put("list", list instanceof List ? list : Collections.emptyList());
                Object nextCursor = result.get("next_cursor");
                page.put("next_cursor", nextCursor != null ? nextCursor.toString() : null);
                return WecomApiResult.success(page);
            }
        } catch (Exception e) {
            log.error("企业微信 groupchat/list 异常: {}", e.getMessage(), e);
            return WecomApiResult.failure("调用 groupchat/list 异常: " + e.getMessage());
        }
    }

    /**
     * 客户群列表（可按群主 userid 过滤），返回列表与下一页游标
     */
    public Map<String, Object> listGroupChatsPage(String accessToken, int limit, String cursor,
                                                    List<String> ownerUserids) {
        Map<String, Object> empty = new HashMap<>();
        empty.put("list", Collections.emptyList());
        empty.put("next_cursor", null);
        WecomApiResult<Map<String, Object>> detailed =
                listGroupChatsPageDetailed(accessToken, limit, cursor, ownerUserids);
        if (!detailed.isOk() || detailed.getData() == null) {
            return empty;
        }
        return detailed.getData();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listGroupChats(String accessToken, int limit, String cursor,
                                                    List<String> ownerUserids) {
        Object list = listGroupChatsPage(accessToken, limit, cursor, ownerUserids).get("list");
        return list instanceof List ? (List<Map<String, Object>>) list : Collections.emptyList();
    }

    public List<Map<String, Object>> listGroupChats(String accessToken, int limit, String cursor) {
        return listGroupChats(accessToken, limit, cursor, null);
    }

    /**
     * 分页拉取客户群 chat_id（最多 maxCount 条，含 errcode/errmsg）
     */
    @SuppressWarnings("unchecked")
    public WecomApiResult<List<Map<String, Object>>> listAllGroupChatIdsDetailed(String accessToken,
                                                                                   List<String> ownerUserids,
                                                                                   int maxCount) {
        if (!StringUtils.hasText(accessToken)) {
            return WecomApiResult.failure("access_token 为空");
        }
        if (maxCount <= 0) {
            return WecomApiResult.success(Collections.emptyList());
        }
        List<Map<String, Object>> all = new ArrayList<>();
        String cursor = null;
        while (all.size() < maxCount) {
            WecomApiResult<Map<String, Object>> pageResult = listGroupChatsPageDetailed(
                    accessToken, Math.min(100, maxCount - all.size()), cursor, ownerUserids);
            if (!pageResult.isOk()) {
                return all.isEmpty()
                        ? WecomApiResult.failure(pageResult.getErrcode(), pageResult.getErrmsg())
                        : WecomApiResult.success(all);
            }
            Map<String, Object> pageData = pageResult.getData();
            List<Map<String, Object>> page = pageData != null && pageData.get("list") instanceof List<?>
                    ? (List<Map<String, Object>>) pageData.get("list") : Collections.emptyList();
            if (page.isEmpty()) {
                break;
            }
            all.addAll(page);
            Object nextCursor = pageData != null ? pageData.get("next_cursor") : null;
            cursor = nextCursor != null ? nextCursor.toString() : null;
            if (!StringUtils.hasText(cursor)) {
                break;
            }
        }
        List<Map<String, Object>> trimmed = all.size() > maxCount ? all.subList(0, maxCount) : all;
        return WecomApiResult.success(trimmed);
    }

    /**
     * 分页拉取客户群 chat_id（最多 maxCount 条）
     */
    public List<Map<String, Object>> listAllGroupChatIds(String accessToken, List<String> ownerUserids, int maxCount) {
        WecomApiResult<List<Map<String, Object>>> detailed =
                listAllGroupChatIdsDetailed(accessToken, ownerUserids, maxCount);
        return detailed.isOk() && detailed.getData() != null ? detailed.getData() : Collections.emptyList();
    }

    /**
     * 客户群详情（含群名、成员数等）
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getGroupChat(String accessToken, String chatId) {
        if (!StringUtils.hasText(accessToken) || !StringUtils.hasText(chatId)) {
            return null;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("chat_id", chatId);
            payload.put("need_name", 1);
            String json = objectMapper.writeValueAsString(payload);
            String url = GROUP_CHAT_GET_URL + "?access_token=" + accessToken;
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(json, JSON))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(body, new TypeReference<>() {});
                if (!isOk(result)) {
                    log.warn("企业微信 groupchat/get 失败 chatId={}: {}", chatId, body);
                    return null;
                }
                Object groupChat = result.get("group_chat");
                if (groupChat instanceof Map) {
                    return (Map<String, Object>) groupChat;
                }
            }
        } catch (Exception e) {
            log.error("企业微信 groupchat/get 异常 chatId={}: {}", chatId, e.getMessage(), e);
        }
        return null;
    }

    /**
     * 配置了客户联系功能的成员
     */
    @SuppressWarnings("unchecked")
    public List<String> getFollowUserList(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return Collections.emptyList();
        }
        try {
            String url = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/get_follow_user_list?access_token=" + accessToken;
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(body, new TypeReference<>() {});
                if (!isOk(result)) {
                    log.warn("企业微信 get_follow_user_list 失败: {}", body);
                    return Collections.emptyList();
                }
                Object list = result.get("follow_user");
                if (list instanceof List) {
                    return (List<String>) list;
                }
            }
        } catch (Exception e) {
            log.error("企业微信 get_follow_user_list 异常: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * 拉取部门成员（含子部门），默认从根部门 1 开始
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listDepartmentUsers(String accessToken, int departmentId) {
        WecomApiResult<List<Map<String, Object>>> detailed = listDepartmentUsersDetailed(accessToken, departmentId);
        return detailed.isOk() && detailed.getData() != null ? detailed.getData() : Collections.emptyList();
    }

    /**
     * 拉取部门成员（含 errcode/errmsg）
     */
    @SuppressWarnings("unchecked")
    public WecomApiResult<List<Map<String, Object>>> listDepartmentUsersDetailed(String accessToken, int departmentId) {
        if (!StringUtils.hasText(accessToken)) {
            return WecomApiResult.failure("access_token 为空");
        }
        try {
            String url = USER_LIST_URL + "?access_token=" + accessToken
                    + "&department_id=" + departmentId + "&fetch_child=1";
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(body, new TypeReference<>() {});
                if (!isOk(result)) {
                    log.warn("企业微信 user/list 失败: {}", body);
                    return WecomApiResult.failure(parseErrcode(result), parseErrmsg(result));
                }
                Object userlist = result.get("userlist");
                if (userlist instanceof List) {
                    return WecomApiResult.success((List<Map<String, Object>>) userlist);
                }
                return WecomApiResult.success(Collections.emptyList());
            }
        } catch (Exception e) {
            log.error("企业微信 user/list 异常: {}", e.getMessage(), e);
            return WecomApiResult.failure("调用 user/list 异常: " + e.getMessage());
        }
    }

    /** 打卡 API 拉取结果（含企微 errcode，便于同步接口向前端提示） */
    public record CheckinDataResult(List<Map<String, Object>> records, Integer errcode, String errmsg) {
        public static CheckinDataResult empty() {
            return new CheckinDataResult(Collections.emptyList(), null, null);
        }
    }

    /**
     * 获取打卡记录（单次最多 100 人）
     */
    @SuppressWarnings("unchecked")
    public CheckinDataResult getCheckinData(String accessToken, long startTimeSec, long endTimeSec,
                                            List<String> userIdList) {
        if (!StringUtils.hasText(accessToken) || userIdList == null || userIdList.isEmpty()) {
            return CheckinDataResult.empty();
        }
        List<Map<String, Object>> all = new ArrayList<>();
        Integer errcode = null;
        String errmsg = null;
        int batch = 100;
        for (int i = 0; i < userIdList.size(); i += batch) {
            List<String> slice = userIdList.subList(i, Math.min(i + batch, userIdList.size()));
            CheckinDataResult part = getCheckinDataBatch(accessToken, startTimeSec, endTimeSec, slice);
            if (part.errcode() != null) {
                errcode = part.errcode();
                errmsg = part.errmsg();
                break;
            }
            if (part.records() != null) {
                all.addAll(part.records());
            }
        }
        return new CheckinDataResult(all, errcode, errmsg);
    }

    @SuppressWarnings("unchecked")
    private CheckinDataResult getCheckinDataBatch(String accessToken, long startTimeSec, long endTimeSec,
                                                  List<String> userIdList) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("opencheckindatatype", 3);
            payload.put("starttime", startTimeSec);
            payload.put("endtime", endTimeSec);
            payload.put("useridlist", userIdList);
            String json = objectMapper.writeValueAsString(payload);
            String url = CHECKIN_DATA_URL + "?access_token=" + accessToken;
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(json, JSON))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(body, new TypeReference<>() {});
                if (!isOk(result)) {
                    log.warn("企业微信 getcheckindata 失败: {}", body);
                    int code = parseErrcode(result);
                    String msg = parseErrmsg(result);
                    return new CheckinDataResult(Collections.emptyList(), code, msg);
                }
                Object data = result.get("checkindata");
                if (data instanceof List) {
                    return new CheckinDataResult((List<Map<String, Object>>) data, null, null);
                }
            }
        } catch (Exception e) {
            log.error("企业微信 getcheckindata 异常: {}", e.getMessage(), e);
        }
        return CheckinDataResult.empty();
    }

    /** 微信客服账号列表 */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listKfAccounts(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return Collections.emptyList();
        }
        try {
            String url = KF_ACCOUNT_LIST_URL + "?access_token=" + accessToken;
            RequestBody body = RequestBody.create("{}", JSON);
            Request request = new Request.Builder().url(url).post(body).build();
            try (Response response = httpClient.newCall(request).execute()) {
                String resp = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(resp, new TypeReference<>() {});
                if (isOk(result)) {
                    Object list = result.get("account_list");
                    if (list instanceof List<?> l) {
                        return (List<Map<String, Object>>) l;
                    }
                }
                log.warn("企微客服账号列表失败: {}", resp);
            }
        } catch (Exception e) {
            log.error("企微客服账号列表异常: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * 某成员的客户 external_userid 列表
     */
    @SuppressWarnings("unchecked")
    public List<String> listExternalContactIds(String accessToken, String userid) {
        if (!StringUtils.hasText(accessToken) || !StringUtils.hasText(userid)) {
            return Collections.emptyList();
        }
        try {
            String url = EXTERNAL_CONTACT_LIST_URL + "?access_token=" + accessToken + "&userid=" + userid;
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(body, new TypeReference<>() {});
                if (!isOk(result)) {
                    log.warn("企业微信 externalcontact/list 失败 userid={}: {}", userid, body);
                    return Collections.emptyList();
                }
                Object list = result.get("external_userid");
                if (list instanceof List<?> l) {
                    List<String> out = new ArrayList<>();
                    for (Object item : l) {
                        if (item != null) {
                            out.add(String.valueOf(item));
                        }
                    }
                    return out;
                }
            }
        } catch (Exception e) {
            log.error("企业微信 externalcontact/list 异常: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * 批量获取客户详情（每批最多 100 个成员 userid，每页最多 100 条客户）
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> batchGetExternalContactByUser(String accessToken, List<String> useridList,
                                                             String cursor, int limit) {
        if (!StringUtils.hasText(accessToken) || useridList == null || useridList.isEmpty()) {
            return Map.of("errcode", -1, "errmsg", "invalid params");
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userid_list", useridList);
            payload.put("cursor", cursor != null ? cursor : "");
            payload.put("limit", limit <= 0 ? 100 : Math.min(limit, 100));
            String json = objectMapper.writeValueAsString(payload);
            String url = EXTERNAL_CONTACT_BATCH_BY_USER_URL + "?access_token=" + accessToken;
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(json, JSON))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                return objectMapper.readValue(body, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("企业微信 externalcontact/batch/get_by_user 异常: {}", e.getMessage(), e);
            return Map.of("errcode", -1, "errmsg", e.getMessage());
        }
    }

    /**
     * 客户详情（含跟进人）
     */
    public Map<String, Object> getExternalContact(String accessToken, String externalUserid) {
        if (!StringUtils.hasText(accessToken) || !StringUtils.hasText(externalUserid)) {
            return Collections.emptyMap();
        }
        try {
            String url = EXTERNAL_CONTACT_GET_URL + "?access_token=" + accessToken
                    + "&external_userid=" + externalUserid;
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(body, new TypeReference<>() {});
                if (!isOk(result)) {
                    log.warn("企业微信 externalcontact/get 失败: {}", body);
                    return Collections.emptyMap();
                }
                return result;
            }
        } catch (Exception e) {
            log.error("企业微信 externalcontact/get 异常: {}", e.getMessage(), e);
        }
        return Collections.emptyMap();
    }

    /**
     * 创建「联系我」渠道活码
     */
    public Map<String, Object> addContactWay(String accessToken, Map<String, Object> payload) {
        if (!StringUtils.hasText(accessToken) || payload == null || payload.isEmpty()) {
            return Map.of("errcode", -1, "errmsg", "invalid params");
        }
        try {
            String json = objectMapper.writeValueAsString(payload);
            String url = ADD_CONTACT_WAY_URL + "?access_token=" + accessToken;
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(json, JSON))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                return objectMapper.readValue(body, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("企业微信 add_contact_way 异常: {}", e.getMessage(), e);
            return Map.of("errcode", -1, "errmsg", e.getMessage());
        }
    }

    /** 微信客服接待人员 */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listKfServicers(String accessToken, String openKfid) {
        if (!StringUtils.hasText(accessToken) || !StringUtils.hasText(openKfid)) {
            return Collections.emptyList();
        }
        try {
            String url = KF_SERVICER_LIST_URL + "?access_token=" + accessToken;
            Map<String, Object> payload = Map.of("open_kfid", openKfid);
            RequestBody body = RequestBody.create(objectMapper.writeValueAsString(payload), JSON);
            Request request = new Request.Builder().url(url).post(body).build();
            try (Response response = httpClient.newCall(request).execute()) {
                String resp = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(resp, new TypeReference<>() {});
                if (isOk(result)) {
                    Object list = result.get("servicer_list");
                    if (list instanceof List<?> l) {
                        List<Map<String, Object>> out = new ArrayList<>();
                        for (Object item : l) {
                            if (item instanceof Map<?, ?> m) {
                                Map<String, Object> row = new HashMap<>();
                                Object uid = m.get("userid");
                                row.put("userid", uid);
                                row.put("name", uid);
                                out.add(row);
                            }
                        }
                        return out;
                    }
                }
                log.warn("企微客服接待人员失败: {}", resp);
            }
        } catch (Exception e) {
            log.error("企微客服接待人员异常: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private boolean isOk(Map<String, Object> result) {
        Object errcode = result.get("errcode");
        return errcode instanceof Number && ((Number) errcode).intValue() == 0;
    }

    private static int parseErrcode(Map<String, Object> result) {
        Object errcode = result.get("errcode");
        return errcode instanceof Number ? ((Number) errcode).intValue() : -1;
    }

    private static String parseErrmsg(Map<String, Object> result) {
        Object errmsg = result.get("errmsg");
        return errmsg != null ? String.valueOf(errmsg) : "";
    }
}
