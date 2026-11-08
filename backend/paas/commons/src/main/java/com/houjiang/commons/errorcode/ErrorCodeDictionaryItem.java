package com.houjiang.commons.errorcode;

import java.util.Arrays;

/**
 * 错误码字典导出项（全量字段，供管理端 / 排障 / 手册生成器使用）。
 *
 * <p>解决的问题（PRD P0-4）：错误码必须可枚举、可导出。
 * 本 DTO 由 {@link ErrorCodeDef} 一行转换而来，与运行时枚举同源，禁止手写第二份。
 *
 * <p><b>注意</b>：本 DTO 含 devMessage / troubleshooting / ownerModule 等内部信息，
 * 只允许在内网管理端与手册中出现；对外（前端映射表、公网接口）必须使用
 * {@link ErrorCodeFrontendItem}（{@link #toFrontend()} 做脱敏裁剪）。
 *
 * @author Amos
 * @since v1.2.0
 */
public class ErrorCodeDictionaryItem {

    private int code;
    private String name;
    private String userMessage;
    private String devMessage;
    private String level;
    private boolean retryable;
    private String domain;
    private int domainId;
    private String ownerModule;
    private String uiAction;
    private String category;
    private String status;
    private String httpSemantic;
    private boolean exposeToUser;
    private String troubleshooting;
    private int[] legacyCodes;
    private String since;
    private String remark;

    /**
     * 由错误码定义转换为字典项。
     *
     * @param def 错误码定义
     * @return 字典项
     */
    public static ErrorCodeDictionaryItem from(ErrorCodeDef def) {
        ErrorMeta meta = def.meta();
        ErrorDomain domain = def.domain();
        ErrorCodeDictionaryItem item = new ErrorCodeDictionaryItem();
        item.code = def.code();
        item.name = def.name();
        item.userMessage = meta.userMessage();
        item.devMessage = meta.devMessage();
        item.level = meta.level().name();
        item.retryable = meta.retryable();
        item.domain = domain.code();
        item.domainId = domain.domainId();
        item.ownerModule = domain.ownerModule();
        item.uiAction = meta.uiAction().name();
        item.category = meta.category().name();
        item.status = meta.status().name();
        item.httpSemantic = meta.httpSemantic().name();
        item.exposeToUser = meta.exposeToUser();
        item.troubleshooting = meta.troubleshooting();
        item.legacyCodes = meta.legacyCodes();
        item.since = meta.since();
        item.remark = meta.remark();
        return item;
    }

    /**
     * 裁剪为前端可用子集：去掉 devMessage / troubleshooting / ownerModule / domainId 等内部字段。
     *
     * <p>解决的问题：错误码字典若全量外泄，会把排障指引、内部模块名、开发态文案暴露出去。
     *
     * @return 前端项
     */
    public ErrorCodeFrontendItem toFrontend() {
        ErrorCodeFrontendItem item = new ErrorCodeFrontendItem();
        item.setCode(this.code);
        item.setName(this.name);
        item.setUserMessage(this.userMessage);
        item.setLevel(this.level);
        item.setRetryable(this.retryable);
        item.setDomain(this.domain);
        item.setUiAction(this.uiAction);
        item.setCategory(this.category);
        item.setExposeToUser(this.exposeToUser);
        item.setLegacyCodes(this.legacyCodes == null
                ? new int[0] : Arrays.copyOf(this.legacyCodes, this.legacyCodes.length));
        return item;
    }

    /**
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code 错误码
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return 英文常量名
     */
    public String getName() {
        return name;
    }

    /**
     * @param name 英文常量名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 用户文案
     */
    public String getUserMessage() {
        return userMessage;
    }

    /**
     * @param userMessage 用户文案
     */
    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    /**
     * @return 开发/排障文案
     */
    public String getDevMessage() {
        return devMessage;
    }

    /**
     * @param devMessage 开发/排障文案
     */
    public void setDevMessage(String devMessage) {
        this.devMessage = devMessage;
    }

    /**
     * @return 级别
     */
    public String getLevel() {
        return level;
    }

    /**
     * @param level 级别
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * @return 是否可重试
     */
    public boolean isRetryable() {
        return retryable;
    }

    /**
     * @param retryable 是否可重试
     */
    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }

    /**
     * @return 域代号
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @param domain 域代号
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * @return 域号
     */
    public int getDomainId() {
        return domainId;
    }

    /**
     * @param domainId 域号
     */
    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    /**
     * @return 归属模块
     */
    public String getOwnerModule() {
        return ownerModule;
    }

    /**
     * @param ownerModule 归属模块
     */
    public void setOwnerModule(String ownerModule) {
        this.ownerModule = ownerModule;
    }

    /**
     * @return 展示动作
     */
    public String getUiAction() {
        return uiAction;
    }

    /**
     * @param uiAction 展示动作
     */
    public void setUiAction(String uiAction) {
        this.uiAction = uiAction;
    }

    /**
     * @return 类别
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category 类别
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status 状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return HTTP 语义
     */
    public String getHttpSemantic() {
        return httpSemantic;
    }

    /**
     * @param httpSemantic HTTP 语义
     */
    public void setHttpSemantic(String httpSemantic) {
        this.httpSemantic = httpSemantic;
    }

    /**
     * @return 是否允许前端直出
     */
    public boolean isExposeToUser() {
        return exposeToUser;
    }

    /**
     * @param exposeToUser 是否允许前端直出
     */
    public void setExposeToUser(boolean exposeToUser) {
        this.exposeToUser = exposeToUser;
    }

    /**
     * @return 排查指引
     */
    public String getTroubleshooting() {
        return troubleshooting;
    }

    /**
     * @param troubleshooting 排查指引
     */
    public void setTroubleshooting(String troubleshooting) {
        this.troubleshooting = troubleshooting;
    }

    /**
     * @return 旧码列表
     */
    public int[] getLegacyCodes() {
        return legacyCodes == null ? new int[0] : Arrays.copyOf(legacyCodes, legacyCodes.length);
    }

    /**
     * @param legacyCodes 旧码列表
     */
    public void setLegacyCodes(int[] legacyCodes) {
        this.legacyCodes = legacyCodes;
    }

    /**
     * @return 引入版本
     */
    public String getSince() {
        return since;
    }

    /**
     * @param since 引入版本
     */
    public void setSince(String since) {
        this.since = since;
    }

    /**
     * @return 备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }
}
