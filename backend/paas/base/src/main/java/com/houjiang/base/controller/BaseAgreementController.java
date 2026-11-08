package com.houjiang.base.controller;

import com.houjiang.base.service.BaseAgreementService;
import com.houjiang.commons.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 协议定义与版本管理Controller（注册协议可配置化）。
 *
 * <p>命名空间 {@code /base/agreement}，与入驻协议 {@code /base/protocol} 区分。</p>
 *
 * @author Amos
 */
@RestController
@RequestMapping("base/agreement")
@RequiredArgsConstructor
public class BaseAgreementController {

    private final BaseAgreementService baseAgreementService;

    // ================= 注册/登录侧（免登录） =================

    /**
     * 按场景查询启用中的协议及其当前生效版本正文
     */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody(required = false) Map<String, Object> param) {
        return baseAgreementService.listByScene(param);
    }

    /**
     * 按协议编码批量查询当前生效版本（供 BFF 校验用）
     */
    @PostMapping("/latestVersions")
    public ResponseResult latestVersions(@RequestBody(required = false) Map<String, Object> param) {
        return baseAgreementService.latestVersions(param);
    }

    // ================= 后台：协议定义 =================

    /**
     * 协议定义分页
     */
    @PostMapping("/admin/page")
    public ResponseResult adminPage(@RequestBody(required = false) Map<String, Object> param) {
        return baseAgreementService.adminPage(param);
    }

    /**
     * 新增或更新协议定义
     */
    @PostMapping("/admin/save")
    public ResponseResult adminSave(@RequestBody Map<String, Object> param) {
        return baseAgreementService.adminSave(param);
    }

    /**
     * 删除协议定义（连同全部版本）
     */
    @PostMapping("/admin/delete")
    public ResponseResult adminDelete(@RequestBody Map<String, Object> param) {
        return baseAgreementService.adminDelete(param);
    }

    // ================= 后台：协议版本 =================

    /**
     * 查询某协议的版本列表
     */
    @PostMapping("/version/list")
    public ResponseResult versionList(@RequestBody(required = false) Map<String, Object> param) {
        return baseAgreementService.versionList(param);
    }

    /**
     * 新增或更新协议版本（草稿）
     */
    @PostMapping("/version/save")
    public ResponseResult versionSave(@RequestBody Map<String, Object> param) {
        return baseAgreementService.versionSave(param);
    }

    /**
     * 发布协议版本
     */
    @PostMapping("/version/publish")
    public ResponseResult versionPublish(@RequestBody Map<String, Object> param) {
        return baseAgreementService.versionPublish(param);
    }

    /**
     * 下线协议版本
     */
    @PostMapping("/version/offline")
    public ResponseResult versionOffline(@RequestBody Map<String, Object> param) {
        return baseAgreementService.versionOffline(param);
    }

    /**
     * 删除协议版本（已发布版本须先下线）
     */
    @PostMapping("/version/delete")
    public ResponseResult versionDelete(@RequestBody Map<String, Object> param) {
        return baseAgreementService.versionDelete(param);
    }

    /**
     * 写入站点开源 EULA 接受标记（accepted_eula_version / accepted_eula_time）
     */
    @PostMapping("/eula/persistSite")
    public ResponseResult persistSiteEula(@RequestBody Map<String, Object> param) {
        return baseAgreementService.persistSiteEula(param);
    }
}
