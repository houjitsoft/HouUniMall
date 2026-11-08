package com.houjiang.commons.errorcode;

/**
 * 错误码域编号分配表 —— 全站唯一的域权威定义。
 *
 * <p>解决的问题：
 * <ol>
 *   <li>旧码段（0/1/2/200、1097-1099、1199、1200-1299、1600-1603、2000-2001、2401、
 *       3001-3019、7001-7002、8002-8005、10000-18040、10086、999999）互相穿插、跨枚举冲突，
 *       新人不敢新增码。新码段采用 <code>code = domainId * 1000 + seq</code>，
 *       <code>domainId ∈ [20,99]</code>、<code>seq ∈ [1,999]</code>，码值域 <b>20001-99999</b>，
 *       与存量最大值 18040 之间留 1960 的隔离带。</li>
 *   <li>同一个数字在不同枚举里是两件事（如 11007 在 BizCodeEnume 是「权限不足」、
 *       在 RiderCodeEnume 是「短信验证码错误」）。新体系里域号决定语义归属，号段互不重叠。</li>
 * </ol>
 *
 * <p>域内序号（seq）分配约定：
 * <ul>
 *   <li>001-009 跨域统一语义槽（001 资源不存在 / 002 参数非法 / 003 状态不允许 / 004 重复冲突 /
 *       005 禁止访问 / 006 额度超限 / 007 查询为空 / 008 上游调用失败 / 009 配置缺失）。</li>
 *   <li>010-099 域内通用业务槽。</li>
 *   <li>100-799 子模块顺序段（每子模块占 100 号）。</li>
 *   <li>800-899 域内扩展预留。</li>
 *   <li>900-999 <b>保留，禁止业务使用</b>（自检 V3 拦截）。</li>
 * </ul>
 *
 * <p>扩容：优先启用 RESERVED 域号；耗尽后 domainId 扩到 3 位 → 100001-999998（避开 999999）。
 * 扩容只需放宽域号上界，公式与解析逻辑不变。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum ErrorDomain {

    // ==================== 20-28 平台基础域 ====================
    SYSTEM(20, "SYSTEM", "系统通用", "paas-commons", false, "框架级/未知/DB/序列化/线程池"),
    VALIDATION(21, "VALIDATION", "参数校验", "paas-commons", false, "全局 handler 的参数与方法校验"),
    UPSTREAM(22, "UPSTREAM", "上游依赖", "paas-commons / bff-web", false, "Feign 下游调用失败、超时、解码失败"),
    THIRDPARTY(23, "THIRDPARTY", "第三方集成", "paas-thirdparty", false, "微信/支付宝/短信/对象存储等外部通道"),
    FILE(24, "FILE", "文件与存储", "paas-commons", false, "上传/下载/存储配置"),
    RATE_LIMIT(25, "RATE_LIMIT", "限流与幂等", "paas-commons", false, "AccessLimit / RepeatClick / 幂等冲突"),
    SECURITY(26, "SECURITY", "安全与风控", "paas-commons / paas-auth", false, "签名、风控、IP 封禁、敏感操作"),
    CONFIG(27, "CONFIG", "配置与字典", "paas-base", false, "系统配置、数据字典"),
    JOB(28, "JOB", "任务与异步", "paas-job", false, "定时任务、异步执行"),

    RESERVED_29(29, "RESERVED_29", "保留", "—", true, "域号保留，禁止分配业务"),

    // ==================== 30-36 账号与主体域 ====================
    AUTH(30, "AUTH", "认证登录", "paas-auth", false, "账号、凭证、token、短信码"),
    PERMISSION(31, "PERMISSION", "权限与角色", "paas-permission", false, "角色、菜单、数据权限"),
    USER(32, "USER", "用户与会员", "paas-userms", false, "会员、等级、地址、余额"),
    MERCHANT(33, "MERCHANT", "商户与店铺", "paas-merchant", false, "商户入驻、审核、合同模板"),
    ORG(34, "ORG", "组织与员工", "paas-permission / paas-userms", false, "组织架构、部门、员工"),
    RIDER(35, "RIDER", "骑手与配送", "legacy/migration-source/backend/paas/rider", false, "骑手、审核、提现账户"),
    SUPPLIER(36, "SUPPLIER", "供应商", "plugins/supplier", false, "供应商入驻与供货"),

    RESERVED_37(37, "RESERVED_37", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_38(38, "RESERVED_38", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_39(39, "RESERVED_39", "保留", "—", true, "域号保留，禁止分配业务"),

    // ==================== 40-47 交易域 ====================
    GOODS(40, "GOODS", "商品", "paas-goods", false, "SPU/SKU、分类、规格"),
    CART(41, "CART", "购物车", "paas-goods / paas-order", false, "购物车项、数量上限"),
    ORDER(42, "ORDER", "订单", "paas-order", false, "订单生命周期、取消、发货"),
    AFTER_SALE(43, "AFTER_SALE", "售后服务", "paas-order", false, "售后单、退款"),
    PAYMENT(44, "PAYMENT", "支付", "paas-payment", false, "支付单、渠道、验签、退款"),
    MARKETING(45, "MARKETING", "营销", "paas-goods", false, "优惠券、秒杀、拼团"),
    LOGISTICS(46, "LOGISTICS", "物流配送", "paas-order", false, "运单、配送轨迹"),
    INVENTORY(47, "INVENTORY", "库存", "paas-goods", false, "库存锁定、扣减、不足"),

    RESERVED_48(48, "RESERVED_48", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_49(49, "RESERVED_49", "保留", "—", true, "域号保留，禁止分配业务"),

    // ==================== 50-57 业务插件域 ====================
    CMS(50, "CMS", "内容站点", "plugins/cms", false, "站点、栏目、内容、模板"),
    MESSAGE(51, "MESSAGE", "消息与 IM", "plugins/im / paas-thirdparty", false, "站内信、IM 会话"),
    CRM(52, "CRM", "客户管理", "plugins/crm", false, "客户、商机、跟进"),
    OA(53, "OA", "办公 OA", "plugins/oa", false, "考勤、薪资、审批"),
    FINANCE(54, "FINANCE", "财务", "plugins/finance", false, "账务、对账、发票"),
    ERP(55, "ERP", "ERP", "plugins/erp", false, "采购、仓储、生产"),
    PM(56, "PM", "项目管理", "plugins/pm", false, "项目、任务、企微群"),
    MES(57, "MES", "MES", "plugins/mes", false, "工单、工序、报工"),

    RESERVED_58(58, "RESERVED_58", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_59(59, "RESERVED_59", "保留", "—", true, "域号保留，禁止分配业务"),

    // ==================== 60-65 扩展能力域 ====================
    GEO(60, "GEO", "GEO 优化", "plugins/geo", false, "GEO 内容与投放"),
    AI(61, "AI", "AI 能力", "plugins/aicode", false, "AI 生成、模型调用"),
    ORDERFOOD(62, "ORDERFOOD", "点餐", "plugins/orderfood", false, "点餐、桌台、菜品"),
    WEBPOS(63, "WEBPOS", "收银", "plugins/webpos", false, "收银台、结账"),
    LOGGING(64, "LOGGING", "日志审计", "plugins/logging", false, "审计日志落库与查询"),
    ACTIVITY(65, "ACTIVITY", "活动运营", "plugins/order/order-activity-hub", false,
            "活动运营（2026-10-17 拆分：order 7 / userms 3 / goods 1 / cms 2，入口为 order-activity-hub）"),

    RESERVED_66(66, "RESERVED_66", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_67(67, "RESERVED_67", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_68(68, "RESERVED_68", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_69(69, "RESERVED_69", "保留", "—", true, "域号保留，禁止分配业务"),

    // ==================== 70-74 插件平台域 ====================
    PLUGIN_PLATFORM(70, "PLUGIN_PLATFORM", "插件平台", "paas-plugin-* / plugin-engine", false, "插件注册、清单、运行时"),
    PLUGIN_LICENSE(71, "PLUGIN_LICENSE", "插件授权", "paas-plugin-license / plugin-signature", false, "License 与制品验签"),
    PLUGIN_PUBLISH(72, "PLUGIN_PUBLISH", "插件发布", "paas-plugin-publish / plugin-artifact", false, "上传幂等、版本状态机"),
    PLUGIN_UPDATE(73, "PLUGIN_UPDATE", "插件升级", "paas-plugin-update", false, "安装/升级/回滚编排"),
    EXTENSION(74, "EXTENSION", "二开扩展", "backend/extensions/custom-*", false, "客户二开模块"),

    RESERVED_75(75, "RESERVED_75", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_76(76, "RESERVED_76", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_77(77, "RESERVED_77", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_78(78, "RESERVED_78", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_79(79, "RESERVED_79", "保留", "—", true, "域号保留，禁止分配业务"),

    // ==================== 80-81 BFF 域 ====================
    BFF(80, "BFF", "BFF 契约", "bff-web", false, "BFF 转发、响应解码、路由"),
    GATEWAY(81, "GATEWAY", "网关与租户", "bff-web", false, "租户解析、网关策略"),

    RESERVED_82(82, "RESERVED_82", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_83(83, "RESERVED_83", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_84(84, "RESERVED_84", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_85(85, "RESERVED_85", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_86(86, "RESERVED_86", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_87(87, "RESERVED_87", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_88(88, "RESERVED_88", "保留", "—", true, "域号保留，禁止分配业务"),
    RESERVED_89(89, "RESERVED_89", "保留", "—", true, "域号保留，禁止分配业务"),

    RESERVED_90(90, "RESERVED_90", "保留（未来新域）", "—", true, "域号保留，禁止分配业务"),
    RESERVED_91(91, "RESERVED_91", "保留（未来新域）", "—", true, "域号保留，禁止分配业务"),
    RESERVED_92(92, "RESERVED_92", "保留（未来新域）", "—", true, "域号保留，禁止分配业务"),
    RESERVED_93(93, "RESERVED_93", "保留（未来新域）", "—", true, "域号保留，禁止分配业务"),
    RESERVED_94(94, "RESERVED_94", "保留（未来新域）", "—", true, "域号保留，禁止分配业务"),
    RESERVED_95(95, "RESERVED_95", "保留（未来新域）", "—", true, "域号保留，禁止分配业务"),
    RESERVED_96(96, "RESERVED_96", "保留（未来新域）", "—", true, "域号保留，禁止分配业务"),
    RESERVED_97(97, "RESERVED_97", "保留（未来新域）", "—", true, "域号保留，禁止分配业务"),
    RESERVED_98(98, "RESERVED_98", "保留（未来新域）", "—", true, "域号保留，禁止分配业务"),

    /** 自检/测试专用域，禁止业务使用（99001 用于自检自测）。 */
    RESERVED_SELFTEST(99, "RESERVED_SELFTEST", "自检测试", "paas-errorcode", true, "禁止业务使用，仅供自检单测")
    ;

    /** 域号下界（当前启用）。 */
    public static final int MIN_DOMAIN_ID = 20;

    /** 域号上界（当前启用，5 位码值）。 */
    public static final int MAX_DOMAIN_ID = 99;

    /** 域内 seq 上界（900-999 为保留段，业务实际可用到 899）。 */
    public static final int MAX_SEQ = 999;

    /** 每域 900-999 保留段的起点，业务不得使用（自检 V3）。 */
    public static final int RESERVED_SEQ_START = 900;

    private final int domainId;
    private final String code;
    private final String cnName;
    private final String ownerModule;
    private final boolean reserved;
    private final String description;

    ErrorDomain(int domainId, String code, String cnName, String ownerModule, boolean reserved, String description) {
        this.domainId = domainId;
        this.code = code;
        this.cnName = cnName;
        this.ownerModule = ownerModule;
        this.reserved = reserved;
        this.description = description;
    }

    /**
     * 域号。
     *
     * @return 域号，20-99
     */
    public int domainId() {
        return domainId;
    }

    /**
     * 域英文代号（与枚举名一致）。
     *
     * @return 域代号
     */
    public String code() {
        return code;
    }

    /**
     * 域中文名。
     *
     * @return 中文名
     */
    public String cnName() {
        return cnName;
    }

    /**
     * 归属 Maven 模块（追责用）。
     *
     * @return 归属模块名
     */
    public String ownerModule() {
        return ownerModule;
    }

    /**
     * 是否为保留域（禁止分配业务码）。
     *
     * @return true 表示保留域
     */
    public boolean reserved() {
        return reserved;
    }

    /**
     * 域职责描述。
     *
     * @return 描述
     */
    public String description() {
        return description;
    }

    /**
     * 本域最小可用码值（domainId * 1000 + 1）。
     *
     * @return 号段起点
     */
    public int baseCode() {
        return domainId * 1000 + 1;
    }

    /**
     * 本域最大码值（domainId * 1000 + 999）。
     *
     * @return 号段终点
     */
    public int maxCode() {
        return domainId * 1000 + MAX_SEQ;
    }

    /**
     * 判断码值是否落在本域号段内。
     *
     * @param code 待判断的码值
     * @return true 表示属于本域
     */
    public boolean owns(int code) {
        return code >= baseCode() && code <= maxCode();
    }

    /**
     * 按码值反查归属域。
     *
     * @param code 统一错误码（20001-99999）
     * @return 归属域
     * @throws IllegalArgumentException 码值不在 20001-99999 区间时抛出
     */
    public static ErrorDomain of(int code) {
        return ofId(code / 1000);
    }

    /**
     * 按域号反查域定义。
     *
     * @param domainId 域号
     * @return 域定义
     * @throws IllegalArgumentException 域号未登记（不在 20-99）时抛出
     */
    public static ErrorDomain ofId(int domainId) {
        if (domainId < MIN_DOMAIN_ID || domainId > MAX_DOMAIN_ID) {
            throw new IllegalArgumentException(
                    "错误码域号越界：domainId=" + domainId + "，合法范围 [" + MIN_DOMAIN_ID + "," + MAX_DOMAIN_ID + "]");
        }
        for (ErrorDomain domain : values()) {
            if (domain.domainId == domainId) {
                return domain;
            }
        }
        throw new IllegalArgumentException("错误码域号未登记：domainId=" + domainId);
    }
}
