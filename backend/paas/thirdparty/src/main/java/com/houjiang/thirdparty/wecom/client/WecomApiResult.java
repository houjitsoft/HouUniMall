package com.houjiang.thirdparty.wecom.client;

import lombok.Getter;

/**
 * 企业微信 API 调用结果（含 errcode/errmsg，便于向上层透传真实失败原因）
 * @author Amos
 */
@Getter
public class WecomApiResult<T> {

    private final boolean ok;
    private final int errcode;
    private final String errmsg;
    private final T data;

    private WecomApiResult(boolean ok, int errcode, String errmsg, T data) {
        this.ok = ok;
        this.errcode = errcode;
        this.errmsg = errmsg;
        this.data = data;
    }

    public static <T> WecomApiResult<T> success(T data) {
        return new WecomApiResult<>(true, 0, "ok", data);
    }

    public static <T> WecomApiResult<T> failure(int errcode, String errmsg) {
        return new WecomApiResult<>(false, errcode, errmsg, null);
    }

    public static <T> WecomApiResult<T> failure(String errmsg) {
        return failure(-1, errmsg);
    }

    /**
     * 转为面向运维/前端的可读提示（含常见 errcode 说明）
     */
    public String toUserMessage() {
        if (ok) {
            return "ok";
        }
        if (errcode == 60020) {
            String rejectedIp = extractRejectedIp(errmsg);
            String ipHint = rejectedIp != null
                ? "企微检测到的出口 IP 为 " + rejectedIp + "，请加入白名单"
                : "请将服务器公网出口 IP 加入白名单";
            return "企业微信拒绝访问（errcode=60020 IP 不在可信名单），" + ipHint
                + "。配置路径：应用管理 → 对应自建应用 → 企业可信IP（通讯录/员工同步用 Secret 所在应用）"
                + (errmsg != null && !errmsg.isBlank() ? "。原始信息：" + errmsg : "");
        }
        if (errcode == 48009) {
            return "企业微信接口无权限（errcode=48009）：当前 Secret 属于「客户联系助手」，不能调用通讯录 user/list。"
                + "请将「通讯录 Secret」清空或改为主自建应用 Secret（与 Secret 字段相同），客户联系 Secret 只填在「客户联系 Secret」栏。";
        }
        if (errcode == 48002) {
            return "企业微信 API 无权限（errcode=48002）：请到企微「客户联系→客户→可调用接口的应用→修改」，"
                + "将 HouCloud 使用的自建应用加入授权列表，Secret 使用该应用在「应用管理」中的 Secret";
        }
        if (errcode == 40014 || errcode == 40001) {
            return "企业微信 access_token 无效（errcode=" + errcode + "），请检查 CorpId / Secret / 客户联系 Secret 是否正确";
        }
        if (errmsg != null && !errmsg.isEmpty()) {
            return "企业微信接口失败（errcode=" + errcode + "）：" + errmsg;
        }
        return "企业微信接口失败（errcode=" + errcode + "）";
    }

    /** 从 errmsg 解析企微返回的被拒 IP，例如 from ip: 139.226.155.199 */
    private static String extractRejectedIp(String errmsg) {
        if (errmsg == null || errmsg.isBlank()) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("from ip:\\s*([0-9a-fA-F:.]+)", java.util.regex.Pattern.CASE_INSENSITIVE)
            .matcher(errmsg);
        return m.find() ? m.group(1).trim() : null;
    }
}
