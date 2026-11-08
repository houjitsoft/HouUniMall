package com.houjiang.mall.service.decoration;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.mall.entity.decoration.LikeDecoratePage;
import com.houjiang.mall.decoration.MobileTemplateViews;
import com.houjiang.mall.mapper.decoration.LikeDecoratePageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 平台商城装修（hou_base_decorate_page，likeshop 字段：id / type / name / data）
 *
 * 端类型隔离：page_platform = mobile(H5/小程序) | pc(PC端)。
 * 列表、设为首页、兜底建页均按端独立；不传该参数时默认 mobile，
 * 保证 H5/小程序前台（既有调用方）行为完全不变。
 *
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class LikeDecoratePageAdminService {

    /** 移动端（H5/小程序） */
    public static final String PLATFORM_MOBILE = "mobile";
    /** PC 端 */
    public static final String PLATFORM_PC = "pc";

    private static final DateTimeFormatter DT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final LikeDecoratePageMapper likeDecoratePageMapper;

    /**
     * 从入参取端类型，缺省或非法值一律回退 mobile。
     * 保证老调用方（不传端）继续读移动端数据。
     */
    private static String platform(Map<String, Object> params) {
        String p = str(params, "pagePlatform", "page_platform", "platform");
        if (PLATFORM_PC.equalsIgnoreCase(p)) {
            return PLATFORM_PC;
        }
        return PLATFORM_MOBILE;
    }

    public Map<String, Object> detail(Map<String, Object> params) {
        LikeDecoratePage page = resolvePage(params);
        if (page == null) {
            Integer type = intVal(params, "type", "page_type", null);
            if (type != null) {
                page = ensureType(type, defaultName(type), "[]");
            }
        }
        if (page == null) {
            throw new IllegalArgumentException("页面不存在");
        }
        return toDetailVo(page);
    }

    public Integer add(Map<String, Object> params) {
        String name = str(params, "name", "page_name");
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("缺少页面名称");
        }
        Integer type = intVal(params, "type", "page_type", 1);
        String data = str(params, "data", "page_data", "content");
        if (data == null) {
            data = "[]";
        }
        long now = System.currentTimeMillis() / 1000;
        LikeDecoratePage page = new LikeDecoratePage();
        page.setType(type);
        page.setName(name.trim());
        page.setData(data);
        page.setMeta(str(params, "meta", "common"));
        page.setPagePlatform(platform(params));
        page.setCreateTime(now);
        page.setUpdateTime(now);
        likeDecoratePageMapper.insert(page);
        return page.getId();
    }

    public void save(Map<String, Object> params) {
        Integer id = intVal(params, "id", "page_id", null);
        LikeDecoratePage page = id != null ? likeDecoratePageMapper.selectById(id) : null;
        if (page == null) {
            Integer type = intVal(params, "type", "page_type", null);
            if (type != null) {
                page = ensureType(type, str(params, "name", "page_name", defaultName(type)),
                        str(params, "data", "page_data", "[]"));
            }
        }
        if (page == null) {
            throw new IllegalArgumentException("页面不存在，请先指定 id 或 type");
        }
        if (params.containsKey("name") || params.containsKey("page_name")) {
            page.setName(str(params, "name", "page_name"));
        }
        if (params.containsKey("type") || params.containsKey("page_type")) {
            page.setType(intVal(params, "type", "page_type", page.getType()));
        }
        if (params.containsKey("data") || params.containsKey("page_data") || params.containsKey("content")) {
            page.setData(str(params, "data", "page_data", "content"));
        }
        if (params.containsKey("meta") || params.containsKey("common")) {
            page.setMeta(str(params, "meta", "common"));
        }
        page.setUpdateTime(System.currentTimeMillis() / 1000);
        likeDecoratePageMapper.updateById(page);
    }

    public void delete(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("缺少页面 id");
        }
        LikeDecoratePage page = likeDecoratePageMapper.selectById(id);
        if (page == null) {
            throw new IllegalArgumentException("页面不存在");
        }
        // 保护范围只限该页所属端的首页，另一端的首页不受影响
        Integer homeId = resolveHomePageId(
                page.getPagePlatform() == null ? PLATFORM_MOBILE : page.getPagePlatform());
        if (homeId != null && homeId.equals(id)) {
            throw new IllegalArgumentException("当前首页不可删除，请先设置其他页面为首页");
        }
        likeDecoratePageMapper.deleteById(id);
    }

    public Map<String, Object> homeLists(Map<String, Object> params) {
        String platform = platform(params);
        if (likeDecoratePageMapper.selectCount(
                new LambdaQueryWrapper<LikeDecoratePage>()
                        .eq(LikeDecoratePage::getType, 1)
                        .eq(LikeDecoratePage::getPagePlatform, platform)) == 0) {
            ensureType(1, "商城首页", "[]", platform);
        }
        int pageNo = intVal(params, "page", "page_no", 1);
        int size = intVal(params, "size", "limit", 15);
        String name = str(params, "name", null);

        LambdaQueryWrapper<LikeDecoratePage> w = new LambdaQueryWrapper<>();
        w.eq(LikeDecoratePage::getType, 1)
                .eq(LikeDecoratePage::getPagePlatform, platform);
        if (StringUtils.hasText(name)) {
            w.like(LikeDecoratePage::getName, name.trim());
        }
        w.orderByDesc(LikeDecoratePage::getUpdateTime);

        Page<LikeDecoratePage> p = likeDecoratePageMapper.selectPage(new Page<>(pageNo, size), w);
        Integer homeId = resolveHomePageId(platform);

        List<Map<String, Object>> lists = new ArrayList<>();
        for (LikeDecoratePage row : p.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.getId());
            item.put("name", row.getName());
            item.put("type", row.getType());
            item.put("page_platform", row.getPagePlatform());
            item.put("is_home", homeId != null && homeId.equals(row.getId()) ? 1 : 0);
            item.put("page_template_code", row.getPageTemplateCode() == null ? "" : row.getPageTemplateCode());

            item.put("create_time", formatTime(row.getCreateTime()));
            item.put("update_time", formatTime(row.getUpdateTime()));
            lists.add(item);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("lists", lists);
        out.put("list", lists);
        out.put("count", p.getTotal());
        out.put("page", pageNo);
        out.put("size", size);
        return out;
    }

    public LikeDecoratePage ensureType(int type, String name, String data) {
        return ensureType(type, name, data, PLATFORM_MOBILE);
    }

    /** 按端兜底建页：同一type 在两端各自独立存在 */
    public LikeDecoratePage ensureType(int type, String name, String data, String platform) {
        LikeDecoratePage exist = likeDecoratePageMapper.selectOne(
                new LambdaQueryWrapper<LikeDecoratePage>()
                        .eq(LikeDecoratePage::getType, type)
                        .eq(LikeDecoratePage::getPagePlatform, platform)
                        .last("LIMIT 1"));
        if (exist != null) {
            return exist;
        }
        long now = System.currentTimeMillis() / 1000;
        LikeDecoratePage page = new LikeDecoratePage();
        page.setType(type);
        page.setName(StringUtils.hasText(name) ? name.trim() : defaultName(type));
        page.setData(StringUtils.hasText(data) ? data : "[]");
        page.setMeta(type == 1 ? "{\"is_home\":1}" : "");
        page.setPagePlatform(platform);
        page.setCreateTime(now);
        page.setUpdateTime(now);
        likeDecoratePageMapper.insert(page);
        return page;
    }

    private static String defaultName(int type) {
        return switch (type) {
            case 1 -> "商城首页";
            case 2 -> "个人中心";
            case 3 -> "客服设置";
            case 4 -> "分类页面";
            case 5 -> "商品详情";
            case 6 -> "商城风格";
            case 7 -> "限时秒杀";
            case 8 -> "超级拼团";
            default -> "装修页面";
        };
    }

    public void setHome(Integer id) {
        setHome(id, PLATFORM_MOBILE);
    }

    /** 设为首页：只在同一端内互斥，不影响另一端的首页标记 */
    public void setHome(Integer id, String platform) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("缺少页面 id");
        }
        LikeDecoratePage target = likeDecoratePageMapper.selectById(id);
        if (target == null || !Integer.valueOf(1).equals(target.getType())) {
            throw new IllegalArgumentException("页面不存在或不是首页微页面");
        }
        List<LikeDecoratePage> all = likeDecoratePageMapper.selectList(
                new LambdaQueryWrapper<LikeDecoratePage>()
                        .eq(LikeDecoratePage::getType, 1)
                        .eq(LikeDecoratePage::getPagePlatform, platform));
        long now = System.currentTimeMillis() / 1000;
        for (LikeDecoratePage p : all) {
            String meta = clearHomeFlag(p.getMeta());
            if (p.getId().equals(id)) {
                meta = setHomeFlag(meta);
            }
            p.setMeta(meta);
            p.setUpdateTime(now);
            likeDecoratePageMapper.updateById(p);
        }
    }

    /** 前台首页使用的 type=1 页面 id（移动端，供 H5/小程序既有调用方使用） */
    public Integer resolveHomePageId() {
        return resolveHomePageId(PLATFORM_MOBILE);
    }

    /** 按端解析首页 id */
    public Integer resolveHomePageId(String platform) {
        List<LikeDecoratePage> all = likeDecoratePageMapper.selectList(
                new LambdaQueryWrapper<LikeDecoratePage>()
                        .eq(LikeDecoratePage::getType, 1)
                        .eq(LikeDecoratePage::getPagePlatform, platform)
                        .orderByAsc(LikeDecoratePage::getId));
        for (LikeDecoratePage p : all) {
            if (isHomeMeta(p.getMeta())) {
                return p.getId();
            }
        }
        return all.isEmpty() ? null : all.get(0).getId();
    }

    public LikeDecoratePage getHomePage() {
        Integer id = resolveHomePageId();
        if (id == null) {
            return null;
        }
        return likeDecoratePageMapper.selectById(id);
    }

    // ------------------------------------------------------------------ 页面模板

    /**
     * 模板库（后台模板选择页）。
     *
     * <p>模板定义镜像自 base 的 {@code MobilePageTemplates}，见 {@code MobileTemplateViews}。
     *
     * @return 模板列表，含 code / name / summary / themeColor / style / columns
     */
    public List<Map<String, Object>> templates() {
        return MobileTemplateViews.toAdminList();
    }

    /**
     * 读取某页面当前套用的模板 code。
     *
     * <p>库中为 NULL（从未设置）时返回默认模板 {@link MobileTemplateViews#DEFAULT_CODE}，
     * 与前台实际渲染一致；空串表示后台显式「不套用」，保持原样返回。
     *
     * @param params { pageId } 或 { id }
     * @return templateCode 字符串
     */
    public String currentTemplate(Map<String, Object> params) {
        LikeDecoratePage page = requirePage(params);
        String code = page.getPageTemplateCode();
        return code == null ? MobileTemplateViews.DEFAULT_CODE : code;
    }

    /**
     * 为页面套用模板（空串表示显式「不套用」，保持页面原版式）。
     *
     * <p>写的是装修页面自身的 {@code page_template_code}，与 H5 首页读取的是同一条记录；
     * 语义：NULL=未设置（走默认模板 tea），空串=不套用，有效 code=该模板。
     *
     * @param params { pageId, templateCode }
     * @return 实际生效的 code（不套用时为空串）
     */
    public String applyTemplate(Map<String, Object> params) {
        LikeDecoratePage page = requirePage(params);
        String raw = str(params, "templateCode", "template_code", "code");
        String code = (raw == null || raw.isBlank()) ? "" : raw.trim();
        if (!code.isEmpty() && !MobileTemplateViews.isValid(code)) {
            throw new IllegalArgumentException("未知的模板 code：" + code);
        }
        // 空串必须显式 set：updateById 默认忽略空值，取消套用时清不掉。
        likeDecoratePageMapper.update(null, new LambdaUpdateWrapper<LikeDecoratePage>()
                .eq(LikeDecoratePage::getId, page.getId())
                .set(LikeDecoratePage::getPageTemplateCode, code)
                .set(LikeDecoratePage::getUpdateTime, System.currentTimeMillis() / 1000));
        return code;
    }

    /**
     * 取模板操作的目标页面：必须显式给 id，避免误改首页。
     *
     * @param params 请求参数
     * @return 页面实体
     */
    private LikeDecoratePage requirePage(Map<String, Object> params) {
        Integer id = intVal(params, "pageId", "page_id", null);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("缺少页面 id");
        }
        LikeDecoratePage page = likeDecoratePageMapper.selectById(id);
        if (page == null) {
            throw new IllegalArgumentException("页面不存在");
        }
        return page;
    }

    private LikeDecoratePage resolvePage(Map<String, Object> params) {
        Integer id = intVal(params, "id", "page_id", null);
        if (id != null) {
            return likeDecoratePageMapper.selectById(id);
        }
        Integer type = intVal(params, "type", "page_type", null);
        if (type != null) {
            return likeDecoratePageMapper.selectOne(
                    new LambdaQueryWrapper<LikeDecoratePage>()
                            .eq(LikeDecoratePage::getType, type)
                            .last("LIMIT 1"));
        }
        return null;
    }

    private Map<String, Object> toDetailVo(LikeDecoratePage page) {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("id", page.getId());
        vo.put("type", page.getType());
        vo.put("name", page.getName());
        vo.put("common", page.getMeta());
        vo.put("page_platform", page.getPagePlatform());
        vo.put("page_template_code", page.getPageTemplateCode() == null ? "" : page.getPageTemplateCode());

        vo.put("create_time", formatTime(page.getCreateTime()));
        vo.put("update_time", formatTime(page.getUpdateTime()));
        vo.put("data", normalizeAdminDetailData(page));
        return vo;
    }

    /**
     * 首页 data 形态：[[page-meta], widget...]；秒杀/拼团为 [[data-meta], widget...]。
     * 商品详情、个人中心是扁平组件数组，不要强行塞 page-meta。
     */
    @SuppressWarnings("unchecked")
    private List<Object> normalizeAdminDetailData(LikeDecoratePage page) {
        Object parsed = parseDataArray(page.getData());
        List<Object> data = parsed instanceof List<?> rawList ? new ArrayList<>(rawList) : new ArrayList<>();
        Integer type = page.getType();

        if (!data.isEmpty() && data.get(0) instanceof List<?> firstGroup
                && !firstGroup.isEmpty() && firstGroup.get(0) instanceof Map<?, ?> grouped) {
            if (isMetaName(String.valueOf(grouped.get("name")))) {
                return data;
            }
        }

        if (!data.isEmpty() && data.get(0) instanceof Map<?, ?> firstMap) {
            Map<String, Object> first = (Map<String, Object>) firstMap;
            if (isMetaName(String.valueOf(first.get("name")))) {
                List<Object> result = new ArrayList<>();
                result.add(List.of(first));
                result.addAll(data.subList(1, data.size()));
                return result;
            }
        }

        if (Integer.valueOf(1).equals(type)) {
            List<Object> widgets = new ArrayList<>();
            for (Object item : data) {
                if (item instanceof Map<?, ?> map && isMetaName(String.valueOf(map.get("name")))) {
                    continue;
                }
                widgets.add(item);
            }
            List<Object> result = new ArrayList<>();
            result.add(parsePageMetaGroup(page.getMeta()));
            result.addAll(widgets);
            return result;
        }
        return data;
    }

    private static boolean isMetaName(String name) {
        return "page-meta".equals(name) || "data-meta".equals(name);
    }

    @SuppressWarnings("unchecked")
    private List<Object> parsePageMetaGroup(String meta) {
        if (!StringUtils.hasText(meta)) {
            return defaultPageMetaGroup();
        }
        try {
            Object parsed = JSON.parse(meta);
            if (parsed instanceof List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof Map<?, ?> map) {
                    return new ArrayList<>((List<Object>) list);
                }
            }
            if (parsed instanceof Map<?, ?> map) {
                return List.of((Map<String, Object>) map);
            }
        } catch (Exception ignored) {
            // fall through
        }
        return defaultPageMetaGroup();
    }

    private List<Object> defaultPageMetaGroup() {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("title_type", 1);
        content.put("bg_img", "");
        content.put("bg_color", "#F8F8F8");
        content.put("module_type", 1);
        content.put("title", "首页");
        content.put("title_color", 1);
        content.put("titlebg_type", 1);
        content.put("titlebg_color", "#FFFFFF");
        content.put("titlebg_img", "");
        content.put("scroll_type", 1);
        content.put("scroll_color", "#FFFFFF");
        content.put("scroll_img", "");

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", "页面设置");
        meta.put("name", "page-meta");
        meta.put("content", content);
        meta.put("styles", new LinkedHashMap<>());
        return List.of(meta);
    }

    private static Object parseDataArray(String raw) {
        if (!StringUtils.hasText(raw)) {
            return new JSONArray();
        }
        try {
            return JSON.parse(raw);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private static boolean isHomeMeta(String meta) {
        if (!StringUtils.hasText(meta)) {
            return false;
        }
        try {
            var obj = JSON.parseObject(meta);
            return obj != null && obj.getIntValue("is_home") == 1;
        } catch (Exception e) {
            return meta.contains("\"is_home\":1") || meta.contains("\"is_home\": 1");
        }
    }

    private static String setHomeFlag(String meta) {
        try {
            var obj = StringUtils.hasText(meta) ? JSON.parseObject(meta) : new com.alibaba.fastjson2.JSONObject();
            if (obj == null) {
                obj = new com.alibaba.fastjson2.JSONObject();
            }
            obj.put("is_home", 1);
            return obj.toJSONString();
        } catch (Exception e) {
            return "{\"is_home\":1}";
        }
    }

    private static String clearHomeFlag(String meta) {
        if (!StringUtils.hasText(meta)) {
            return meta;
        }
        try {
            var obj = JSON.parseObject(meta);
            if (obj == null) {
                return meta;
            }
            obj.remove("is_home");
            return obj.isEmpty() ? "" : obj.toJSONString();
        } catch (Exception e) {
            return meta.replace("\"is_home\":1", "").replace("\"is_home\": 1", "");
        }
    }

    private static String formatTime(Long ts) {
        if (ts == null || ts <= 0) {
            return "";
        }
        return DT.format(Instant.ofEpochSecond(ts));
    }

    private static String str(Map<String, Object> params, String k1, String k2) {
        return str(params, k1, k2, null);
    }

    private static String str(Map<String, Object> params, String k1, String k2, String k3) {
        if (params == null) {
            return null;
        }
        for (String k : new String[]{k1, k2, k3}) {
            if (k == null) {
                continue;
            }
            Object v = params.get(k);
            if (v != null) {
                return v.toString();
            }
        }
        return null;
    }

    private static Integer intVal(Map<String, Object> params, String k1, String k2, Integer def) {
        String s = str(params, k1, k2);
        if (!StringUtils.hasText(s)) {
            return def;
        }
        return Integer.parseInt(s.trim());
    }
}
