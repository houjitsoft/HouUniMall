package com.houjiang.mall.controller.setting;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.setting.SettingSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商城后台 - 热门搜索 Controller。
 * 对应 like-admin SettingSearchController（/adminapi/setting.hot_search）。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/setting/search")
@RequiredArgsConstructor
public class MallSettingSearchController {

    private final SettingSearchService searchService;

    /**
     * 热门搜索详情。
     * GET /mall/setting/search/lists
     */
    @GetMapping("/lists")
    public ResponseResult<Map<String, Object>> lists() {
        return ResponseResult.success(searchService.getConfig());
    }

    /**
     * 热门搜索编辑。
     * POST /mall/setting/search/edit
     * body: {status, data:[{name, sort}]}
     */
    @PostMapping("/edit")
    public ResponseResult<Void> edit(@RequestBody Map<String, Object> params) {
        searchService.setConfig(params);
        return ResponseResult.success();
    }
}
