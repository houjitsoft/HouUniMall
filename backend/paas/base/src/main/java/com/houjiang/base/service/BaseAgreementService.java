package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.entity.BaseAgreementEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * 协议定义与版本管理Service。
 *
 * <p>协议正文按版本存储；注册页通过 {@link #listByScene} 一次性拿到
 * 「协议定义 + 当前生效版本正文」。</p>
 *
 * @author Amos
 */
public interface BaseAgreementService extends IService<BaseAgreementEntity> {

    /**
     * 按场景查询启用中的协议及其当前生效版本（注册页/登录页使用，无需登录态）。
     *
     * @param param scene=场景（默认 register）
     * @return 协议定义 + 生效版本正文的列表
     */
    ResponseResult listByScene(Map<String, Object> param);

    /**
     * 按协议编码查询当前生效版本（供 BFF 做注册前必选协议校验）。
     *
     * @param param codes=协议编码集合（逗号分隔），scene=场景
     * @return 编码 -> 生效版本 的映射
     */
    ResponseResult latestVersions(Map<String, Object> param);

    /**
     * 后台：协议定义分页
     *
     * @param param page/limit/code/name/scene/status
     * @return 分页结果
     */
    ResponseResult adminPage(Map<String, Object> param);

    /**
     * 后台：新增或更新协议定义
     *
     * @param param 协议定义字段
     * @return 操作结果
     */
    ResponseResult adminSave(Map<String, Object> param);

    /**
     * 后台：删除协议定义（连同其全部版本，历史同意记录保留）
     *
     * @param param id
     * @return 操作结果
     */
    ResponseResult adminDelete(Map<String, Object> param);

    /**
     * 后台：查询某协议的版本列表
     *
     * @param param agreementId 或 code
     * @return 版本列表（生效在前）
     */
    ResponseResult versionList(Map<String, Object> param);

    /**
     * 后台：新增或更新协议版本（保存后为草稿，需发布才生效）
     *
     * @param param 版本字段
     * @return 操作结果
     */
    ResponseResult versionSave(Map<String, Object> param);

    /**
     * 后台：发布协议版本（发布后未同意该版本号的用户需重新同意）
     *
     * @param param id
     * @return 操作结果
     */
    ResponseResult versionPublish(Map<String, Object> param);

    /**
     * 后台：下线协议版本
     *
     * @param param id
     * @return 操作结果
     */
    ResponseResult versionOffline(Map<String, Object> param);

    /**
     * 后台：删除协议版本（已发布版本须先下线）
     *
     * @param param id
     * @return 操作结果
     */
    ResponseResult versionDelete(Map<String, Object> param);

    /**
     * 写入站点开源 EULA 接受标记（accepted_eula_version / accepted_eula_time）。
     *
     * @param param acceptedEulaVersion、acceptedEulaTime
     * @return 操作结果
     */
    ResponseResult persistSiteEula(Map<String, Object> param);
}
