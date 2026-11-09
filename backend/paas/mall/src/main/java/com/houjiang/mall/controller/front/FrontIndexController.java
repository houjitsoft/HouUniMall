package com.houjiang.mall.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.decoration.LikeDecoratePage;
import com.houjiang.mall.entity.decoration.LikeDecorateTabbar;
import com.houjiang.mall.entity.goods.MallGoodsCategory;
import com.houjiang.mall.decoration.H5DecorateBridge;
import com.houjiang.mall.decoration.MobileTemplateViews;
import com.houjiang.mall.mapper.decoration.LikeDecoratePageMapper;
import com.houjiang.mall.mapper.decoration.LikeDecorateTabbarMapper;
import com.houjiang.mall.mapper.goods.MallGoodsCategoryMapper;
import com.houjiang.mall.service.decoration.LikeDecoratePageAdminService;
import com.houjiang.mall.service.decoration.MallStyleAdminService;
import com.houjiang.mall.service.setting.CommonConfigService;
import com.houjiang.mall.service.setting.SettingSearchService;
import com.houjiang.mall.service.setting.SettingWebsiteService;
import com.houjiang.mall.util.DecorateContentNormalizer;
import com.houjiang.mall.util.MallMediaUrlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城前台首页数据接口（供 BFF 内部调用）
 *
 * 从 hou_base_decorate_page 表读取 likeshop 原始装修数据，
 * 组装为前端 index.vue 期望的格式。
 *
 * 路径：/mall/front/index/*
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/front/index")
@RequiredArgsConstructor
public class FrontIndexController {

    // BFF 首页配置聚合入口（商城前端统一从本模块读取）

    private final LikeDecoratePageMapper likeDecoratePageMapper;
    private final LikeDecorateTabbarMapper likeDecorateTabbarMapper;
    private final MallGoodsCategoryMapper mallGoodsCategoryMapper;
    private final LikeDecoratePageAdminService likeDecoratePageAdminService;
    private final CommonConfigService commonConfigService;
    private final MallStyleAdminService mallStyleAdminService;
    private final SettingWebsiteService settingWebsiteService;
    private final SettingSearchService settingSearchService;
    private final MallMediaUrlHelper mediaUrlHelper;
    private final H5DecorateBridge h5DecorateBridge;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取商城公共配置
     * POST /mall/front/index/config
     *
     * 从 hou_decorate_tabbar 读取底部导航，
     * 从 hou_merchant_config mall_style 读取系统主题（后台「商城风格」页），
     * 网站信息来自 website 配置。
     *
     * @return { domain, style, tabbar, login, website, webPage, version, mall_style, hot_search, service_config }
     */
    @RequestMapping(value = "/config", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult<Map<String, Object>> config() {
        try {
            Map<String, Object> data = new LinkedHashMap<>();

            // domain（前端 getImageUrl 拼接用，必须以 / 结尾）
            data.put("domain", mediaUrlHelper.resolvePublicDomain());

            // 导航颜色（hou_merchant_config: tabbar/style）
            Map<String, String> styleCfg = commonConfigService.getJsonMap("tabbar", "style");
            String defaultColor = firstNonBlank(styleCfg, "default_color", "defaultColor", "#999999");
            String selectedColor = firstNonBlank(styleCfg, "selected_color", "selectedColor", "#4A5DFF");
            Map<String, Object> style = new LinkedHashMap<>();
            style.put("defaultColor", defaultColor);
            style.put("selectedColor", selectedColor);
            style.put("default_color", defaultColor);
            style.put("selected_color", selectedColor);
            data.put("style", style);

            // 底部 tabbar（从数据库读取）
            List<Map<String, Object>> tabbarList = new ArrayList<>();
            List<LikeDecorateTabbar> tabbars = likeDecorateTabbarMapper.selectList(
                    new LambdaQueryWrapper<LikeDecorateTabbar>()
                            .eq(LikeDecorateTabbar::getIsShow, 1)
                            .orderByAsc(LikeDecorateTabbar::getId));
            for (LikeDecorateTabbar tab : tabbars) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", tab.getId());
                item.put("name", tab.getName());
                item.put("selected", mediaUrlHelper.toAbsoluteUrl(
                        tab.getSelected() != null ? tab.getSelected() : ""));
                item.put("unselected", mediaUrlHelper.toAbsoluteUrl(
                        tab.getUnselected() != null ? tab.getUnselected() : ""));
                // link 是 JSON 字符串，解析为对象
                if (tab.getLink() != null && !tab.getLink().isEmpty()) {
                    try {
                        item.put("link", objectMapper.readValue(tab.getLink(), Map.class));
                    } catch (Exception e) {
                        item.put("link", new LinkedHashMap<>());
                    }
                } else {
                    item.put("link", new LinkedHashMap<>());
                }
                // 时间戳转日期字符串
                item.put("create_time", tab.getCreateTime() != null
                        ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(tab.getCreateTime() * 1000))
                        : "");
                item.put("update_time", tab.getUpdateTime() != null
                        ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(tab.getUpdateTime() * 1000))
                        : "");
                tabbarList.add(item);
            }
            data.put("tabbar", tabbarList);

            // 登录配置
            Map<String, Object> login = new LinkedHashMap<>();
            login.put("login_way", List.of("1", "2"));
            login.put("coerce_mobile", 0);
            login.put("login_agreement", 0);
            login.put("third_auth", 0);
            login.put("wechat_auth", 0);
            login.put("qq_auth", 0);
            data.put("login", login);

            // 网站信息（hou_merchant_config website.*）
            Map<String, Object> website = new LinkedHashMap<>();
            try {
                website.putAll(settingWebsiteService.getWebsite());
            } catch (Exception e) {
                log.warn("读取网站配置失败，使用默认站点名", e);
            }
            Object shopName = firstNonBlankObj(website.get("name"), website.get("shop_name"), "houcloud商城");
            website.put("name", shopName);
            website.put("shop_name", shopName);
            if (website.get("shop_logo") == null) {
                website.put("shop_logo", "");
            }
            data.put("website", website);

            // 版权/备案信息也并入公共配置，前端页脚不再依赖已下线的 mall-base sysConfig 接口。
            try {
                List<Map<String, String>> copyright = settingWebsiteService.getCopyright();
                data.put("copyright", copyright);
                for (Map<String, String> item : copyright) {
                    String key = item.get("key");
                    String value = item.get("value");
                    if (key != null && value != null && !value.isBlank()) {
                        website.putIfAbsent(key, value);
                    }
                }
            } catch (Exception e) {
                data.put("copyright", new ArrayList<>());
            }

            // logo_copywriting 配置供 PC/H5 头部、登录页和协议页统一读取。
            // 旧版接口返回数组，新版 BFF 保持同样的键值语义，避免前端再直连已下线的 mall-base。
            try {
                data.put("logo_copywriting", commonConfigService.getAll("logo_copywriting"));
            } catch (Exception e) {
                log.warn("读取 logo_copywriting 配置失败", e);
                data.put("logo_copywriting", new LinkedHashMap<>());
            }

            // H5 配置
            Map<String, Object> webPage = new LinkedHashMap<>();
            webPage.put("status", 1);
            webPage.put("page_status", 0);
            webPage.put("page_url", "");
            webPage.put("url", "");
            data.put("webPage", webPage);

            // 版本号
            data.put("version", "v1.0.0");

            // 商城主题风格（后台 /mall/decoration/mall_style）
            Map<String, Object> mallStyle = buildMallStyle();
            data.put("mall_style", mallStyle);

            // 热门搜索（hou_merchant_hot_search）
            try {
                data.put("hot_search", settingSearchService.getConfig());
            } catch (Exception e) {
                log.warn("读取热门搜索失败", e);
                data.put("hot_search", Map.of("status", 0, "data", List.of()));
            }

            // 客服配置（默认配置）
            Map<String, Object> serviceConfig = buildServiceConfig();
            data.put("service_config", serviceConfig);

            return ResponseResult.success(data);
        } catch (Exception e) {
            log.error("获取商城配置失败", e);
            return ResponseResult.error("获取商城配置失败：" + e.getMessage());
        }
    }

    /**
     * 获取首页商品分类（三级树，PC mega menu / 国际站 chips 共用）
     * GET|POST /mall/front/index/category
     *
     * @return { category_lists, lists, list }
     */
    @RequestMapping(value = "/category", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult<Map<String, Object>> category() {
        try {
            List<Map<String, Object>> tree = buildCategoryTree();
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("category_lists", tree);
            data.put("lists", tree);
            data.put("list", tree);
            return ResponseResult.success(data);
        } catch (Exception e) {
            log.error("获取商品分类失败", e);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("category_lists", new ArrayList<>());
            data.put("lists", new ArrayList<>());
            data.put("list", new ArrayList<>());
            return ResponseResult.success(data);
        }
    }

    /**
     * 热门搜索词（PC 顶栏）
     */
    @RequestMapping(value = "/hotSearch", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult<Map<String, Object>> hotSearch() {
        try {
            return ResponseResult.success(settingSearchService.getConfig());
        } catch (Exception e) {
            log.warn("读取热门搜索失败", e);
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("status", 0);
            empty.put("data", new ArrayList<>());
            return ResponseResult.success(empty);
        }
    }

    /**
     * 获取首页装修数据
     * POST /mall/front/index/data
     *
     * @param params 可选参数 { category_id }
     * @return { page: { data: [...] }, article: [], shop_recommend: [] }
     */
    @PostMapping("/data")
    public ResponseResult<Map<String, Object>> indexData(
            @RequestBody(required = false) Map<String, Object> params) {
        try {
            // 查询当前设为首页的 type=1 微页面（meta.is_home 或默认首条）
            LikeDecoratePage homePage = likeDecoratePageAdminService.getHomePage();

            Map<String, Object> pageMap = new LinkedHashMap<>();
            if (homePage != null && homePage.getData() != null) {
                // data 字段是 JSON 数组字符串，需解析为对象
                List<Object> parsedData;
                try {
                    parsedData = objectMapper.readValue(homePage.getData(), new TypeReference<List<Object>>() {});
                } catch (Exception e) {
                    log.warn("首页装修 JSON 解析失败，返回空数组", e);
                    parsedData = new ArrayList<>();
                }

                // 前端 index.vue 期望 page.data 为混合数组：
                // 第一个元素为子数组 [{page-meta}]，后续为普通装修组件对象
                // 参考系统格式：[[{page-meta}], {banner}, {nav}, ...]
                List<Object> finalData = ensurePageMetaFormat(parsedData);
                finalData = DecorateContentNormalizer.normalizePageData(finalData);
                absolutizeMediaInPageData(finalData);

                pageMap.put("data", finalData);
                pageMap.put("id", homePage.getId());
                pageMap.put("name", homePage.getName());
                pageMap.put("type", homePage.getType());
                // NULL=未设置，按默认模板下发；空串=后台显式「不套用」，原样下发
                String homeTplCode = homePage.getPageTemplateCode();
                pageMap.put("page_template_code",
                        homeTplCode == null ? MobileTemplateViews.DEFAULT_CODE : homeTplCode);
            } else {
                pageMap.put("data", buildDefaultData());
                pageMap.put("name", "首页");
                pageMap.put("type", 1);
                pageMap.put("page_template_code", MobileTemplateViews.DEFAULT_CODE);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("page", pageMap);
            result.put("article", new java.util.ArrayList<>());
            result.put("shop_recommend", new java.util.ArrayList<>());

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("获取首页装修数据失败", e);
            return ResponseResult.error("获取首页数据失败：" + e.getMessage());
        }
    }

    /**
     * H5 首页装修数据（老结构）。
     *
     * <p>GET|POST /mall/front/index/legacyHome
     *
     * <p>后台装修已升级为新版组件模型，而 H5（uni-app）只认老的
     * {@code pageData = {pageBaseInfo, moduleList}} 结构。这里用 {@link H5DecorateBridge}
     * 把新版组件翻译成老结构，H5 不改渲染代码即可吃到最新装修配置。
     * 返回字段与 base 老接口 {@code modularizationdIndex} 对齐，H5 可直接替换数据源。
     *
     * @return { pageId, pageName, pageData(JSON 字符串), templateView, goodsList }
     */
    @RequestMapping(value = "/legacyHome", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult<Map<String, Object>> legacyHome() {
        try {
            LikeDecoratePage homePage = likeDecoratePageAdminService.getHomePage();
            Map<String, Object> legacy = h5DecorateBridge.buildLegacyHome(homePage);
            if (legacy == null) {
                return ResponseResult.error("首页装修数据为空，请在后台「移动装修 → 页面装修」保存首页");
            }
            return ResponseResult.success(legacy);
        } catch (Exception e) {
            log.error("获取 H5 首页装修数据失败", e);
            return ResponseResult.error("获取首页装修数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取装修页面（按类型）
     * POST /mall/front/index/decorate
     *
     * @param params { type: 1|2|3|4|5 }
     * @return 装修页面数据
     */
    @PostMapping("/decorate")
    public ResponseResult<Map<String, Object>> getDecorateByType(
            @RequestBody Map<String, Object> params) {
        try {
            Integer type = params.get("type") != null
                    ? Integer.parseInt(params.get("type").toString()) : 1;

            LikeDecoratePage page = likeDecoratePageMapper.selectOne(
                    new LambdaQueryWrapper<LikeDecoratePage>()
                            .eq(LikeDecoratePage::getType, type)
                            .last("LIMIT 1"));

            Map<String, Object> result = new LinkedHashMap<>();
            if (page != null) {
                Object parsedData;
                try {
                    parsedData = objectMapper.readValue(page.getData(), new TypeReference<List<Object>>() {});
                } catch (Exception e) {
                    parsedData = new ArrayList<>();
                }
                result.put("data", parsedData);
                result.put("name", page.getName());
                result.put("type", page.getType());
            }

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("获取装修数据失败", e);
            return ResponseResult.error("获取装修数据失败：" + e.getMessage());
        }
    }

    /**
     * 确保数据第一个元素是包含 page-meta 的子数组
     * 前端 index.vue 期望：page.pages[0][0].content.title_type
     * 参考系统格式：[[{page-meta}], {banner}, {nav}, ...]
     *
     * @param data 原始装修数据（可能是纯一维数组）
     * @return 转换后的混合数组
     */
    @SuppressWarnings("unchecked")
    private List<Object> ensurePageMetaFormat(List<Object> data) {
        if (data == null || data.isEmpty()) {
            return buildDefaultData();
        }

        // 检查第一个元素是否已经是子数组（2D 格式）
        Object first = data.get(0);
        if (first instanceof List) {
            // 已经是参考系统的混合格式，直接返回
            return data;
        }

        // 第一个元素是普通对象（1D 格式），需要注入 page-meta
        // 检查是否已有 page-meta 或 header 组件
        Map<String, Object> pageMetaItem = null;
        int pageMetaIndex = -1;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) instanceof Map) {
                Map<String, Object> item = (Map<String, Object>) data.get(i);
                String name = item.get("name") != null ? item.get("name").toString() : "";
                if ("page-meta".equals(name) || "header".equals(name)) {
                    pageMetaItem = item;
                    pageMetaIndex = i;
                    break;
                }
            }
        }

        List<Object> result = new ArrayList<>();

        if (pageMetaItem != null) {
            // 已有 page-meta/header，将其包装为子数组放到首位
            List<Object> metaGroup = new ArrayList<>();
            metaGroup.add(pageMetaItem);
            result.add(metaGroup);
            // 其余元素保持不变
            for (int i = 0; i < data.size(); i++) {
                if (i != pageMetaIndex) {
                    result.add(data.get(i));
                }
            }
        } else {
            // 没有 page-meta，创建默认的并注入
            List<Object> metaGroup = new ArrayList<>();
            metaGroup.add(buildDefaultPageMeta());
            result.add(metaGroup);
            // 所有原始数据追加在后面
            result.addAll(data);
        }

        return result;
    }

    /**
     * 构建默认 page-meta 组件（页面标题、背景等设置）
     *
     * @return page-meta 组件 Map
     */
    private Map<String, Object> buildDefaultPageMeta() {
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
        meta.put("styles", new ArrayList<>());
        return meta;
    }

    /**
     * 构建默认首页数据（含 page-meta）
     *
     * @return 包含默认 page-meta 的混合数组
     */
    private List<Object> buildDefaultData() {
        List<Object> metaGroup = new ArrayList<>();
        metaGroup.add(buildDefaultPageMeta());
        List<Object> data = new ArrayList<>();
        data.add(metaGroup);
        return data;
    }

    /**
     * 从后台「商城风格」读取当前主题 + PC 布局/导航。
     * 旧数据（仅 decorate_page type=5）作为颜色兜底。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildMallStyle() {
        Map<String, Object> mallStyle = new LinkedHashMap<>();
        try {
            Map<String, Object> all = mallStyleAdminService.getMallStyle();
            if (all != null) {
                String current = String.valueOf(all.getOrDefault("current", "gold_theme"));
                mallStyle.put("current", current);
                copyIfPresent(all, mallStyle, "pc_template_code");
                copyIfPresent(all, mallStyle, "pc_layout");
                copyIfPresent(all, mallStyle, "pc_container");
                copyIfPresent(all, mallStyle, "pc_radius");
                copyIfPresent(all, mallStyle, "pc_currency");
                copyIfPresent(all, mallStyle, "pc_nav");
                copyIfPresent(all, mallStyle, "pc_floors");
                copyIfPresent(all, mallStyle, "pc_show_rank");
                copyIfPresent(all, mallStyle, "pc_show_recommend");
                Object listObj = all.get("list");
                if (listObj instanceof List<?> list && !list.isEmpty()) {
                    mallStyle.put("list", list);
                    for (Object o : list) {
                        if (o instanceof Map<?, ?> item && current.equals(String.valueOf(item.get("className")))) {
                            mallStyle.putAll((Map<String, Object>) item);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("读取 mall_style 配置失败，尝试装修页兜底", e);
        }

        if (mallStyle.get("variable") == null && mallStyle.get("color") == null) {
            mergeDecoratePageTheme(mallStyle);
        }
        if (mallStyle.get("nav_bg_color") == null) {
            mallStyle.put("nav_bg_color", "#FFFFFF");
        }
        if (mallStyle.get("nav_text_color") == null) {
            mallStyle.put("nav_text_color", "#1A1A1A");
        }
        return mallStyle;
    }

    @SuppressWarnings("unchecked")
    private void mergeDecoratePageTheme(Map<String, Object> mallStyle) {
        try {
            LikeDecoratePage stylePage = likeDecoratePageMapper.selectOne(
                    new LambdaQueryWrapper<LikeDecoratePage>()
                            .eq(LikeDecoratePage::getType, 5)
                            .last("LIMIT 1"));
            if (stylePage != null && stylePage.getData() != null && !stylePage.getData().isEmpty()) {
                Map<String, Object> raw = objectMapper.readValue(stylePage.getData(),
                        new TypeReference<Map<String, Object>>() {});
                String themeColor = raw.getOrDefault("themeColor1", "#4A5DFF").toString();
                String darkColor = raw.getOrDefault("themeColor2", "#7B8CFF").toString();
                String navTextColor = raw.getOrDefault("topTextColor", "#000000").toString();
                String navBgColor = raw.getOrDefault("navigationBarColor", "#ffffff").toString();
                mallStyle.put("name", "houcloud主题");
                mallStyle.put("className", "houcloud_theme");
                mallStyle.put("color", themeColor);
                mallStyle.put("dark_color", darkColor);
                mallStyle.put("nav_text_color", navTextColor);
                mallStyle.put("nav_bg_color", navBgColor);
                Map<String, String> variable = new LinkedHashMap<>();
                variable.put("--theme-color", themeColor);
                variable.put("--theme-light-color", lightenColor(themeColor));
                variable.put("--theme-disabled-color", disabledColor(themeColor));
                variable.put("--theme-dark-color", darkColor);
                mallStyle.put("variable", variable);
            }
        } catch (Exception e) {
            log.warn("读取装修页主题失败", e);
        }
    }

    private List<Map<String, Object>> buildCategoryTree() {
        List<MallGoodsCategory> all = mallGoodsCategoryMapper.selectList(
                new LambdaQueryWrapper<MallGoodsCategory>()
                        .eq(MallGoodsCategory::getIsShow, 1)
                        .eq(MallGoodsCategory::getDeleteTime, 0)
                        .orderByAsc(MallGoodsCategory::getSort)
                        .orderByDesc(MallGoodsCategory::getId));
        Map<Integer, List<MallGoodsCategory>> byPid = new LinkedHashMap<>();
        for (MallGoodsCategory cat : all) {
            int pid = cat.getPid() == null ? 0 : cat.getPid();
            byPid.computeIfAbsent(pid, k -> new ArrayList<>()).add(cat);
        }
        return toCategoryNodes(0, byPid);
    }

    private List<Map<String, Object>> toCategoryNodes(int pid, Map<Integer, List<MallGoodsCategory>> byPid) {
        List<MallGoodsCategory> children = byPid.get(pid);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (MallGoodsCategory cat : children) {
            Map<String, Object> item = new LinkedHashMap<>();
            Integer id = cat.getId();
            String name = cat.getName() == null ? "" : cat.getName();
            String image = cat.getImage() != null ? mediaUrlHelper.toAbsoluteUrl(cat.getImage()) : "";
            // 后台字段 + PC 侧栏常用别名，避免前端只认 catId/catName/catPic 时丢数据
            item.put("id", id);
            item.put("catId", id);
            item.put("name", name);
            item.put("catName", name);
            item.put("pid", cat.getPid());
            item.put("level", cat.getLevel());
            item.put("image", image);
            item.put("catPic", image);
            item.put("sort", cat.getSort());
            item.put("is_show", cat.getIsShow());
            List<Map<String, Object>> sons = toCategoryNodes(id == null ? -1 : id, byPid);
            item.put("sons", sons);
            item.put("children", sons);
            nodes.add(item);
        }
        return nodes;
    }

    private static void copyIfPresent(Map<String, Object> from, Map<String, Object> to, String key) {
        Object v = from.get(key);
        if (v != null) {
            to.put(key, v);
        }
    }

    private static Object firstNonBlankObj(Object... values) {
        for (Object v : values) {
            if (v != null && !v.toString().isBlank()) {
                return v;
            }
        }
        return "";
    }

    /**
     * 构建客服配置（暂用默认值，后续可从数据库读取）
     *
     * @return service_config Map
     */
    private Map<String, Object> buildServiceConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        Map<String, Object> defaultChannel = new LinkedHashMap<>();
        defaultChannel.put("way", "1");
        defaultChannel.put("name", "houcloud");
        defaultChannel.put("remarks", "长按添加客服或拨打客服热线");
        defaultChannel.put("phone", "");
        defaultChannel.put("business_time", "工作日：9:00—18:00");
        defaultChannel.put("qr_code", "");
        defaultChannel.put("enterprise_id", "");
        defaultChannel.put("kefu_link", "");
        defaultChannel.put("service_phone", "");

        config.put("mnp", new LinkedHashMap<>(defaultChannel));
        config.put("oa", new LinkedHashMap<>(defaultChannel));
        config.put("h5", new LinkedHashMap<>(defaultChannel));
        return config;
    }

    /**
     * 生成浅色版主题色（用于 --theme-light-color）
     *
     * @param hex HEX 颜色码
     * @return 浅色版 HEX
     */
    private String lightenColor(String hex) {
        try {
            hex = hex.replace("#", "");
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            r = Math.min(255, r + (255 - r) * 80 / 100);
            g = Math.min(255, g + (255 - g) * 80 / 100);
            b = Math.min(255, b + (255 - b) * 80 / 100);
            return String.format("#%02X%02X%02X", r, g, b);
        } catch (Exception e) {
            return "#F0F0FF";
        }
    }

    /**
     * 生成 disabled 版主题色（用于 --theme-disabled-color）
     *
     * @param hex HEX 颜色码
     * @return disabled 版 HEX
     */
    private String disabledColor(String hex) {
        try {
            hex = hex.replace("#", "");
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            r = Math.min(255, r + (255 - r) * 50 / 100);
            g = Math.min(255, g + (255 - g) * 50 / 100);
            b = Math.min(255, b + (255 - b) * 50 / 100);
            return String.format("#%02X%02X%02X", r, g, b);
        } catch (Exception e) {
            return "#C0C0FF";
        }
    }

    private static String firstNonBlank(Map<String, String> map, String... keys) {
        if (map == null) {
            return keys[keys.length - 1];
        }
        for (int i = 0; i < keys.length - 1; i++) {
            String v = map.get(keys[i]);
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return keys[keys.length - 1];
    }

  /**
   * 将装修组件内的图片路径转为绝对 URL，供 H5 直接加载。
   */
    @SuppressWarnings("unchecked")
    private void absolutizeMediaInPageData(List<Object> data) {
        if (data == null) {
            return;
        }
        for (Object item : data) {
            if (item instanceof List<?> list) {
                absolutizeMediaInPageData((List<Object>) list);
            } else if (item instanceof Map<?, ?> map) {
                Map<String, Object> comp = (Map<String, Object>) map;
                Object contentObj = comp.get("content");
                if (contentObj instanceof Map<?, ?> contentMap) {
                    absolutizeMediaInContent((Map<String, Object>) contentMap);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void absolutizeMediaInContent(Map<String, Object> content) {
        for (String key : List.of("bg_img", "bg_image", "titlebg_img", "scroll_img", "titile_img", "image", "qrcode")) {
            Object value = content.get(key);
            if (value instanceof String str && !str.isEmpty()) {
                content.put(key, mediaUrlHelper.toAbsoluteUrl(str));
            }
        }
        Object dataList = content.get("data");
        if (!(dataList instanceof List<?> rows)) {
            return;
        }
        for (Object row : rows) {
            if (!(row instanceof Map<?, ?> rowMap)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) rowMap;
            Object image = item.get("image");
            if (image instanceof String img && !img.isEmpty()) {
                item.put("image", mediaUrlHelper.toAbsoluteUrl(img));
            }
            Object bg = item.get("bg");
            if (bg instanceof String bgStr && !bgStr.isEmpty()) {
                item.put("bg", mediaUrlHelper.toAbsoluteUrl(bgStr));
            }
        }
    }
}
