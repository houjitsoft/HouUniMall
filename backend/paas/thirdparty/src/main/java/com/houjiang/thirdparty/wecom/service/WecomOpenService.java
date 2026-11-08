package com.houjiang.thirdparty.wecom.service;

import com.houjiang.thirdparty.wecom.client.WecomApiResult;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
public interface WecomOpenService {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    String getAccessToken();

    /**
     * TODO 补充方法说明
     * @param departmentId TODO
     * @return TODO
     */
    List<Map<String, Object>> listDepartments(Integer departmentId);

    /** 拉取部门（含企微 errcode/errmsg） */
    /**
     * TODO 补充方法说明
     * @param departmentId TODO
     * @return TODO
     */
    WecomApiResult<List<Map<String, Object>>> fetchDepartments(Integer departmentId);

    /**
     * TODO 补充方法说明
     * @param orgId TODO
     * @param departmentId TODO
     * @return TODO
     */
    WecomApiResult<List<Map<String, Object>>> fetchDepartments(String orgId, Integer departmentId);

    /**
     * TODO 补充方法说明
     * @param departmentId TODO
     * @return TODO
     */
    List<Map<String, Object>> listUsers(int departmentId);

    /** 拉取部门成员（含企微 errcode/errmsg） */
    /**
     * TODO 补充方法说明
     * @param departmentId TODO
     * @return TODO
     */
    WecomApiResult<List<Map<String, Object>>> fetchUsers(int departmentId);

    /**
     * TODO 补充方法说明
     * @param orgId TODO
     * @param departmentId TODO
     * @return TODO
     */
    WecomApiResult<List<Map<String, Object>>> fetchUsers(String orgId, int departmentId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<Map<String, Object>> listCorpTags();

    /** 拉取企业标签（含企微 errcode/errmsg） */
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    WecomApiResult<List<Map<String, Object>>> fetchCorpTags();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<Map<String, Object>> listGroupChats();

    /** 按组织与群主拉取客户群（含群名、成员数） */
    /**
     * TODO 补充方法说明
     * @param orgId TODO
     * @param ownerUserid TODO
     * @return TODO
     */
    List<Map<String, Object>> listGroupChatsByOwner(String orgId, String ownerUserid);

    /**
     * 按组织与企微 userid 拉取客户群（群主或成员），含诊断 hint
     */
    Map<String, Object> queryGroupChatsByOwner(String orgId, String ownerUserid);

    /** {@code refresh=true} 时跳过短期缓存，向企微重新拉取 */
    /**
     * TODO 补充方法说明
     * @param orgId TODO
     * @param ownerUserid TODO
     * @param refresh TODO
     * @return TODO
     */
    Map<String, Object> queryGroupChatsByOwner(String orgId, String ownerUserid, boolean refresh);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<String> listFollowUsers();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<Map<String, Object>> listKfAccounts();

    /**
     * TODO 补充方法说明
     * @param openKfid TODO
     * @return TODO
     */
    List<Map<String, Object>> listKfServicers(String openKfid);

    /** 按多个客服账号 open_kfid 拉取接待人员（在 thirdparty 内循环，减少 OA 多次 Feign） */
    /**
     * TODO 补充方法说明
     * @param openKfidList TODO
     * @return TODO
     */
    List<Map<String, Object>> listKfServicersBatch(List<String> openKfidList);

    /** 客户联系 access_token（contactSecret 优先） */
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    String getContactAccessToken();

    /** 按组织获取客户联系 access_token */
    /**
     * TODO 补充方法说明
     * @param orgId TODO
     * @return TODO
     */
    String getContactAccessToken(String orgId);

    /**
     * TODO 补充方法说明
     * @param userid TODO
     * @return TODO
     */
    List<String> listExternalContactIds(String userid);

    /**
     * TODO 补充方法说明
     * @param externalUserid TODO
     * @return TODO
     */
    Map<String, Object> getExternalContact(String externalUserid);

    /** 批量拉取客户详情（按成员 userid 列表，内部自动分页） */
    /**
     * TODO 补充方法说明
     * @param useridList TODO
     * @return TODO
     */
    List<Map<String, Object>> batchListExternalContacts(List<String> useridList);

    /**
     * TODO 补充方法说明
     * @param payload TODO
     * @return TODO
     */
    Map<String, Object> addContactWay(Map<String, Object> payload);

    /** 获取单个客户群详情（含 owner、member_list） */
    /**
     * TODO 补充方法说明
     * @param orgId TODO
     * @param chatId TODO
     * @return TODO
     */
    Map<String, Object> getGroupChatDetail(String orgId, String chatId);
}
