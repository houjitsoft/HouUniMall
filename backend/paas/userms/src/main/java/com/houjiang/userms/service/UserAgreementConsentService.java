package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserAgreementConsentEntity;

import java.util.List;
import java.util.Map;

/**
 * 用户协议同意记录Service。
 *
 * @author Amos
 */
public interface UserAgreementConsentService extends IService<UserAgreementConsentEntity> {

    /**
     * 批量记录同意行为（注册 / 版本更新后重新同意）。
     *
     * <p>同一用户对同一协议的旧版本记录会被置为失效（status=0）并保留，
     * 目标版本记录不存在则新增、已存在则刷新同意时间与 IP/UA（幂等）。</p>
     *
     * @param userId   用户ID
     * @param username 用户名（冗余检索用）
     * @param consents 协议条目：code / agreementId / agreementName / versionId / versionNo
     * @param ip       客户端IP
     * @param ua       User-Agent 原文
     * @param source   来源端
     * @param scene    同意场景
     * @return 成功写入/刷新的记录条数
     */
    int recordConsents(String userId, String username, List<Map<String, Object>> consents,
                       String ip, String ua, String source, String scene);

    /**
     * 查询某用户的同意记录（默认只返回有效记录）
     *
     * @param param userId / includeInvalid=true 时含失效记录
     * @return 记录列表
     */
    ResponseResult listByUser(Map<String, Object> param);

    /**
     * 后台：同意记录分页查询
     *
     * @param param page/limit/userId/username/agreementCode/beginTime/endTime/status
     * @return 分页结果
     */
    ResponseResult adminPage(Map<String, Object> param);

    /**
     * 后台：同意记录导出（不分页，返回筛选后的全量数据，上限 20000 条）
     *
     * @param param 同分页查询条件
     * @return 记录列表
     */
    ResponseResult adminExport(Map<String, Object> param);
}
