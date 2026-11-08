package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 商城装修模块 BFF — 显式转发 Authorization 至 mall（MALL_ADMIN_TOKEN 鉴权）
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/decorate")
@RequiredArgsConstructor
public class MallDecorateController {

    private final MallServiceFeign mallServiceFeign;

    @GetMapping("/page/detail")
    public ResponseResult getPageDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) Long page_id,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Integer type) {
        Map<String, Object> params = new HashMap<>();
        Long resolvedId = id != null ? id : page_id;
        if (resolvedId != null) {
            params.put("id", resolvedId);
        }
        if (type != null) {
            params.put("type", type);
        }
        return mallServiceFeign.getDecoratePageDetail(params, authorization);
    }

    @PostMapping("/page/save")
    public ResponseResult savePage(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> params) {
        return mallServiceFeign.saveDecoratePage(params, authorization);
    }

    @GetMapping("/page/home_lists")
    public ResponseResult getHomeList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, Object> query) {
        Map<String, Object> params = query != null ? new HashMap<>(query) : new HashMap<>();
        return mallServiceFeign.getHomePageList(params, authorization);
    }

    @PostMapping("/page/delete")
    public ResponseResult deletePage(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> params) {
        return mallServiceFeign.deleteDecoratePage(params, authorization);
    }

    @PostMapping("/page/add")
    public ResponseResult addPage(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> params) {
        return mallServiceFeign.addDecoratePage(params, authorization);
    }

    @PostMapping("/page/set_home")
    public ResponseResult setHomePage(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> params) {
        return mallServiceFeign.setHomePage(params, authorization);
    }

    /**
     * 移动端页面模板库（后台「模板选择」第二步用）。
     *
     * @param authorization 管理员 token
     * @return 模板列表
     */
    @PostMapping("/page/template/list")
    public ResponseResult templateList(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.getDecoratePageTemplates(authorization);
    }

    /**
     * 读取某装修页面当前套用的模板 code。
     *
     * @param authorization 管理员 token
     * @param params { pageId }
     * @return templateCode，未套用为空串
     */
    @PostMapping("/page/template/current")
    public ResponseResult currentTemplate(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> params) {
        return mallServiceFeign.getDecoratePageTemplate(params, authorization);
    }

    /**
     * 为装修页面套用模板（templateCode 传空串表示取消）。
     *
     * @param authorization 管理员 token
     * @param params { pageId, templateCode }
     * @return 实际生效的 code
     */
    @PostMapping("/page/template/apply")
    public ResponseResult applyTemplate(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> params) {
        return mallServiceFeign.applyDecoratePageTemplate(params, authorization);
    }

    @GetMapping("/data/article")    public ResponseResult getArticleData(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("size", 20);
        return mallServiceFeign.getDecorateArticleData(params, authorization);
    }

    @GetMapping("/tabbar/detail")
    public ResponseResult getTabbarDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) Long shop_id) {
        Map<String, Object> params = new HashMap<>();
        if (shop_id != null) {
            params.put("shop_id", shop_id);
        }
        return mallServiceFeign.getTabbarDetail(params, authorization);
    }

    @PostMapping("/tabbar/save")
    public ResponseResult saveTabbar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> params) {
        return mallServiceFeign.saveTabbar(params, authorization);
    }
}
