package com.houjiang.commons.errorcode;

import java.util.Arrays;

/**
 * 错误码字典的前端子集（脱敏后）。
 *
 * <p>解决的问题：字典接口若把 devMessage / troubleshooting / ownerModule 一并返回，
 * 会把排障指引、内部模块名、开发态文案暴露到浏览器（PRD §4 X4）。
 *
 * <p>本 DTO 只保留前端渲染需要的 9 个字段：
 * code / name / userMessage / level / retryable / domain / uiAction / category / exposeToUser / legacyCodes。
 *
 * @author Amos
 * @since v1.2.0
 */
public class ErrorCodeFrontendItem {

    private int code;
    private String name;
    private String userMessage;
    private String level;
    private boolean retryable;
    private String domain;
    private String uiAction;
    private String category;
    private boolean exposeToUser;
    private int[] legacyCodes;

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
     * @return 是否允许直出 userMessage
     */
    public boolean isExposeToUser() {
        return exposeToUser;
    }

    /**
     * @param exposeToUser 是否允许直出 userMessage
     */
    public void setExposeToUser(boolean exposeToUser) {
        this.exposeToUser = exposeToUser;
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
}
