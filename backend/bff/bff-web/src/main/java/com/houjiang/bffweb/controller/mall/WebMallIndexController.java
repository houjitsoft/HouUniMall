package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.util.MallFrontMediaHelper;
import com.houjiang.bffweb.util.MallStyleDefaults;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城前台首页公共接口（mall/uniapp & mall/pc）
/**
 *
 * 路径：/web/mall/index/* + /web/mall/indexVisit/*
 * 输出格式：与前端拦截器期望的 AjaxResult 同构 {code, msg, data, show}
 *
 * index/data 接口通过 Feign 调用 PAAS mall 服务读取 hou_base_decorate_page 表的装修数据
 *
 * @author Amos
 */
@Slf4j
@RestController
public class WebMallIndexController {

    @Autowired
    private MallServiceFeign mallServiceFeign;

    @Autowired
    private BaseServiceFeign baseServiceFeign;

    @Autowired
    private MallFrontMediaHelper mallFrontMediaHelper;

    /**
     * /web/mall/index/config
/**
     * 商城公共配置（首页加载必需）——同时兼容 GET/POST
     * 调用 PAAS mall 服务读取真实数据（tabbar、主题、客服等），失败降级为骨架配置
     */
    @RequestMapping(value = "/web/mall/index/config", method = {RequestMethod.GET, RequestMethod.POST})
    @SuppressWarnings("unchecked")
    public Map<String, Object> config() {
        try {
            ResponseResult result = mallServiceFeign.getFrontIndexConfig();
            if (result != null && result.getData() != null) {
                Object raw = result.getData();
                if (raw instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) raw;
                    MallStyleDefaults.enrichIndexConfig(data);
                    mallFrontMediaHelper.enrichIndexConfig(data);
                    return ok(data);
                }
            }
        } catch (Exception e) {
            log.warn("调用 PAAS mall config 失败，降级为骨架配置", e);
        }
        // 降级：返回最小可用的默认配置
        Map<String, Object> skeleton = buildSkeletonConfig();
        MallStyleDefaults.enrichIndexConfig(skeleton);
        mallFrontMediaHelper.enrichIndexConfig(skeleton);
        return ok(skeleton);
    }

    /**
     * 构建最小可用的骨架配置（降级用）
/**
     *
     * @return 默认配置 Map
     */
    private Map<String, Object> buildSkeletonConfig() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("domain", "");

        Map<String, Object> style = new LinkedHashMap<>();
        style.put("defaultColor", "#999999");
        style.put("selectedColor", "#4A5DFF");
        style.put("default_color", "#999999");
        style.put("selected_color", "#4A5DFF");
        data.put("style", style);
        data.put("tabbar", new ArrayList<>());

        Map<String, Object> login = new LinkedHashMap<>();
        login.put("login_way", List.of("1", "2"));
        login.put("coerce_mobile", 0);
        login.put("login_agreement", 0);
        login.put("third_auth", 0);
        login.put("wechat_auth", 0);
        login.put("qq_auth", 0);
        data.put("login", login);

        Map<String, Object> website = new LinkedHashMap<>();
        website.put("shop_name", "houcloud商城");
        website.put("shop_logo", "");
        data.put("website", website);

        Map<String, Object> webPage = new LinkedHashMap<>();
        webPage.put("status", 1);
        webPage.put("page_status", 0);
        webPage.put("page_url", "");
        webPage.put("url", "");
        data.put("webPage", webPage);

        data.put("version", "v1.0.0");

        Map<String, Object> mallStyle = new LinkedHashMap<>();
        mallStyle.put("nav_bg_color", "#FFFFFF");
        mallStyle.put("nav_text_color", "#1A1A1A");
        data.put("mall_style", mallStyle);

        Map<String, Object> serviceConfig = new LinkedHashMap<>();
        serviceConfig.put("status", 0);
        serviceConfig.put("type", 0);
        data.put("service_config", serviceConfig);

        return data;
    }

    /**
     * /web/mall/index/policy?type=service|privacy
/**
     * 政策协议（服务协议 / 隐私协议）
     */
    @RequestMapping(value = "/web/mall/index/policy", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> policy(@RequestParam(value = "type", required = false) String type) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", "");
        data.put("content", "");
        return ok(data);
    }

    /**
     * /web/mall/index/category
/**
     * 首页商品分类（顶级）
     */
    @RequestMapping(value = "/web/mall/index/category", method = {RequestMethod.GET, RequestMethod.POST})
    @SuppressWarnings("unchecked")
    public Map<String, Object> category() {
        try {
            ResponseResult result = mallServiceFeign.getFrontIndexCategory();
            if (result != null && result.getData() != null) {
                Object raw = result.getData();
                if (raw instanceof Map) {
                    return ok((Map<String, Object>) raw);
                }
            }
        } catch (Exception e) {
            log.warn("调用 PAAS mall category 失败，降级为空列表", e);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("category_lists", new ArrayList<>());
        return ok(data);
    }

    /**
     * /web/mall/index/data
/**
     * 首页聚合数据（轮播 / 楼层 / 推荐）
     *
     * 通过 Feign 调用 PAAS mall 服务的 /mall/front/index/data，
     * 从 hou_base_decorate_page 表读取装修数据
     */
    @RequestMapping(value = "/web/mall/index/data", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> data(@RequestParam(value = "category_id", required = false) Integer categoryId,
                                    @RequestBody(required = false) Map<String, Object> body) {
        try {
            Map<String, Object> params = body != null ? body : new HashMap<>();
            if (categoryId != null) {
                params.put("category_id", categoryId);
            }
            ResponseResult result = mallServiceFeign.getFrontIndexData(params);
            if (result != null && result.getCode() == 1 && result.getData() != null) {
                Object raw = result.getData();
                if (raw instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) raw;
                    mallFrontMediaHelper.enrichIndexData(data);
                    return ok(data);
                }
                return ok(result.getData());
            }
        } catch (Exception e) {
            log.warn("Feign 调用 mall 首页数据失败，返回默认骨架", e);
        }
        // 降级：返回最小可用骨架数据（避免白屏）
        return fallbackIndexData();
    }

    /**
     * BBC PC 首页楼层及楼层广告。
     * 楼层仍由 base/floorAdpage 维护，这里统一通过 BFF 对外提供公开查询，避免浏览器直连内部服务。
     */
    @GetMapping({"/web/mall/index/floors", "/web/mall/index/floor"})
    @SuppressWarnings("unchecked")
    public Map<String, Object> floors() {
        try {
            ResponseResult result = baseServiceFeign.queryAllAdpageFloor();
            if (result != null && result.getCode() == 1) {
                Object data = result.getData();
                if (data instanceof Map && ((Map<String, Object>) data).containsKey("data")) {
                    data = ((Map<String, Object>) data).get("data");
                }
                Map<String, Object> response = ok(data == null ? new ArrayList<>() : data);
                response.put("source", "base");
                return response;
            }
        } catch (Exception e) {
            log.warn("查询 PC 首页楼层失败，返回空楼层", e);
        }
        return ok(new ArrayList<>());
    }

    /**
     * 降级骨架数据：首页最小可用配置
     */
    private Map<String, Object> fallbackIndexData() {
        Map<String, Object> headerContent = new LinkedHashMap<>();
        headerContent.put("title", "首页");
        headerContent.put("title_type", 1);
        headerContent.put("title_color", 1);
        headerContent.put("bg_img", "");
        headerContent.put("bg_color", "#FFFFFF");
        headerContent.put("scroll_type", 1);
        headerContent.put("scroll_color", "#FFFFFF");
        headerContent.put("scroll_img", "");
        headerContent.put("titlebg_type", 1);
        headerContent.put("titlebg_color", "#FFFFFF");
        headerContent.put("titlebg_img", "");

        Map<String, Object> headerItem = new LinkedHashMap<>();
        headerItem.put("name", "header");
        headerItem.put("content", headerContent);

        List<Object> headerGroup = new ArrayList<>();
        headerGroup.add(headerItem);
        List<Object> pageData = new ArrayList<>();
        pageData.add(headerGroup);

        Map<String, Object> page = new LinkedHashMap<>();
        page.put("data", pageData);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("page", page);
        data.put("article", new ArrayList<>());
        data.put("shop_recommend", new ArrayList<>());
        return ok(data);
    }

    /**
     * /web/mall/index/hotSearch
     * 热门搜索词（读 PAAS mall 热搜配置）
     */
    @RequestMapping(value = "/web/mall/index/hotSearch", method = {RequestMethod.GET, RequestMethod.POST})
    @SuppressWarnings("unchecked")
    public Map<String, Object> hotSearch() {
        try {
            ResponseResult result = mallServiceFeign.getFrontHotSearch();
            if (result != null && result.getData() != null) {
                return ok(result.getData());
            }
        } catch (Exception e) {
            log.warn("调用热门搜索失败", e);
        }
        return ok(new ArrayList<>());
    }

    /**
     * /web/mall/index/decorate
     * 按 type 读取装修页
     */
    @RequestMapping(value = "/web/mall/index/decorate", method = {RequestMethod.GET, RequestMethod.POST})
    @SuppressWarnings("unchecked")
    public Map<String, Object> decorate(@RequestParam(value = "id", required = false) Integer id,
                                        @RequestParam(value = "type", required = false) Integer type,
                                        @RequestBody(required = false) Map<String, Object> body) {
        try {
            Map<String, Object> params = body != null ? body : new HashMap<>();
            if (type != null) {
                params.put("type", type);
            } else if (id != null) {
                params.put("type", id);
            } else if (!params.containsKey("type")) {
                params.put("type", 1);
            }
            ResponseResult result = mallServiceFeign.getFrontDecorate(params);
            if (result != null && result.getData() != null) {
                return ok(result.getData());
            }
        } catch (Exception e) {
            log.warn("调用装修页失败", e);
        }
        Map<String, Object> empty = new LinkedHashMap<>();
        empty.put("data", new ArrayList<>());
        return ok(empty);
    }

    /**
     * POST /web/mall/indexVisit/add
/**
     * 访问统计上报（当前为空操作，等 mall_index_visit 表落库后再写入）
     */
    @PostMapping("/web/mall/indexVisit/add")
    public Map<String, Object> indexVisitAdd(@RequestBody(required = false) Map<String, Object> params) {
        return ok(new HashMap<>());
    }

    /**
     * 构造前端拦截器期望的 AjaxResult 同构成功包：{code:1, msg:'', data:..., show:0}
     */
    private static Map<String, Object> ok(Object data) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("code", 1);
        resp.put("msg", "");
        resp.put("data", data);
        resp.put("show", 0);
        return resp;
    }
}
