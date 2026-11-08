package com.houjiang.mall.decoration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 移动端页面模板视图（mall 侧）。
 *
 * <p>模板库的权威定义在 base 服务的
 * {@code com.houjiang.base.service.template.MobilePageTemplates}，
 * 但 mall 不依赖 base 的编译期模块（只有 Feign），而首页 {@code legacyHome} 是高频接口，
 * 不适合每请求一次 RPC 去取三个静态字段。
 *
 * <p>因此这里保留一份<b>只读镜像</b>：仅 {@code code / name / summary / themeColor / style} 五个字段，
 * 与 base 的 {@code toAdminList()} / {@code toFrontView()} 输出保持一致。
 * <b>新增或修改模板时必须同步两处</b>（base 的 MobilePageTemplates 与本文件），
 * 否则后台能选到、前台下发不出来。
 *
 * <p>模板只决定版式与配色，不携带任何商品数据。
 *
 * @author Amos
 */
public final class MobileTemplateViews {

    private MobileTemplateViews() {
    }

    /** 默认模板 code，与 base {@code MobilePageTemplates.DEFAULT_CODE} 保持一致。 */
    public static final String DEFAULT_CODE = "tea";

    /**
     * 模板定义。
     *
     * @param code       模板 code，落到 hou_base_decorate_page.page_template_code
     * @param name       后台展示名
     * @param summary    一句话说明
     * @param themeColor 主色
     * @param style      版式代号：single-column / double-column / banner-first
     */
    public record Template(String code, String name, String summary,
                           String themeColor, String style) {
    }

    /** 与 base 模板库顺序一致 */
    private static final List<Template> TEMPLATES = List.of(
            new Template("simple", "简约白", "单列大图、克制留白，突出商品本身", "#19c7a5", "single-column"),
            new Template("vivid", "活力橙", "双列瀑布、价格突出，适合促销与拉新", "#f2994a", "double-column"),
            new Template("premium", "高端黑", "宽幅banner、小图留白，适合品牌旗舰店", "#1f2733", "banner-first"),
            new Template("tea", "奢侈品简单风格", "轻奢简约：暖象牙底、墨绿横幅、茶褐金强调与衬线标题", "#b0805a", "tea-brand")
    );

    /**
     * 按 code 取模板定义。
     *
     * @param code 模板 code，可为 null
     * @return 命中返回模板，未命中或为空返回 {@code null}
     */
    public static Template find(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String trimmed = code.trim();
        for (Template t : TEMPLATES) {
            if (t.code().equalsIgnoreCase(trimmed)) {
                return t;
            }
        }
        return null;
    }

    /**
     * 是否为已知模板 code。
     *
     * @param code 模板 code
     * @return 合法返回 true
     */
    public static boolean isValid(String code) {
        return find(code) != null;
    }

    /**
     * 后台模板列表（字段与 base toAdminList 对齐）。
     *
     * @return 模板列表
     */
    public static List<Map<String, Object>> toAdminList() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Template t : TEMPLATES) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", t.code());
            row.put("name", t.name());
            row.put("summary", t.summary());
            row.put("themeColor", t.themeColor());
            row.put("style", t.style());
            row.put("columns", columnsOf(t.style()));
            row.put("isDefault", DEFAULT_CODE.equals(t.code()));
            result.add(row);
        }
        return result;
    }

    /**
     * 前台渲染所需的模板描述。
     *
     * <p>code 为 null（未设置）时回落默认模板 {@link #DEFAULT_CODE}；
     * 空串表示后台显式选择「不套用」，返回<b>空 Map</b>，前端据此保持页面原始版式。
     *
     * @param code 模板 code
     * @return {templateCode, style, themeColor, columns}，显式不套用时为空 Map
     */
    public static Map<String, Object> toFrontView(String code) {
        if (code != null && code.isBlank()) {
            return new LinkedHashMap<>();
        }
        Template t = find(code);
        if (t == null) {
            t = find(DEFAULT_CODE);
        }
        if (t == null) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("templateCode", t.code());
        view.put("style", t.style());
        view.put("themeColor", t.themeColor());
        view.put("columns", columnsOf(t.style()));
        return view;
    }

    private static int columnsOf(String style) {
        return "double-column".equals(style) || "tea-brand".equals(style) ? 2 : 1;
    }
}
