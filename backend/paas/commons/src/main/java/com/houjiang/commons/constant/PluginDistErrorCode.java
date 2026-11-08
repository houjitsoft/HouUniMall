package com.houjiang.commons.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 插件分发链路统一错误码（3001-3018）。
 *
 * <p>来源：《线上插件目录与分发方案》§4.5。发布、目录、分发、客户端升级四组接口
 * 统一使用本套错误码，配合 {@code ResponseResult{code, data, message}} 返回，
 * 前端按 code 分支处理，禁止依赖 message 文案做判断。
 *
 * <p>命名与常量名一一对应，便于上层直接引用（如
 * {@code ResponseResult.fail(PluginDistErrorCode.NOT_PURCHASED, null)}）。
 *
 * @author Amos
 * @since M0
 */
public final class PluginDistErrorCode {

    /**
     * 未登录。
     */
    public static final int NOT_LOGIN = 3001;

    /**
     * 未购买该插件（无有效订单）。
     */
    public static final int NOT_PURCHASED = 3002;

    /**
     * License 已过期。
     */
    public static final int LICENSE_EXPIRED = 3003;

    /**
     * License 已被吊销（退款 / 违规 / 紧急下架联动）。
     */
    public static final int LICENSE_REVOKED = 3004;

    /**
     * 插件或该版本已下架（offline）。
     */
    public static final int PLUGIN_OR_VERSION_OFFLINE = 3005;

    /**
     * 版本未发布（草稿 / 待审 / 审核驳回），不可下载安装。
     */
    public static final int VERSION_NOT_PUBLISHED = 3006;

    /**
     * 下载令牌已过期。
     */
    public static final int DOWNLOAD_TOKEN_EXPIRED = 3007;

    /**
     * 下载令牌与租户（instanceId）不匹配，疑似越权转发。
     */
    public static final int TOKEN_TENANT_MISMATCH = 3008;

    /**
     * 制品不存在（存储键或制品记录缺失）。
     */
    public static final int ARTIFACT_NOT_FOUND = 3009;

    /**
     * 校验和不匹配（md5 / sha256 与实际制品不一致）。
     */
    public static final int CHECKSUM_MISMATCH = 3010;

    /**
     * 制品签名无效或 kid 未知（含签名与版本不匹配的张冠李戴场景）。
     */
    public static final int SIGNATURE_INVALID = 3011;

    /**
     * 版本冲突（同版本已存在 / 版本号未递增）。
     */
    public static final int VERSION_CONFLICT = 3012;

    /**
     * 清单非法（plugin_code 与包名不一致、必填项缺失等）。
     */
    public static final int MANIFEST_INVALID = 3013;

    /**
     * 框架版本不兼容（宿主框架版本 &lt; min_framework_version）。
     */
    public static final int INCOMPATIBLE_FRAMEWORK = 3014;

    /**
     * 依赖插件缺失（依赖声明的插件未安装或版本不满足）。
     */
    public static final int DEPENDENCY_MISSING = 3015;

    /**
     * 已有安装/升级/回滚任务在进行中（并发互斥）。
     */
    public static final int TASK_IN_PROGRESS = 3016;

    /**
     * 制品体积超限（&gt; 64MB）。
     */
    public static final int SIZE_EXCEEDED = 3017;

    /**
     * 回滚点缺失（找不到可回滚的备份版本目录）。
     */
    public static final int ROLLBACK_POINT_MISSING = 3018;

    /**
     * 依赖成环（A 依赖 B、B 又依赖 A，或更长的回环）。
     *
     * <p>成环的依赖图没有合法的安装顺序——无论先装哪个都会撞上「依赖未安装」。
     * 与其让用户反复试错，不如在预检阶段直接指出环上的完整链路。
     */
    public static final int DEPENDENCY_CYCLE = 3019;

    /**
     * 插件宿主不匹配：制品声明的 hostService 不是本服务，跨宿主装载被 fail-closed 拒绝。
     *
     * <p>典型场景：在 base 的插件中心对 {@code hostService: goods} 的插件调 /install。
     * 该插件必须由 goods 宿主装载（制品投放 goods 的 plugins 目录后由宿主引擎扫描），
     * base 的 install 流水线拒绝把插件装进错误进程——这里给明确的错误码与文案，
     * 而不是让它落到引擎内部变成 500。
     */
    public static final int HOST_SERVICE_MISMATCH = 3020;

    /**
     * 错误码 -&gt; 中文含义。
     */
    private static final Map<Integer, String> MESSAGES = buildMessages();

    /**
     * 私有构造，常量类不允许实例化。
     */
    private PluginDistErrorCode() {
    }

    /**
     * 取错误码对应的中文含义；未知错误码返回统一兜底文案。
     *
     * @param code 错误码
     * @return 中文含义
     */
    public static String getMessage(int code) {
        return MESSAGES.getOrDefault(code, "插件分发失败，未知错误码：" + code);
    }

    /**
     * 判断是否为插件分发链路的错误码（3001-3020）。
     *
     * @param code 错误码
     * @return true 表示属于本套错误码
     */
    public static boolean isPluginDistCode(int code) {
        return code >= NOT_LOGIN && code <= HOST_SERVICE_MISMATCH;
    }

    /**
     * 返回全部错误码及其含义（不可变视图，供管理端下拉与文档生成使用）。
     *
     * @return 错误码字典
     */
    public static Map<Integer, String> allMessages() {
        return MESSAGES;
    }

    private static Map<Integer, String> buildMessages() {
        Map<Integer, String> map = new HashMap<>(32);
        map.put(NOT_LOGIN, "未登录");
        map.put(NOT_PURCHASED, "未购买该插件");
        map.put(LICENSE_EXPIRED, "授权已过期");
        map.put(LICENSE_REVOKED, "授权已被吊销");
        map.put(PLUGIN_OR_VERSION_OFFLINE, "插件或版本已下架");
        map.put(VERSION_NOT_PUBLISHED, "版本未发布");
        map.put(DOWNLOAD_TOKEN_EXPIRED, "下载链接已过期");
        map.put(TOKEN_TENANT_MISMATCH, "下载链接与站点不匹配");
        map.put(ARTIFACT_NOT_FOUND, "制品不存在");
        map.put(CHECKSUM_MISMATCH, "制品校验和不一致");
        map.put(SIGNATURE_INVALID, "制品签名无效");
        map.put(VERSION_CONFLICT, "版本冲突");
        map.put(MANIFEST_INVALID, "插件清单非法");
        map.put(INCOMPATIBLE_FRAMEWORK, "框架版本不兼容");
        map.put(DEPENDENCY_MISSING, "依赖插件缺失");
        map.put(TASK_IN_PROGRESS, "已有任务在进行中");
        map.put(SIZE_EXCEEDED, "制品体积超限");
        map.put(ROLLBACK_POINT_MISSING, "回滚点缺失");
        map.put(DEPENDENCY_CYCLE, "依赖成环");
        map.put(HOST_SERVICE_MISMATCH, "插件宿主不匹配");
        return Collections.unmodifiableMap(map);
    }
}
