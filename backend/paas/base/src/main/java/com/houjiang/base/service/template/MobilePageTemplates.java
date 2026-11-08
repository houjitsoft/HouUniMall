package com.houjiang.base.service.template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 移动端页面模板库（内置预置模板）。
 *
 * <p>后台「移动端装修 → 模板选择」从这里取列表；选中后把 {@code templateCode} 写回
 * {@code hou_base_modularizationd.template_code}，前台 uniapp 按该字段选择渲染版式。
 *
 * <p>模板只定义<b>版式与主题</b>（模块顺序、栅格列数、配色、圆角风格），
 * 不写死商品数据——商品由页面自身的 {@code moduleJson.items} 与商品库决定，
 * 因此换模板不会丢商品配置。
 *
 * <p>命名说明：沿用内部代号 {@code hou_base_*} 既有表与 {@code template_code} 字段口径，
 * 不做品牌改名（改名属破坏性变更）。
 *
 * @author HouCloud
 */
public final class MobilePageTemplates {

    private MobilePageTemplates() {
    }

    /** 模板库版本，便于后续扩展与前端缓存失效判断。 */
    public static final String LIB_VERSION = "1.1.0";

    /**
     * 默认模板 code：页面未设置模板（NULL）时前台按此模板渲染。
     *
     * <p>与「显式不套用」区分：{@code page_template_code} 为空串表示保持页面原版式，
     * 该开关不受默认值影响。
     */
    public static final String DEFAULT_CODE = "tea";

    /**
     * 模板定义。顺序即后台展示顺序。
     *
     * @param code       模板 code，落到 page_template_code
     * @param name       后台展示名
     * @param summary    一句话说明，后台模板卡副标题
     * @param themeColor 主色（uniapp 以CSS 变量下发）
     * @param style      版式风格代号，前台据此选择布局分支
     */
    public record Template(String code, String name, String summary,
                           String themeColor, String style) {
    }

    private static final List<Template> TEMPLATES = buildTemplates();

    private static List<Template> buildTemplates() {
        List<Template> list = new ArrayList<>();
        // 简约白：单列大图，克制留白，适合品牌与生鲜
        list.add(new Template("simple", "简约白", "单列大图、克制留白，突出商品本身",
                "#19c7a5", "single-column"));
        // 活力橙：双列瀑布 + 价格突出，适合下沉市场与促销
        list.add(new Template("vivid", "活力橙", "双列瀑布、价格突出，适合促销与拉新",
                "#f2994a", "double-column"));
        // 高端黑：宽幅 banner + 小图留白，适合品牌旗舰店
        list.add(new Template("premium", "高端黑", "宽幅banner、小图留白，适合品牌旗舰店",
                "#1f2733", "banner-first"));
        // 奢侈品简单风格：轻奢留白，适合礼品、珠宝、茶酒等品牌旗舰
        list.add(new Template("tea", "奢侈品简单风格", "轻奢简约：暖象牙底、墨绿横幅、茶褐金强调与衬线标题",
                "#b0805a", "tea-brand"));
        return List.copyOf(list);
    }

    /**
     * 全部模板。
     *
     * @return 不可变模板列表
     */
    public static List<Template> all() {
        return TEMPLATES;
    }

    /**
     * 按 code 取模板。
     *
     * @param code 模板 code
     * @return 命中返回模板，未命中返回 {@code null}
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
     * 模板 code 白名单，供持久层校验用。
     *
     * @return code 集合
     */
    public static List<String> codes() {
        return TEMPLATES.stream().map(Template::code).toList();
    }

    /**
     * 给后台的模板列表 VO（附加预览用布局信息）。
     *
     * @return 模板列表，每个元素含 code/name/summary/themeColor/style/layoutHint
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

    private static int columnsOf(String style) {
        return "double-column".equals(style) || "tea-brand".equals(style) ? 2 : 1;
    }

    /**
     * 给前台的模板描述：只下发渲染需要的最小字段。
     *
     * @param code 模板 code；null 或未知 code 回落到 {@link #DEFAULT_CODE}
     * @return 模板描述
     */
    public static Map<String, Object> toFrontView(String code) {
        Template t = find(code);
        if (t == null) {
            t = find(DEFAULT_CODE);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("templateCode", t.code());
        view.put("style", t.style());
        view.put("themeColor", t.themeColor());
        view.put("columns", columnsOf(t.style()));
        return view;
    }
}