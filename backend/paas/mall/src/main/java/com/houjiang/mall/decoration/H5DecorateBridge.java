package com.houjiang.mall.decoration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.decoration.LikeDecoratePage;
import com.houjiang.mall.feign.BaseInformationFeign;
import com.houjiang.mall.mapper.decoration.H5DecorateDataMapper;
import com.houjiang.mall.util.MallMediaUrlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 新版后台装修 → H5 老模块结构 的桥接转换。
 *
 * <p>背景：后台「装修管理」已经换成新版组件模型（page-meta / banner / notice / nav / cube /
 * goodslists / seckill …，每个组件是 {name, content, styles}），
 * 而 H5（uni-app）客户端只认老结构 {@code pageData = {pageBaseInfo, moduleList:[{moduleSign, moduleJson}]}}。
 * 两边不通会导致「后台改了装修、H5 首页纹丝不动」。
 *
 * <p>这里把新版 widget 翻译成老 moduleSign/moduleJson，让 H5 不改一行渲染代码也能吃到新配置。
 * 未找到对应老模块的组件（例如「店铺列表」在 H5 侧没有对应模块）会被跳过并记日志，
 * 不猜、不硬塞，避免 H5 渲染出乱七八糟的模块。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class H5DecorateBridge {

    private final MallMediaUrlHelper mediaUrlHelper;
    private final BaseInformationFeign baseInformationFeign;
    private final H5DecorateDataMapper decorateDataMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 组装 H5 首页所需的老结构响应体。
     *
     * @param page 新版装修首页（is_home=1）
     * @return { pageId, pageName, pageData(JSON 字符串), templateView, goodsList }
     */
    public Map<String, Object> buildLegacyHome(LikeDecoratePage page) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Object> widgets = parseWidgets(page == null ? null : page.getData());
        if (widgets.isEmpty()) {
            return null;
        }
        Map<String, Object> legacy = toLegacyPage(widgets);
        try {
            result.put("pageId", page.getId());
            result.put("pageName", page.getName());
            result.put("pageData", objectMapper.writeValueAsString(legacy));
        } catch (Exception e) {
            throw new IllegalStateException("装修数据序列化失败", e);
        }
        result.put("templateView", MobileTemplateViews.toFrontView(page.getPageTemplateCode()));
        result.put("goodsList", new ArrayList<>());
        return result;
    }

    /** 解析装修 data 字段：可能是 JSON 字符串，也可能是已经是数组。 */
    @SuppressWarnings("unchecked")
    public List<Object> parseWidgets(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<Object> parsed = objectMapper.readValue(raw, new TypeReference<List<Object>>() {
            });
            List<Object> flat = new ArrayList<>();
            for (Object item : parsed) {
                if (item instanceof List<?> nested) {
                    for (Object one : nested) {
                        if (one != null) {
                            flat.add(one);
                        }
                    }
                } else if (item != null) {
                    flat.add(item);
                }
            }
            return flat;
        } catch (Exception e) {
            log.warn("装修 JSON 解析失败，返回空组件列表", e);
            return new ArrayList<>();
        }
    }

    /** 组件数组 → 老 pageData 结构。 */
    public Map<String, Object> toLegacyPage(List<Object> widgets) {
        Map<String, Object> pageBaseInfo = new LinkedHashMap<>();
        List<Object> moduleList = new ArrayList<>();

        for (Object widgetObj : widgets) {
            if (!(widgetObj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> widget = (Map<String, Object>) raw;
            String name = str(widget.get("name"));
            Map<String, Object> content = widget.get("content") instanceof Map<?, ?> c
                    ? (Map<String, Object>) c
                    : new LinkedHashMap<>();
            if (isHidden(content)) {
                continue;
            }

            switch (name) {
                case "page-meta" -> pageBaseInfo.putAll(toPageBaseInfo(content));
                case "search" -> moduleList.add(module("moduleSearch", toSearch(content)));
                case "banner" -> moduleList.add(module("moduleSwiper", toSwiper(content)));
                case "middle-banner" -> moduleList.add(module("moduleImg", toImageList(content)));
                case "notice" -> moduleList.add(module("moduleStoreNotice", toNotice(content)));
                case "nav" -> moduleList.add(module("moduleTab", toTab(content)));
                case "cube" -> moduleList.add(module("moduleMagicCube", toMagicCube(content)));
                case "title" -> moduleList.add(module("moduleTitle", toTitle(content)));
                case "blank" -> moduleList.add(module("modulePlace", toPlace(content)));
                case "goodslists" -> moduleList.add(module("moduleGoods", toGoods(content)));
                case "seckill" -> moduleList.add(module("moduleSecKill", toSeckill(content)));
                case "team" -> moduleList.add(module("modulePintuan", toPintuan(content)));
                case "subsidy" -> moduleList.add(module("moduleDiscount", toDiscount(content)));
                case "news" -> moduleList.add(module("moduleArticle", toArticle(content)));
                default -> {
                    if (name != null && !name.isBlank()) {
                        log.info("H5 桥接：暂不支持的新组件 {}，本次跳过", name);
                    }
                }
            }
        }

        pageBaseInfo.putIfAbsent("title", "首页");
        pageBaseInfo.putIfAbsent("pageIsIndex", 1);

        Map<String, Object> legacy = new LinkedHashMap<>();
        legacy.put("pageBaseInfo", pageBaseInfo);
        legacy.put("moduleList", moduleList);
        return legacy;
    }

    // ------------------------------------------------------------------ 各组件映射

    private Map<String, Object> toPageBaseInfo(Map<String, Object> content) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", str(content.get("title")));
        info.put("pageIsIndex", 1);
        String bg = media(str(content.get("bg_img")));
        if (!bg.isBlank()) {
            info.put("bgImg", bg);
        }
        String bgColor = str(content.get("bg_color"));
        if (!bgColor.isBlank()) {
            info.put("bgColor", bgColor);
        }
        return info;
    }

    private Map<String, Object> toSearch(Map<String, Object> content) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("text", str(content.get("text")));
        json.put("style", "1");
        json.put("textPosition", "1");
        json.put("padTop", "0px");
        json.put("padBom", "10px");
        json.put("borderStyle", "1");
        json.put("borderColor", "#e5e7eb");
        json.put("radius", 15);
        json.put("inputBg", "");
        json.put("inputBgColor", "#ffffff");
        json.put("inputColor", "#333333");
        json.put("inputImg", "");
        json.put("inputImgW", "");
        json.put("inputImgH", "");
        json.put("rightImg", "");
        json.put("rightImgW", "");
        json.put("rightImgH", "");
        return json;
    }

    private Map<String, Object> toSwiper(Map<String, Object> content) {
        List<Object> items = new ArrayList<>();
        for (Object itemObj : list(content.get("data"))) {
            if (!(itemObj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) raw;
            if (!isShown(item.get("is_show"))) {
                continue;
            }
            String image = media(str(item.get("image")));
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("link", link(item.get("link")));
            entry.put("imgUrl", image);
            // H5 的 moduleSwiper 按 imgUrlList[currentImageIndex] 取图（支持同一坑位多图轮播），
            // 只给 imgUrl 会取到 undefined，表现为轮播区域空白/只剩一张破图。
            entry.put("imgUrlList", new ArrayList<>(List.of(image)));
            entry.put("linkSource", "1");
            items.add(entry);
        }
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("padding", "0px");
        json.put("padTop", "0px");
        json.put("padBom", "0px");
        json.put("pageMargin", "0px");
        json.put("items", items);
        json.put("idArr", new ArrayList<>());
        return json;
    }

    private Map<String, Object> toImageList(Map<String, Object> content) {
        List<Object> items = new ArrayList<>();
        for (Object itemObj : list(content.get("data"))) {
            if (!(itemObj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) raw;
            if (!isShown(item.get("is_show"))) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("link", link(item.get("link")));
            entry.put("imgUrl", media(str(item.get("image"))));
            entry.put("linkSource", "1");
            items.add(entry);
        }
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("style", "3");
        json.put("items", items);
        return json;
    }

    private Map<String, Object> toNotice(Map<String, Object> content) {
        List<Object> items = new ArrayList<>();
        for (Object itemObj : list(content.get("lists"))) {
            if (!(itemObj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) raw;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("value", str(item.get("title")));
            entry.put("link", link(item.get("link")));
            items.add(entry);
        }
        Map<String, Object> scroll = new LinkedHashMap<>();
        scroll.put("number", 0);
        scroll.put("items", items);

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("hasIcon", "1");
        json.put("iconStyle", "1");
        json.put("scroll", scroll);
        json.put("scrollDirection", "top");
        json.put("textColor", "#212121");
        json.put("bgColor", "#ffffff");
        json.put("padding", "10px");
        return json;
    }

    private Map<String, Object> toTab(Map<String, Object> content) {
        List<Object> items = new ArrayList<>();
        for (Object itemObj : list(content.get("data"))) {
            if (!(itemObj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) raw;
            if (!isShown(item.get("is_show"))) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", str(item.get("name")));
            entry.put("link", link(item.get("link")));
            entry.put("imgUrl", media(str(item.get("image"))));
            items.add(entry);
        }
        // 后台 nav 的 show_nums 是「3/4/5 个」的枚举(1/2/3)，H5 moduleTab 要的是实际列数 rowNum(3/4/5)
        int showNums = intOf(content.get("show_nums"), 3);
        int rowNum = switch (showNums) {
            case 1 -> 3;
            case 2 -> 4;
            default -> 5;
        };
        // 后台 show_type：1=固定显示 2=分页滑动；H5 tabStyle 同口径
        int showType = intOf(content.get("show_type"), 1);

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("style", showType == 2 ? "2" : "1");
        json.put("rowNum", rowNum);
        json.put("items", items);
        json.put("textColor", "#212121");
        json.put("iconColor", "#212121");
        json.put("bgColor", "#ffffff");
        json.put("padding", "10px");
        // H5 moduleTab 读的是分方向的 padding / margin / 圆角，缺字段会渲染成 undefined
        json.put("pdT", 10);
        json.put("pdR", 10);
        json.put("pdB", 10);
        json.put("pdL", 10);
        json.put("mrT", 0);
        json.put("mrR", 0);
        json.put("mrB", 0);
        json.put("mrL", 0);
        json.put("rdsTL", 8);
        json.put("rdsTR", 8);
        json.put("rdsBL", 8);
        json.put("rdsBR", 8);
        json.put("idArr", new ArrayList<>());
        return json;
    }

    private Map<String, Object> toMagicCube(Map<String, Object> content) {
        List<Map<String, Object>> items = new ArrayList<>();
        List<Object> data = list(content.get("data"));
        // 后台 cube.image_type：0=1行1个 1=1行2个 2=1行3个 3=左1右2 4=左2右2 5=上1下2
        // H5 moduleMagicCube 只支持等分网格：layoutType 决定每行几个，width/height 是占格数
        int imageType = intOf(content.get("image_type"), 1);
        int layoutType = switch (imageType) {
            case 0 -> 1;
            case 2 -> 3;
            default -> 2;
        };
        // imageType 0 走 H5 的「按图比例」分支(type<=2)，其余走等高方块分支
        int cubeType = imageType == 0 ? 0 : 3;
        for (int i = 0; i < data.size(); i++) {
            if (!(data.get(i) instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) raw;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("link", link(item.get("link")));
            entry.put("image_url", media(str(item.get("image"))));
            entry.put("link_id", 0);
            entry.put("goodsId", "");
            entry.put("commonId", "");
            entry.put("commonName", "");
            entry.put("width", 1);
            entry.put("height", 1);
            // H5 的 moduleMagicCube 用 image_height*width/image_width 算格子高度；
            // 这两个为空会算出 NaN，整个魔方塌成 0 高（表现为图片魔方不显示）。按 1:1 兜底。
            entry.put("image_width", 1);
            entry.put("image_height", 1);
            entry.put("x", layoutType == 1 ? 0 : i % layoutType);
            entry.put("y", layoutType == 1 ? 0 : i / layoutType);
            items.add(entry);
        }
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("items", items);
        json.put("type", cubeType);
        json.put("layoutType", layoutType);
        json.put("borderWidth", 0);
        json.put("paddingMargin", "0px");
        return json;
    }

    private Map<String, Object> toTitle(Map<String, Object> content) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("titleText", str(content.get("title")));
        json.put("titleLink", link(content.get("link")));
        String size = str(content.get("title_size"));
        json.put("fzSize", size.isBlank() ? "16px" : size + "px");
        // 后台标题样式：1=左标题+右侧「更多」 2=居中标题；H5 align 同口径
        json.put("align", intOf(content.get("style"), 1) == 2 ? "center" : "left");
        String color = str(content.get("title_color"));
        json.put("textColor", color.isBlank() ? "#212121" : color);
        // 副标题：后台一直能配，老桥接没下发，H5 侧新增字段承接
        json.put("subtitle", str(content.get("subtitle")));
        String subColor = str(content.get("subtitle_color"));
        json.put("subtitleColor", subColor.isBlank() ? "#666666" : subColor);
        String subSize = str(content.get("subtitle_size"));
        json.put("subtitleSize", subSize.isBlank() ? "12px" : subSize + "px");
        // 右侧「更多」：right=1 时展示，跳转复用标题 link
        boolean showRight = intOf(content.get("right"), 0) == 1;
        json.put("rightText", showRight ? str(content.get("right_title")) : "");
        String rightColor = str(content.get("right_color"));
        json.put("rightColor", rightColor.isBlank() ? "#888888" : rightColor);
        json.put("icon", "无");
        json.put("iconColor", "#9e9e9e");
        json.put("bgColor", "#fff");
        json.put("padTop", "0px");
        json.put("padBom", "0px");
        return json;
    }

    private Map<String, Object> toPlace(Map<String, Object> content) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("style", "1");
        json.put("height", intOf(content.get("height"), 20));
        json.put("bgColor", "#ffffff");
        json.put("isBackground", true);
        json.put("lineType", 1);
        json.put("lineColor", "#d8d8d8");
        return json;
    }

    private Map<String, Object> toGoods(Map<String, Object> content) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("style", "2");
        json.put("priceThrough", true);
        json.put("cart", intOf(content.get("cart_btn"), 1) == 1);
        json.put("cartStyle", str(content.get("cart_style")));
        if (str(json.get("cartStyle")).isBlank()) {
            json.put("cartStyle", "1");
        }
        json.put("cartText", "立即购买");
        json.put("sale", true);
        json.put("waterFull", false);
        List<Object> items = goodsItems(content);
        // 后台没手动挑商品（自动取数模式）时，兜底取在售商品，避免 H5 整块空白
        if (items.isEmpty()) {
            items = saleGoodsItems(intOf(content.get("goods_num"), 10));
        }
        json.put("items", items);
        return json;
    }

    /**
     * 自动取数模式的商品条目：读在售商品主表，转成 H5 moduleGoods 期望结构。
     */
    private List<Object> saleGoodsItems(int limit) {
        List<Object> items = new ArrayList<>();
        try {
            for (Map<String, Object> row : decorateDataMapper.selectSaleGoods(Math.max(limit, 1))) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("commonId", row.get("commonId"));
                entry.put("goodsId", row.get("commonId"));
                entry.put("commonName", str(row.get("commonName")));
                entry.put("commonPrice", row.get("commonPrice"));
                entry.put("commonMarketPrice", row.get("commonMarketPrice"));
                entry.put("commonCostPrice", 0);
                entry.put("commonImage", media(str(row.get("commonImage"))));
                entry.put("commonSalenum", row.get("commonSalenum"));
                entry.put("shopId", row.get("shopId"));
                entry.put("shopName", str(row.get("shopName")));
                entry.put("catId", row.get("catId"));
                entry.put("catName", str(row.get("catName")));
                entry.put("brandId", 0);
                entry.put("brandName", "");
                items.add(entry);
            }
        } catch (Exception e) {
            log.warn("H5 桥接：在售商品查询失败，商品模块本次为空", e);
        }
        return items;
    }

    /**
     * 新版商品条目 → H5 老 moduleGoods 条目。
     *
     * <p>新版后台存的是 {@code {id, image, name, price}}（由商品选择器写入），
     * 老结构要的是 {@code commonId/goodsId/commonName/commonPrice/commonImage…}。
     * 没做这层映射的话，后台选了商品、H5 依然是空的。
     */
    private List<Object> goodsItems(Map<String, Object> content) {
        List<Object> items = new ArrayList<>();
        Object listsObj = content.get("goodsLists");
        if (!(listsObj instanceof Map<?, ?> lists)) {
            return items;
        }
        for (Object itemObj : list(lists.get("list"))) {
            if (!(itemObj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) raw;
            String id = str(item.get("id"));
            if (id.isBlank() && str(item.get("name")).isBlank()) {
                // 后台未选商品时会存成空对象占位，直接跳过，避免 H5 渲染出空卡片
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("commonId", id);
            entry.put("goodsId", id);
            entry.put("commonName", str(item.get("name")));
            entry.put("commonPrice", item.get("price"));
            // 后台商品选择器写入的是 max_line_price / sale_num（后台预览也读这两个），
            // 这里以它们为准、兼容旧别名，保证后台预览与 H5 渲染同一份字段
            Object market = item.get("max_line_price") != null ? item.get("max_line_price") : item.get("marketPrice");
            Object sale = item.get("sale_num") != null ? item.get("sale_num") : item.get("sale");
            entry.put("commonMarketPrice", market);
            entry.put("commonCostPrice", 0);
            entry.put("commonImage", media(str(item.get("image"))));
            entry.put("commonSalenum", sale == null ? 0 : sale);
            entry.put("shopId", item.get("shopId"));
            entry.put("shopName", item.get("shopName"));
            entry.put("catId", item.get("catId"));
            entry.put("catName", item.get("catName"));
            entry.put("brandId", 0);
            entry.put("brandName", "");
            items.add(entry);
        }
        return items;
    }

    private Map<String, Object> toSeckill(Map<String, Object> content) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("style", "1");
        json.put("items", seckillItems(intOf(content.get("showNums"), 3)));
        json.put("isTimeLimit", true);
        json.put("isOriginPrice", true);
        json.put("isSale", true);
        json.put("isBorder", "1");
        json.put("isBackground", "1");
        json.put("isRadius", "1");
        json.put("padding", "10px");
        json.put("idArr", new ArrayList<>());
        json.put("activityId", new ArrayList<>());
        return json;
    }

    private Map<String, Object> toPintuan(Map<String, Object> content) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("style", "1");
        json.put("items", pintuanItems(intOf(content.get("showNums"), 3)));
        String title = str(content.get("title"));
        json.put("titleName", title.isBlank() ? "拼团中心" : title);
        json.put("btnName", "去拼团");
        json.put("isSale", true);
        json.put("isBorder", "1");
        json.put("isBackground", "1");
        json.put("isRadius", "1");
        json.put("padding", "10px");
        json.put("idArr", new ArrayList<>());
        json.put("activityId", new ArrayList<>());
        return json;
    }

    /**
     * 「限时秒杀」数据：按进行中的场次实时取数。
     *
     * <p>新版装修只存取数方式和展示条数，不存商品明细，所以必须查库。
     * 每个场次转成 H5 moduleSecKill 期望的 {titleName, end_time, btnName, items[]}。
     * 查询失败返回空列表（H5 侧 v-if 隐藏模块），不让活动故障拖垮整个首页。
     */
    private List<Object> seckillItems(int limit) {
        List<Object> items = new ArrayList<>();
        try {
            int goodsLimit = Math.max(limit, 1);
            for (Map<String, Object> row : decorateDataMapper.selectOngoingSeckill(5)) {
                List<Object> goods = new ArrayList<>();
                Object seckillId = row.get("seckillId");
                for (Map<String, Object> g : decorateDataMapper.selectSeckillGoods(seckillId, goodsLimit)) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("goodsId", g.get("goodsId"));
                    entry.put("commonId", g.get("commonId"));
                    entry.put("goodsName", str(g.get("goodsName")));
                    entry.put("goodsImage", media(str(g.get("goodsImage"))));
                    entry.put("goodsPrice", g.get("goodsPrice"));
                    entry.put("seckillPrice", g.get("seckillPrice"));
                    entry.put("commonSalenum", g.get("commonSalenum"));
                    goods.add(entry);
                }
                Map<String, Object> section = new LinkedHashMap<>();
                section.put("titleName", str(row.get("seckillName")));
                section.put("end_time", str(row.get("seckillEndTime")));
                section.put("btnName", "去抢购");
                section.put("items", goods);
                items.add(section);
            }
        } catch (Exception e) {
            log.warn("H5 桥接：秒杀活动查询失败，秒杀模块本次为空", e);
        }
        return items;
    }

    /**
     * 「超级拼团」数据：查进行中的拼团活动，转 H5 modulePintuan 期望结构。
     */
    private List<Object> pintuanItems(int limit) {
        List<Object> items = new ArrayList<>();
        try {
            for (Map<String, Object> row : decorateDataMapper.selectOngoingPintuan(Math.max(limit, 1))) {
                Map<String, Object> goods = new LinkedHashMap<>();
                goods.put("goodsId", row.get("commonId"));
                goods.put("commonId", row.get("commonId"));
                goods.put("commonName", str(row.get("commonName")));
                goods.put("commonImage", media(str(row.get("commonImage"))));
                goods.put("price", row.get("commonPrice"));
                goods.put("personNum", row.get("personNum"));
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("items", goods);
                items.add(entry);
            }
        } catch (Exception e) {
            log.warn("H5 桥接：拼团活动查询失败，拼团模块本次为空", e);
        }
        return items;
    }

    private Map<String, Object> toDiscount(Map<String, Object> content) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("style", "1");
        json.put("items", discountItems(content));
        // 补贴专区没有活动时限，开启倒计时会渲染成「已结束」
        json.put("isTimeLimit", false);
        json.put("isOriginPrice", true);
        json.put("isSale", true);
        json.put("isBorder", "1");
        json.put("isBackground", "1");
        json.put("isRadius", "1");
        return json;
    }

    /**
     * 促销风格（秒杀/补贴）的商品条目：在售商品转 {goodsName, goodsImage, goodsPrice, seckillPrice}。
     */
    private List<Object> promotionGoodsItems(int limit) {
        List<Object> goods = new ArrayList<>();
        try {
            for (Map<String, Object> row : decorateDataMapper.selectSaleGoods(Math.max(limit, 1))) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("goodsId", row.get("commonId"));
                entry.put("commonId", row.get("commonId"));
                entry.put("goodsName", str(row.get("commonName")));
                entry.put("goodsImage", media(str(row.get("commonImage"))));
                Object price = row.get("commonPrice");
                entry.put("goodsPrice", price);
                // moduleDiscount 读的是 discountPrice（不是 seckillPrice），漏了会显示 ¥0.00；
                // 补贴专区按 85 折展示，与原价形成对比
                entry.put("discountPrice", discountOf(price));
                entry.put("seckillPrice", price);
                entry.put("commonSalenum", row.get("commonSalenum"));
                goods.add(entry);
            }
        } catch (Exception e) {
            log.warn("H5 桥接：补贴专区商品查询失败", e);
        }
        return goods;
    }

    /**
     * 补贴价：按原价 85 折计算，保留两位小数。
     */
    private double discountOf(Object price) {
        if (price == null) {
            return 0D;
        }
        try {
            return Math.round(Double.parseDouble(String.valueOf(price)) * 85) / 100.0;
        } catch (NumberFormatException e) {
            return 0D;
        }
    }

    /**
     * 「国家补贴」数据：H5 侧与秒杀同构（单场次 + 商品列表），这里复用在售商品填充。
     */
    private List<Object> discountItems(Map<String, Object> content) {
        // moduleDiscount 与秒杀同构，条目要的是 goodsName/goodsImage/seckillPrice，
        // 不能直接复用商品列表组件的 commonName/commonPrice 结构，否则 H5 上名称价格全空
        List<Object> goods = promotionGoodsItems(intOf(content.get("showNums"), 4));
        if (goods.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, Object> section = new LinkedHashMap<>();
        String title = str(content.get("title"));
        section.put("titleName", title.isBlank() ? "国家补贴" : title);
        section.put("end_time", "");
        section.put("btnName", "去看看");
        section.put("items", goods);
        List<Object> items = new ArrayList<>();
        items.add(section);
        return items;
    }

    private Map<String, Object> toArticle(Map<String, Object> content) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("style", "1");
        json.put("items", articleItems());
        json.put("padding", "10px");
        return json;
    }

    /**
     * 「最新资讯」数据：实时调 base 服务用户端资讯接口（最新 20 条已发布），
     * 转成 H5 moduleArticle 期望的 {informationId, informationTitle, informationCoverList, pageView, releaseTime}。
     * 查询失败时返回空列表（H5 侧 v-if 隐藏模块），不让资讯故障拖垮整个首页。
     */
    private List<Object> articleItems() {
        List<Object> items = new ArrayList<>();
        try {
            ResponseResult res = baseInformationFeign.userInformation();
            if (res == null || res.getCode() != 1 || !(res.getData() instanceof List<?> rows)) {
                return items;
            }
            for (Object rowObj : rows) {
                if (!(rowObj instanceof Map<?, ?> raw)) {
                    continue;
                }
                Map<String, Object> row = (Map<String, Object>) raw;
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("informationId", row.get("informationId"));
                entry.put("informationTitle", str(row.get("informationTitle")));
                List<Object> covers = new ArrayList<>();
                if (row.get("informationCoverList") instanceof List<?> coverList) {
                    for (Object cover : coverList) {
                        String url = media(str(cover));
                        if (!url.isBlank()) {
                            covers.add(url);
                        }
                    }
                }
                entry.put("informationCoverList", covers);
                entry.put("pageView", row.get("pageView") == null ? 0 : row.get("pageView"));
                entry.put("releaseTime", str(row.get("releaseTime")));
                items.add(entry);
            }
        } catch (Exception e) {
            log.warn("H5 桥接：资讯数据获取失败，资讯模块本次为空", e);
        }
        return items;
    }

    // ------------------------------------------------------------------ 工具方法

    private Map<String, Object> module(String sign, Map<String, Object> moduleJson) {
        Map<String, Object> module = new LinkedHashMap<>();
        module.put("moduleSign", sign);
        module.put("moduleJson", moduleJson);
        return module;
    }

    /** 新版链接对象 → 老版 url 字符串。 */
    @SuppressWarnings("unchecked")
    private String link(Object linkObj) {
        if (linkObj == null) {
            return "";
        }
        if (linkObj instanceof String text) {
            return text;
        }
        if (!(linkObj instanceof Map<?, ?> raw)) {
            return "";
        }
        Map<String, Object> link = (Map<String, Object>) raw;
        String path = str(link.get("path"));
        if (path.isBlank()) {
            // 未配链接的组件（图片魔方等）就不下发 link；
            // H5 侧 moduleMagicCube/moduleSwiper 等已加 if(!link) return 保护，不会误跳转
            return "";
        }
        Map<String, Object> query = link.get("query") instanceof Map<?, ?> q
                ? (Map<String, Object>) q
                : new LinkedHashMap<>();
        String page = normalizePage(path);
        if (page.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(page);
        boolean first = !page.contains("?");
        for (Map.Entry<String, Object> entry : query.entrySet()) {
            if (entry.getValue() == null || str(entry.getValue()).isBlank()) {
                continue;
            }
            sb.append(first ? '?' : '&');
            first = false;
            sb.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return sb.toString();
    }

    private String media(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        try {
            String absolute = mediaUrlHelper.toAbsoluteUrl(url);
            return absolute == null ? url : absolute;
        } catch (Exception e) {
            return url;
        }
    }

    /** 组件级隐藏开关：enabled=0 或 disabled=true 的组件不下发。 */
    private boolean isHidden(Map<String, Object> content) {
        return intOf(content.get("enabled"), 1) == 0;
    }

    /** 条目级显示开关：is_show 明确为 0/2 时隐藏。 */
    private boolean isShown(Object isShow) {
        if (isShow == null) {
            return true;
        }
        String value = str(isShow);
        return value.isBlank() || "1".equals(value) || "3".equals(value);
    }

/**
     * H5 已注册页面清单（对齐 frontend/uniapp/src/pages.json，2026-10-16 抽取）。
     *
     * <p>后台装修里配的跳转路径带 {@code /pages/} 前缀（如 /pages/news/news），
     * 而 H5 实际注册的是 /news/news —— 直接透传会触发
     * {@code navigateTo:fail page ... is not found}。这里用白名单做归一化：
     * 命中就去前缀重试，仍不命中就返回空串（H5 不跳转，也就不会报错）。
     */
    private static final String H5_PAGE_CSV =
            "pages/index/launch,pages/index/index,pages/cart/cart,pages/finds/finds,pages/search/search,pages/personal/personal,pages/information/information,pages/diy/diy,pages/storeIndex/storeIndex,hybrid/web_view/web_view,hybrid/im_web_view/im_web_view,pages/description/descrption,account/account,account/knowAccount,account/shopAccount,account/accountExport,account/accountDetail,account/corporateAccount,account/accountVoucher,general/login/login,general/brandsearchpage/brandsearchpage,general/diy/diy,general/event/event,general/autologin/autologin,general/register/register,general/shareredpacked/shareredpacked,general/settingsPage/settingsPage,general/landingPage/landingPage,general/registrationPage/registrationPage,general/register_phone/register_phone,general/regiter_complete/regiter_complete,general/retrievePage/retrievePage,general/reguster_enterprise/reguster_enterprise,general/reguster_company/reguster_company,general/reguster_information/reguster_information,general/privacyPage/privacyPage,general/userAgreementPage/userAgreementPage,general/productlist/productlist,general/sureorder/sureorder,general/shopdesc/shopdesc,general/feedback/feedback,general/searchpage/searchpage,general/news/news,general/logistics/logistics,general/transaction/transaction,general/shoplist/shoplist,general/webview/webview,general/countryList/countryList,general/scanlogin/scanlogin,packet1/check/check,packet1/checkRecords/checkRecords,packet1/checkLists/checkLists,packet1/articleDetail/articleDetail,packet1/chainCheck/chainCheck,packet1/chainCheckLists/chainCheckLists,packet1/settingLanguage/settingLanguage,packet1/video/video,packet1/settingCurrency/settingCurrency,member/footprint/footprint,member/collection/collection,member/plus/plus,member/plus/plusActivate,member/plus/plusRechargeRecord,member/orderList/orderList,member/orderVirtual/orderVirtual,member/deliveryOrder/deliveryOrder,member/myfeedback/myfeedback,member/userFeedback/userFeedback,member/feedbackSubmit/feedbackSubmit,member/feedbackDetail/feedbackDetail,member/orderreturnlist/orderreturnlist,member/chainList/chainList,member/orderdesc/orderdesc,member/shippingdesc/shippingdesc,member/orderChain/orderChain,member/returnitem/returnitem,member/returnsectionmoney/returnsectionmoney,member/offlinePayment/offlinePayment,member/offlinePayStatus/offlinePayStatus,member/payStatus/payStatus,member/invoiceList/invoiceList,member/invoiceManage/invoiceManage,member/invoiceApply/invoiceApply,member/addInvoiceAddress/addInvoiceAddress,member/invoiceSelect/invoiceSelect,member/invoiceDetails/invoiceDetails,member/forservice/forservice,member/returnStatus/returnStatus,member/platformApply/platformApply,member/returnPlatFormDetails/returnPlatFormDetails,member/proof/proof,member/myhComplaint/myhComplaint,member/deliveryProblem/deliveryProblem,member/complaintDetails/complaintDetails,member/complainsRecords/complainsRecords,member/evaluateRiders/evaluateRiders,member/barter/barter,member/barterDetails/barterDetails,integral/index/index,integral/integral_product_list/integral_product_list,integral/integral_product_detail/integral_product_detail,integral/integral_product_buy/integral_product_buy,integral/integral_order_list/integral_order_list,integral/signin/signin,integral/integral_classification/integral_classification,integral/integral_details/integral_details,distributionCenter/index/index,distributionCenter/applyWechat/applyWechat,distributionCenter/settlementToday/settlementToday,distributionCenter/withdrawalRecord/withdrawalRecord,distributionCenter/accumulatedCommission/accumulatedCommission,distributionCenter/estimatedRevenue/estimatedRevenue,distributionCenter/myClients/myClients,distributionCenter/promoteCustomers/promoteCustomers,distributionCenter/shareProducts/shareProducts,distributionCenter/commissionOrder/commissionOrder,distributionCenter/giftPackGoods/giftPackGoods,distributionCenter/myShop/myShop,distributionCenter/myShopMore/myShopMore,distributionCenter/productLibrary/productLibrary,distributionCenter/shopIntroduceEdit/shopIntroduceEdit,distributionCenter/withdrawMoney/withdrawMoney,distributionCenter/storePromotion/storePromotion,activities/fullDiscount/fullDiscount,activities/offermessage/offermessage,activities/getredenvelopes/getredenvelopes,activities/getcoupon/getcoupon,activities/voucheritems/voucheritems,activities/coupon/coupon,activities/redenvelopes/redenvelopes,activities/pintuan_index/pintuan_index,activities/pintuan_lists/pintuan_lists,activities/luckdraw/luckdraw,activities/limitedTimeDiscount/limitedTimeDiscount,activities/secondKillGoods/secondKillGoods,activities/sureorderVoucher/sureorderVoucher,activities/information/information,activities/informationDetails/informationDetails,activities/combinationPromotion/combinationPromotion,activities/combinationDetails/combinationDetails,activities/exchangeCouponList/exchangeCouponList,activities/exchangeCouponIndex/exchangeCouponIndex,activities/discounGoods/discounGoods,find/publish/publish,find/orderGoods/orderGoods,find/goodsList/goodsList,find/topicList/topicList,find/homePage/homePage,find/details/details,find/relShows/relShows,find/editInfo/editInfo,find/userSign/userSign,find/bgWall/bgWall,find/followList/followList,find/fansList/fansList,find/newFriends/newFriends,find/video/video,find/zanList/zanList,find/message/message,find/commentsList/commentsList,find/saveList/saveList,store/index/index,store/nearbyStores/nearbyStores,store/nearbyGoods/nearbyGoods,store/shopDiy/shopDiy,store/chainStore/chainStore,store/shopsearch/shopsearch,setting/bindPhone/bindPhone,setting/bindPhone/home,setting/addresslist/addresslist,setting/addaddress/addaddress,setting/userInfos/userInfos,setting/modifyUserAvatar/modifyUserAvatar,setting/modifyUserNickname/modifyUserNickname,setting/securityIndex/securityIndex,setting/companyAgreementPage/companyAgreementPage,setting/certificationStep/certificationStep,setting/authenticated/authenticated,setting/logOut/logOut,setting/complaint/complaint,setting/complaintRecords/complaintRecords,setting/customerService/customerService,setting/aboutUs/aboutUs,setting/changePaymentPage/changePaymentPage,setting/changeBindingPhone/changeBindingPhone,setting/downloadApp/downloadApp,wallet/index/index,wallet/cardRecharge/cardRecharge,wallet/cashOut/cashOut,wallet/transfer/transfer,wallet/dealsList/dealsList,wallet/dealsDesc/dealsDesc,wallet/withdrawalRecord/withdrawalRecord,trade/payrecord/payrecord,trade/paymentPage/paymentPage,trade/paySuccess/paySuccess,trade/payFail/payFail,aisearch/index/index,tuikit/webLiveList/webLiveList,tuikit/webLiveMain/webLiveMain,tuikit/webLiveEnd/webLiveEnd,tuikit/livelist/index,tuikit/audience/index,tuikit/anchor/index,tuikit/liveend/index,tuikit/prize/prize,appletLive/live_status/live_status,appletLive/live_list/live_list";

    private static final java.util.Set<String> H5_PAGES = loadH5Pages();

    private static java.util.Set<String> loadH5Pages() {
        java.util.Set<String> set = new java.util.HashSet<>();
        for (String page : H5_PAGE_CSV.split(",")) {
            String trimmed = page.trim();
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        return set;
    }

    /** 后台链接语义 → H5 真实页面（两边命名完全不同的少数几个）。 */
    private static final Map<String, String> LINK_ALIAS = Map.of(
            "/pages/user_set/user_set", "/general/settingsPage/settingsPage",
            "/pages/as_us/as_us", "/pages/description/descrption",
            "/pages/news_detail/news_detail", "/activities/informationDetails/informationDetails",
            "/pages/agreement/agreement", "/general/privacyPage/privacyPage",
            "/pages/collection/collection", "/member/collection/collection",
            "/pages/news/news", "/general/news/news",
            "/pages/store/store", "/store/index/index");

    /**
     * 归一化跳转地址：只放行 H5 真正注册过的页面。
     *
     * @return 合法页面路径（不含前导斜杠），无法识别时返回空串
     */
    private String normalizePage(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return "";
        }
        String path = rawPath.startsWith("/") ? rawPath.substring(1) : rawPath;
        int query = path.indexOf('?');
        String bare = query >= 0 ? path.substring(0, query) : path;
        String queryPart = query >= 0 ? path.substring(query) : "";

        // uni-app 的 navigateTo 要求以 / 开头，这里统一补上前导斜杠
        String alias = LINK_ALIAS.get("/" + bare);
        if (alias != null) {
            return alias + queryPart;
        }
        if (H5_PAGES.contains(bare)) {
            return "/" + bare + queryPart;
        }
        // 后台统一带 pages/ 前缀，H5 多为无前缀子包
        if (bare.startsWith("pages/") && H5_PAGES.contains(bare.substring("pages/".length()))) {
            return "/" + bare.substring("pages/".length()) + queryPart;
        }
        if (H5_PAGES.contains("pages/" + bare)) {
            return "/" + bare + queryPart;
        }
        log.info("H5 桥接：跳转目标 {} 在 H5 未注册，不下发 link（H5 侧空值已保护）", rawPath);
        return "";
    }

    @SuppressWarnings("unchecked")
    private List<Object> list(Object value) {
        return value instanceof List<?> raw ? (List<Object>) raw : new ArrayList<>();
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int intOf(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}